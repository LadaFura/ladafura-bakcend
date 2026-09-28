package com.pharmacopee.ladafura.services.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.referencement.DemandeReferencementRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.referencement.StatutReferencementResponse;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeReferencementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacopeeReferencementServiceImpl implements IPharmacopeeReferencementService {

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final PharmacopeeRepository pharmacopeeRepository;

    @Override
    public StatutReferencementResponse soumettreDemandeReferencement(DemandeReferencementRequest request) {
        Utilisateur user = pharmacopeeAuthService.getCurrentUtilisateur();

        Optional<Pharmacopee> existante = pharmacopeeRepository.findByUtilisateurId(user.getId());
        if (existante.isPresent()) {
            Pharmacopee p = existante.get();
            log.warn("Tentative de soumission d'une 2ème demande par l'utilisateur ID {}. Statut existant: {}",
                    user.getId(), p.getStatut());
            throw new ConflictException("Une demande de référencement existe déjà pour votre compte (ID: "
                    + p.getId() + ", Statut: " + p.getStatut() + ").");
        }

        Pharmacopee pharmacopee = Pharmacopee.builder()
                .nom(request.getNom().trim())
                .description(request.getDescription())
                .telephone(request.getTelephone().trim())
                .statut(StatutPharmacopee.EN_ATTENTE)
                .utilisateur(user)
                .build();

        Pharmacopee saved = pharmacopeeRepository.save(pharmacopee);
        log.info("Nouvelle demande de référencement enregistrée pour l'utilisateur ID: {} (Pharmacopée ID: {})",
                user.getId(), saved.getId());

        return buildResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StatutReferencementResponse consulterStatutReferencement() {
        Utilisateur user = pharmacopeeAuthService.getCurrentUtilisateur();
        Optional<Pharmacopee> pharmacopeeOpt = pharmacopeeRepository.findByUtilisateurId(user.getId());

        if (pharmacopeeOpt.isEmpty()) {
            return StatutReferencementResponse.builder()
                    .pharmacopeeId(null)
                    .nomPharmacopee(null)
                    .telephone(null)
                    .statut(null)
                    .validee(false)
                    .message("Aucun dossier de référencement n'a encore été soumis pour votre compte. Veuillez soumettre une demande.")
                    .build();
        }

        return buildResponse(pharmacopeeOpt.get());
    }

    private StatutReferencementResponse buildResponse(Pharmacopee p) {
        String message;
        boolean validee = (p.getStatut() == StatutPharmacopee.VALIDEE);

        switch (p.getStatut()) {
            case EN_ATTENTE -> message = "Votre dossier de référencement est en cours d'étude par l'administration de LADAFURA. Vos fonctionnalités commerciales seront activées dès validation.";
            case VALIDEE -> message = "Votre établissement est agréé et validé par LADAFURA. Vous pouvez gérer vos produits, disponibilités, stocks et commandes.";
            case SUSPENDUE -> message = "Votre établissement a été temporairement suspendu par l'administration. Veuillez contacter le support de LADAFURA.";
            case REJETEE -> message = "Votre demande de référencement a été rejetée par l'administration. Veuillez vous rapprocher des administrateurs pour régulariser votre dossier.";
            default -> message = "Statut : " + p.getStatut();
        }

        return StatutReferencementResponse.builder()
                .pharmacopeeId(p.getId())
                .nomPharmacopee(p.getNom())
                .telephone(p.getTelephone())
                .statut(p.getStatut())
                .validee(validee)
                .message(message)
                .build();
    }
}
