package com.pharmacopee.ladafura.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Praticien;
import com.pharmacopee.ladafura.dto.pharmacopee.collaborateur.CreateCollaborateurRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.collaborateur.PharmacopeeCollaborateurResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.collaborateur.UpdateCollaborateurRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PraticienRepository;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeCollaborateurService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PharmacopeeCollaborateurServiceImpl implements IPharmacopeeCollaborateurService {

    private final PraticienRepository praticienRepository;
    private final PharmacopeeRepository pharmacopeeRepository;
    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final PasswordEncoder passwordEncoder;
    private final IFirebaseAuthService firebaseAuthService;

    @Override
    @Transactional(readOnly = true)
    public Page<PharmacopeeCollaborateurResponse> getCollaborateurs(Pageable pageable) {
        Long pharmacopeeId = pharmacopeeAuthService.getMe().getPharmacopeeId();
        if (pharmacopeeId == null) {
            throw new ForbiddenException("Aucune pharmacopée active");
        }

        return praticienRepository.findByPharmacopeesIdAndEstPraticienPrincipalFalse(pharmacopeeId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacopeeCollaborateurResponse getCollaborateurById(Long id) {
        Long pharmacopeeId = pharmacopeeAuthService.getMe().getPharmacopeeId();
        if (pharmacopeeId == null) {
            throw new ForbiddenException("Aucune pharmacopée active");
        }

        Praticien collaborateur = praticienRepository.findByIdAndPharmacopeesIdAndEstPraticienPrincipalFalse(id, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborateur non trouvé ou non autorisé"));

        return mapToResponse(collaborateur);
    }

    @Override
    @Transactional
    public PharmacopeeCollaborateurResponse addCollaborateur(CreateCollaborateurRequest request) {
        pharmacopeeAuthService.verifyPraticienPrincipal();

        Long pharmacopeeId = pharmacopeeAuthService.getMe().getPharmacopeeId();
        Pharmacopee pharmacopee = pharmacopeeRepository.findById(pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée non trouvée"));

        if (praticienRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        // Créer l'utilisateur dans Firebase en premier
        String firebaseUid = firebaseAuthService.createUser(
            request.getEmail(), 
            request.getMotDePasse(), 
            request.getPrenom() + " " + request.getNom()
        );
        
        // Assigner le rôle PHARMACOPEE dans Firebase
        firebaseAuthService.setRole(firebaseUid, Role.PHARMACOPEE.name());

        Praticien collaborateur = new Praticien();
        collaborateur.setNom(request.getNom());
        collaborateur.setPrenom(request.getPrenom());
        collaborateur.setEmail(request.getEmail());
        collaborateur.setTelephone(request.getTelephone());
        collaborateur.setSpecialite(request.getSpecialite());
        collaborateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        collaborateur.setFirebaseUid(firebaseUid); // Ne pas oublier l'UID
        collaborateur.setRole(Role.PHARMACOPEE);
        collaborateur.setEstPraticienPrincipal(false);
        collaborateur.setStatut(StatutUtilisateur.ACTIF);
        
        // Associate with pharmacopee
        collaborateur.getPharmacopees().add(pharmacopee);

        Praticien saved = praticienRepository.save(collaborateur);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public PharmacopeeCollaborateurResponse updateCollaborateur(Long id, UpdateCollaborateurRequest request) {
        pharmacopeeAuthService.verifyPraticienPrincipal();

        Long pharmacopeeId = pharmacopeeAuthService.getMe().getPharmacopeeId();
        Praticien collaborateur = praticienRepository.findByIdAndPharmacopeesIdAndEstPraticienPrincipalFalse(id, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborateur non trouvé ou non autorisé"));

        if (!collaborateur.getEmail().equals(request.getEmail()) && praticienRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        collaborateur.setNom(request.getNom());
        collaborateur.setPrenom(request.getPrenom());
        collaborateur.setEmail(request.getEmail());
        collaborateur.setTelephone(request.getTelephone());
        collaborateur.setSpecialite(request.getSpecialite());

        // Gérer le changement de statut Firebase si nécessaire
        if (collaborateur.getStatut() != request.getStatut() && collaborateur.getFirebaseUid() != null) {
            if (request.getStatut() == StatutUtilisateur.ACTIF) {
                firebaseAuthService.enableUser(collaborateur.getFirebaseUid());
            } else {
                firebaseAuthService.disableUser(collaborateur.getFirebaseUid());
            }
        }
        
        collaborateur.setStatut(request.getStatut());

        Praticien saved = praticienRepository.save(collaborateur);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCollaborateur(Long id) {
        pharmacopeeAuthService.verifyPraticienPrincipal();

        Long pharmacopeeId = pharmacopeeAuthService.getMe().getPharmacopeeId();
        Praticien collaborateur = praticienRepository.findByIdAndPharmacopeesIdAndEstPraticienPrincipalFalse(id, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborateur non trouvé ou non autorisé"));

        // Retirer la pharmacopée du collaborateur (ou supprimer le collaborateur s'il n'a qu'une pharmacopée)
        Pharmacopee pharmacopee = pharmacopeeRepository.findById(pharmacopeeId).orElseThrow();
        collaborateur.getPharmacopees().remove(pharmacopee);
        
        if (collaborateur.getPharmacopees().isEmpty()) {
            String uid = collaborateur.getFirebaseUid();
            praticienRepository.delete(collaborateur);
            
            if (uid != null) {
                try {
                    firebaseAuthService.deleteUser(uid);
                } catch (Exception e) {
                    log.error("Impossible de supprimer le collaborateur dans Firebase (UID: {})", uid, e);
                }
            }
        } else {
            praticienRepository.save(collaborateur);
        }
    }

    private PharmacopeeCollaborateurResponse mapToResponse(Praticien praticien) {
        return PharmacopeeCollaborateurResponse.builder()
                .id(praticien.getId())
                .nom(praticien.getNom())
                .prenom(praticien.getPrenom())
                .email(praticien.getEmail())
                .telephone(praticien.getTelephone())
                .specialite(praticien.getSpecialite())
                .statut(praticien.getStatut())
                .dateCreation(praticien.getDateCreation())
                .build();
    }
}
