package com.pharmacopee.ladafura.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.admin.user.AdminChangeStatusRequest;
import com.pharmacopee.ladafura.dto.admin.user.AdminCreateUserRequest;
import com.pharmacopee.ladafura.dto.admin.user.AdminUpdateUserRequest;
import com.pharmacopee.ladafura.dto.admin.user.AdminUserResponse;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AdminUserMapper;
import com.pharmacopee.ladafura.repository.AgentCollecteRepository;
import com.pharmacopee.ladafura.repository.SourceRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminUserService;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminUserServiceImpl implements IAdminUserService {

    private final UtilisateurRepository utilisateurRepository;
    private final AgentCollecteRepository agentCollecteRepository;
    private final SourceRepository sourceRepository;
    private final AdminUserMapper userMapper;
    private final IFirebaseAuthService firebaseAuthService;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAllUsers(Role role, StatutUtilisateur statut, Pageable pageable) {
        log.info("Récupération paginée des utilisateurs (role={}, statut={})", role, statut);
        Page<Utilisateur> page;
        if (role != null && statut != null) {
            page = utilisateurRepository.findByRoleAndStatut(role, statut, pageable);
        } else if (role != null) {
            page = utilisateurRepository.findByRole(role, pageable);
        } else if (statut != null) {
            page = utilisateurRepository.findByStatut(statut, pageable);
        } else {
            page = utilisateurRepository.findAll(pageable);
        }
        return page.map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long id) {
        log.info("Recherche de l'utilisateur avec l'ID {}", id);
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        return userMapper.toDto(user);
    }

    @Override
    public AdminUserResponse createUser(AdminCreateUserRequest request) {
        log.info("Création d'un nouvel utilisateur avec email {}", request.getEmail());

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Utilisateur", "email", request.getEmail());
        }

        // 1. Création du compte dans Firebase Authentication
        String displayName = request.getPrenom() + " " + request.getNom();
        String firebaseUid = firebaseAuthService.createUser(request.getEmail(), request.getMotDePasse(), displayName);
        firebaseAuthService.setRole(firebaseUid, request.getRole().name());

        // 2. Persistance dans MySQL selon le type de compte (SANS enregistrer le mot de passe)
        Utilisateur user;
        if (request.getRole() == Role.AGENT_COLLECTE) {
            AgentCollecte agent = new AgentCollecte();
            agent.setMatricule(request.getMatricule());
            agent.setZoneCouverture(request.getZoneCouverture());
            user = agent;
        } else if (request.getRole() == Role.PHARMACOPEE && request.getSpecialite() != null) {
            Source source = new Source();
            source.setSpecialite(request.getSpecialite());
            source.setAnneesExperience(request.getAnneesExperience());
            source.setAdresse(request.getAdresse());
            user = source;
        } else {
            user = new Utilisateur();
        }

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setTelephone(request.getTelephone());
        user.setRole(request.getRole());
        user.setStatut(request.getStatut() != null ? request.getStatut() : StatutUtilisateur.ACTIF);
        user.setFirebaseUid(firebaseUid);

        Utilisateur savedUser = utilisateurRepository.save(user);
        log.info("Utilisateur créé avec succès : ID {} / UID {}", savedUser.getId(), firebaseUid);
        return userMapper.toDto(savedUser);
    }

    @Override
    public AdminUserResponse updateUser(Long id, AdminUpdateUserRequest request) {
        log.info("Mise à jour des informations de l'utilisateur {}", id);
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        if (request.getNom() != null) user.setNom(request.getNom());
        if (request.getPrenom() != null) user.setPrenom(request.getPrenom());
        if (request.getTelephone() != null) user.setTelephone(request.getTelephone());

        if (request.getRole() != null && user.getRole() != request.getRole()) {
            user.setRole(request.getRole());
            if (user.getFirebaseUid() != null) {
                firebaseAuthService.setRole(user.getFirebaseUid(), request.getRole().name());
            }
        }

        if (request.getStatut() != null && user.getStatut() != request.getStatut()) {
            user.setStatut(request.getStatut());
            syncFirebaseUserStatus(user.getFirebaseUid(), request.getStatut());
        }

        if (user instanceof AgentCollecte agent) {
            if (request.getMatricule() != null) agent.setMatricule(request.getMatricule());
            if (request.getZoneCouverture() != null) agent.setZoneCouverture(request.getZoneCouverture());
        } else if (user instanceof Source source) {
            if (request.getSpecialite() != null) source.setSpecialite(request.getSpecialite());
            if (request.getAnneesExperience() != null) source.setAnneesExperience(request.getAnneesExperience());
            if (request.getAdresse() != null) source.setAdresse(request.getAdresse());
        }

        Utilisateur updatedUser = utilisateurRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public AdminUserResponse changeUserStatus(Long id, AdminChangeStatusRequest request) {
        log.info("Changement de statut de l'utilisateur {} vers {}", id, request.getStatut());
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        user.setStatut(request.getStatut());
        syncFirebaseUserStatus(user.getFirebaseUid(), request.getStatut());

        Utilisateur saved = utilisateurRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Suppression de l'utilisateur {}", id);
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        if (user.getFirebaseUid() != null) {
            firebaseAuthService.deleteUser(user.getFirebaseUid());
        }
        utilisateurRepository.delete(user);
    }

    private void syncFirebaseUserStatus(String firebaseUid, StatutUtilisateur statut) {
        if (firebaseUid == null) return;
        if (statut == StatutUtilisateur.ACTIF) {
            firebaseAuthService.enableUser(firebaseUid);
        } else {
            firebaseAuthService.disableUser(firebaseUid);
        }
    }
}
