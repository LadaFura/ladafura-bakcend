package com.pharmacopee.ladafura.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Praticien;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.referencement.DemandeReferencementRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.referencement.StatutReferencementResponse;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.exceptions.QuotaExceededException;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PraticienRepository;
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
    private final PraticienRepository praticienRepository;

    @Override
    public StatutReferencementResponse soumettreDemandeReferencement(DemandeReferencementRequest request) {
        Utilisateur user = pharmacopeeAuthService.getCurrentUtilisateur();
        List<Pharmacopee> existantes = pharmacopeeAuthService.getMyPharmacopees();

        Optional<Praticien> praticienOpt = praticienRepository.findById(user.getId());
        String plan = praticienOpt.map(Praticien::getPlanAbonnement).orElse("GRATUIT");
        int quotaMax = praticienOpt.map(p -> p.getQuotaMaxStructures() != null ? p.getQuotaMaxStructures() : 1).orElse(1);

        if (existantes.size() >= quotaMax) {
            log.warn("Quota de structures atteint pour l'utilisateur ID {}. Actuelles: {}, Quota: {}, Plan: {}",
                    user.getId(), existantes.size(), quotaMax, plan);
            throw new QuotaExceededException("Vous avez atteint la limite de " + quotaMax
                    + " structure(s) pour votre formule actuelle (" + plan
                    + "). Veuillez souscrire à un abonnement supérieur pour ajouter d'autres structures.");
        }

        Pharmacopee pharmacopee = Pharmacopee.builder()
                .nom(request.getNom().trim())
                .description(request.getDescription())
                .telephone(request.getTelephone().trim())
                .statut(StatutPharmacopee.EN_ATTENTE)
                .utilisateur(user)
                .build();

        Pharmacopee saved = pharmacopeeRepository.save(pharmacopee);

        if (praticienOpt.isPresent()) {
            Praticien praticien = praticienOpt.get();
            praticien.getPharmacopees().add(saved);
            praticienRepository.save(praticien);
        }

        log.info("Nouvelle demande d'établissement (EN_ATTENTE) enregistrée pour l'utilisateur ID: {} (Pharmacopée ID: {})",
                user.getId(), saved.getId());

        return buildResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StatutReferencementResponse consulterStatutReferencement() {
        List<Pharmacopee> existantes = pharmacopeeAuthService.getMyPharmacopees();

        if (existantes.isEmpty()) {
            return StatutReferencementResponse.builder()
                    .pharmacopeeId(null)
                    .nomPharmacopee(null)
                    .telephone(null)
                    .statut(null)
                    .validee(false)
                    .message("Aucun dossier de référencement n'a encore été soumis pour votre compte. Veuillez soumettre une demande.")
                    .build();
        }

        Pharmacopee active;
        try {
            active = pharmacopeeAuthService.getCurrentPharmacopee();
        } catch (Exception e) {
            active = existantes.get(existantes.size() - 1);
        }

        return buildResponse(active);
    }

    private StatutReferencementResponse buildResponse(Pharmacopee p) {
        String message;
        boolean validee = (p.getStatut() == StatutPharmacopee.VALIDEE);

        switch (p.getStatut()) {
            case EN_ATTENTE -> message = "Votre demande de référencement est en cours d'étude par l'administration de LADAFURA. Vos fonctionnalités commerciales seront activées dès validation.";
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
