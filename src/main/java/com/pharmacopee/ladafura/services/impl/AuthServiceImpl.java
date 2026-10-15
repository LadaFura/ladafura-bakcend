package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.auth.UserRecord;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.auth.AuthMeResponse;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.UnauthorizedException;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IAuthService;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final IFirebaseAuthService firebaseAuthService;

    @Override
    @Transactional
    public AuthMeResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Tentative d'accès non authentifié sur /api/v1/auth/me");
            throw new UnauthorizedException("Authentification requise pour effectuer cette opération.");
        }

        String identifier = authentication.getName();

        // Recherche par email puis par firebaseUid
        Optional<Utilisateur> optUser = utilisateurRepository.findByEmail(identifier)
                .or(() -> utilisateurRepository.findByFirebaseUid(identifier));

        Utilisateur utilisateur = optUser.orElseGet(() -> autoProvisionFirebaseUser(identifier, authentication));

        if (utilisateur.getStatut() != StatutUtilisateur.ACTIF) {
            log.warn("Compte inactif ou suspendu pour l'utilisateur ID: {} (statut={})", utilisateur.getId(), utilisateur.getStatut());
            throw new ForbiddenException("Votre compte est inactif ou désactivé. Veuillez contacter le support.");
        }

        return AuthMeResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .telephone(utilisateur.getTelephone())
                .role(utilisateur.getRole())
                .statut(utilisateur.getStatut())
                .firebaseUid(utilisateur.getFirebaseUid())
                .dateCreation(utilisateur.getDateCreation())
                .build();
    }

    private Utilisateur autoProvisionFirebaseUser(String identifier, Authentication authentication) {
        log.info("Auto-provisioning du profil utilisateur dans la base de données pour : {}", identifier);

        String email = identifier.contains("@") ? identifier : null;
        String firebaseUid = identifier;
        String displayName = null;
        String phone = null;

        // 1. Tenter d'extraire les infos depuis le jeton Firebase
        try {
            if (authentication.getCredentials() != null) {
                String tokenStr = authentication.getCredentials().toString();
                var token = firebaseAuthService.verifyIdToken(tokenStr);
                if (token.getUid() != null) {
                    firebaseUid = token.getUid();
                }
                if (token.getEmail() != null) {
                    email = token.getEmail();
                }
                if (token.getName() != null) {
                    displayName = token.getName();
                }
            }
        } catch (Exception e) {
            log.debug("Impossible d'extraire les métadonnées depuis le jeton : {}", e.getMessage());
        }

        // 2. Tenter de récupérer les métadonnées Firebase UserRecord
        try {
            UserRecord record = null;
            if (firebaseUid != null && !firebaseUid.contains("@")) {
                record = firebaseAuthService.getFirebaseUser(firebaseUid);
            } else if (email != null) {
                record = com.google.firebase.auth.FirebaseAuth.getInstance().getUserByEmail(email);
            }
            if (record != null) {
                firebaseUid = record.getUid();
                if (email == null) email = record.getEmail();
                if (displayName == null) displayName = record.getDisplayName();
                if (phone == null) phone = record.getPhoneNumber();
            }
        } catch (Exception ex) {
            log.debug("UserRecord Firebase introuvable pour {} : {}", identifier, ex.getMessage());
        }

        Utilisateur newUser = new Utilisateur();
        newUser.setEmail(email != null ? email : identifier);
        newUser.setFirebaseUid(firebaseUid);
        if (displayName != null && !displayName.isBlank()) {
            String[] parts = displayName.trim().split("\\s+", 2);
            newUser.setPrenom(parts[0]);
            newUser.setNom(parts.length > 1 ? parts[1] : parts[0]);
        } else {
            newUser.setPrenom("Utilisateur");
            newUser.setNom("Citoyen");
        }
        if (phone != null) {
            newUser.setTelephone(phone);
        }
        newUser.setRole(Role.POPULATION);
        newUser.setStatut(StatutUtilisateur.ACTIF);
        newUser.setDateCreation(LocalDateTime.now());

        Utilisateur saved = utilisateurRepository.save(newUser);
        log.info("Nouveau compte utilisateur auto-provisionné avec succès : ID={}, Email={}, Role={}",
                saved.getId(), saved.getEmail(), saved.getRole());
        return saved;
    }
}
