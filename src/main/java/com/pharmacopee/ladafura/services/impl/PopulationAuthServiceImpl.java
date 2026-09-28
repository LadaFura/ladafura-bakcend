package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.auth.PopulationAuthResponse;
import com.pharmacopee.ladafura.dto.population.auth.PopulationRegisterRequest;
import com.pharmacopee.ladafura.dto.population.auth.PopulationSyncRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.UnauthorizedException;
import com.pharmacopee.ladafura.mappers.PopulationAuthMapper;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopulationAuthServiceImpl implements IPopulationAuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final IFirebaseAuthService firebaseAuthService;
    private final PopulationAuthMapper populationAuthMapper;

    @Override
    @Transactional
    public PopulationAuthResponse register(PopulationRegisterRequest request) {
        log.info("Tentative d'inscription d'un nouvel utilisateur Population (email: {})", request.getEmail());

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            log.warn("Tentative d'inscription avec un email déjà existant : {}", request.getEmail());
            throw new ConflictException("Utilisateur", "email", request.getEmail());
        }

        // 1. Création du compte dans Firebase Authentication
        String displayName = request.getPrenom() + " " + request.getNom();
        String firebaseUid;
        try {
            firebaseUid = firebaseAuthService.createUser(request.getEmail(), request.getMotDePasse(), displayName);
        } catch (Exception e) {
            log.error("Échec de création du compte dans Firebase : {}", e.getMessage());
            throw new BadRequestException("Échec de la création du compte dans le service d'authentification : " + e.getMessage());
        }

        // 2. Attribution du custom claim de rôle POPULATION dans Firebase
        try {
            firebaseAuthService.setRole(firebaseUid, Role.POPULATION.name());
        } catch (Exception e) {
            log.warn("Impossible d'attribuer le custom claim POPULATION dans Firebase pour l'UID {}: {}", firebaseUid, e.getMessage());
        }

        // 3. Persistance dans MySQL (sans stocker le mot de passe)
        Utilisateur user = new Utilisateur();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setTelephone(request.getTelephone());
        user.setRole(Role.POPULATION);
        user.setStatut(StatutUtilisateur.ACTIF);
        user.setFirebaseUid(firebaseUid);
        user.setDateCreation(LocalDateTime.now());

        Utilisateur savedUser = utilisateurRepository.save(user);
        log.info("Utilisateur Population créé avec succès : ID={}, UID={}", savedUser.getId(), firebaseUid);

        return populationAuthMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Utilisateur getCurrentPopulationUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Tentative d'accès non authentifié sur un endpoint Population");
            throw new UnauthorizedException("Authentification requise pour effectuer cette opération.");
        }

        String identifier = authentication.getName();

        // Recherche par email puis par firebaseUid
        Utilisateur utilisateur = utilisateurRepository.findByEmail(identifier)
                .or(() -> utilisateurRepository.findByFirebaseUid(identifier))
                .orElseThrow(() -> new UnauthorizedException("Aucun compte utilisateur trouvé pour l'identifiant : " + identifier));

        if (utilisateur.getStatut() != StatutUtilisateur.ACTIF) {
            log.warn("Compte inactif ou suspendu pour l'utilisateur ID: {} (statut={})", utilisateur.getId(), utilisateur.getStatut());
            throw new ForbiddenException("Votre compte est inactif ou désactivé. Veuillez contacter le support.");
        }

        if (utilisateur.getRole() != Role.POPULATION) {
            log.warn("Rôle non autorisé pour l'utilisateur ID: {} (rôle={})", utilisateur.getId(), utilisateur.getRole());
            throw new ForbiddenException("Accès réservé exclusivement aux utilisateurs du rôle POPULATION.");
        }

        return utilisateur;
    }

    @Override
    @Transactional(readOnly = true)
    public PopulationAuthResponse getMe() {
        Utilisateur user = getCurrentPopulationUser();
        return populationAuthMapper.toDto(user);
    }

    @Override
    @Transactional
    public PopulationAuthResponse syncFirebaseUser(PopulationSyncRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Authentification Firebase requise pour synchroniser le profil.");
        }

        String identifier = authentication.getName();

        Optional<Utilisateur> existing = utilisateurRepository.findByEmail(identifier)
                .or(() -> utilisateurRepository.findByFirebaseUid(identifier));

        if (existing.isPresent()) {
            Utilisateur user = existing.get();
            if (request != null) {
                if (request.getNom() != null && !request.getNom().isBlank()) {
                    user.setNom(request.getNom());
                }
                if (request.getPrenom() != null && !request.getPrenom().isBlank()) {
                    user.setPrenom(request.getPrenom());
                }
                if (request.getTelephone() != null && !request.getTelephone().isBlank()) {
                    user.setTelephone(request.getTelephone());
                }
            }
            if (user.getRole() == null) {
                user.setRole(Role.POPULATION);
            }
            return populationAuthMapper.toDto(utilisateurRepository.save(user));
        }

        // Création du compte si l'utilisateur s'est authentifié sur Firebase en premier
        String email = identifier.contains("@") ? identifier : null;
        String firebaseUid = identifier;

        Utilisateur newUser = new Utilisateur();
        newUser.setEmail(email != null ? email : identifier + "@firebase.ladafura.ml");
        newUser.setFirebaseUid(firebaseUid);
        newUser.setNom(request != null && request.getNom() != null ? request.getNom() : "Utilisateur");
        newUser.setPrenom(request != null && request.getPrenom() != null ? request.getPrenom() : "Population");
        if (request != null && request.getTelephone() != null) {
            newUser.setTelephone(request.getTelephone());
        }
        newUser.setRole(Role.POPULATION);
        newUser.setStatut(StatutUtilisateur.ACTIF);
        newUser.setDateCreation(LocalDateTime.now());

        Utilisateur saved = utilisateurRepository.save(newUser);
        log.info("Compte Population synchronisé depuis Firebase: ID={}, UID={}", saved.getId(), firebaseUid);

        return populationAuthMapper.toDto(saved);
    }
}
