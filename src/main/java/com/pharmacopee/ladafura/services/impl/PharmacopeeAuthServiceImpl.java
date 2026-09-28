package com.pharmacopee.ladafura.services.impl;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.auth.PharmacopeeAuthResponse;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.exceptions.UnauthorizedException;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PharmacopeeAuthServiceImpl implements IPharmacopeeAuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PharmacopeeRepository pharmacopeeRepository;

    @Override
    public Utilisateur getCurrentUtilisateur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Tentative d'accès non authentifié détectée");
            throw new UnauthorizedException("Authentification requise pour effectuer cette opération.");
        }

        String identifier = authentication.getName();

        // Recherche par email puis par firebaseUid
        Utilisateur utilisateur = utilisateurRepository.findByEmail(identifier)
                .or(() -> utilisateurRepository.findByFirebaseUid(identifier))
                .orElseThrow(() -> new UnauthorizedException("Aucun compte utilisateur trouvé pour l'identifiant : " + identifier));

        if (utilisateur.getStatut() != StatutUtilisateur.ACTIF) {
            log.warn("Compte inactif pour l'utilisateur ID: {}", utilisateur.getId());
            throw new ForbiddenException("Votre compte utilisateur est inactif ou désactivé. Veuillez contacter un administrateur.");
        }

        if (utilisateur.getRole() != Role.PHARMACOPEE && utilisateur.getRole() != Role.ADMINISTRATEUR) {
            log.warn("Rôle non autorisé pour l'utilisateur ID: {} (rôle={})", utilisateur.getId(), utilisateur.getRole());
            throw new ForbiddenException("Accès réservé exclusivement aux acteurs de type PHARMACOPEE.");
        }

        return utilisateur;
    }

    @Override
    public Pharmacopee getCurrentPharmacopee() {
        Utilisateur user = getCurrentUtilisateur();

        return pharmacopeeRepository.findByUtilisateurId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopee", "utilisateurId", user.getId()));
    }

    @Override
    public PharmacopeeAuthResponse getMe() {
        Utilisateur user = getCurrentUtilisateur();
        Optional<Pharmacopee> pharmacopeeOpt = pharmacopeeRepository.findByUtilisateurId(user.getId());

        PharmacopeeAuthResponse.PharmacopeeAuthResponseBuilder builder = PharmacopeeAuthResponse.builder()
                .utilisateurId(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .role(user.getRole())
                .statutUtilisateur(user.getStatut())
                .firebaseUid(user.getFirebaseUid());

        if (pharmacopeeOpt.isPresent()) {
            Pharmacopee p = pharmacopeeOpt.get();
            builder.pharmacopeeId(p.getId())
                   .nomPharmacopee(p.getNom())
                   .telephonePharmacopee(p.getTelephone())
                   .statutReferencement(p.getStatut())
                   .validee(p.getStatut() == StatutPharmacopee.VALIDEE);
        } else {
            builder.validee(false);
        }

        return builder.build();
    }

    @Override
    public void verifyPharmacopeeOwnership(Long requestedPharmacopeeId) {
        if (requestedPharmacopeeId == null) {
            throw new ForbiddenException("Identifiant de pharmacopée manquant.");
        }

        Pharmacopee currentPharmacopee = getCurrentPharmacopee();

        if (!currentPharmacopee.getId().equals(requestedPharmacopeeId)) {
            log.warn("Violation d'accès : la pharmacopée ID {} a tenté d'accéder aux ressources de la pharmacopée ID {}",
                    currentPharmacopee.getId(), requestedPharmacopeeId);
            throw new ForbiddenException("Accès refusé : vous n'êtes pas autorisé à accéder aux ressources d'une autre pharmacopée.");
        }
    }

    @Override
    public void verifyPharmacopeeValidated() {
        Pharmacopee currentPharmacopee = getCurrentPharmacopee();

        if (currentPharmacopee.getStatut() != StatutPharmacopee.VALIDEE) {
            log.warn("Tentative d'opération sur pharmacopée non validée ID: {}, statut: {}",
                    currentPharmacopee.getId(), currentPharmacopee.getStatut());
            throw new ForbiddenException("Opération impossible : votre établissement de pharmacopée n'est pas encore validé par l'administrateur (Statut : "
                    + currentPharmacopee.getStatut() + ").");
        }
    }
}
