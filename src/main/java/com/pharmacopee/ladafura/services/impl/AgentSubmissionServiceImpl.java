package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.agent.submission.AgentCollecteRecapitulatifResponse;
import com.pharmacopee.ladafura.dto.agent.submission.AgentSubmissionResultResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutValidation;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.VertuDeLaPlanteRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;
import com.pharmacopee.ladafura.services.interfaces.IAgentSubmissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentSubmissionServiceImpl implements IAgentSubmissionService {

    private final CollecteRepository collecteRepository;
    private final VertuDeLaPlanteRepository vertuDeLaPlanteRepository;
    private final IAgentAuthService agentAuthService;

    @Override
    @Transactional(readOnly = true)
    public AgentCollecteRecapitulatifResponse getRecapitulatif(Long collecteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnership(collecte, currentAgent);

        List<VertuDeLaPlante> vertus = vertuDeLaPlanteRepository.findByCollecteId(collecteId);
        return buildRecapitulatif(collecte, vertus);
    }

    @Override
    @Transactional
    public AgentSubmissionResultResponse soumettreCollecteVerifiee(Long collecteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnership(collecte, currentAgent);

        if (collecte.getStatut() != StatutCollecte.BROUILLON && collecte.getStatut() != StatutCollecte.REJETEE) {
            log.warn("Tentative de soumission impossible pour la collecte ID {} (statut : {})", collecteId, collecte.getStatut());
            throw new BadRequestException("Impossible de soumettre cette collecte car son statut actuel est " + collecte.getStatut()
                    + ". Seules les collectes au statut BROUILLON ou REJETEE peuvent être soumises.");
        }

        List<VertuDeLaPlante> vertus = vertuDeLaPlanteRepository.findByCollecteId(collecteId);
        AgentCollecteRecapitulatifResponse recap = buildRecapitulatif(collecte, vertus);

        if (!recap.isConformePourSoumission()) {
            String errorMsg = String.join(" | ", recap.getErreursBloquantes());
            log.warn("Refus de soumission pour la collecte ID {} en raison d'erreurs bloquantes : {}", collecteId, errorMsg);
            throw new BadRequestException("Soumission impossible : le dossier de collecte est incomplet. " + errorMsg);
        }

        // Passage au statut SOUMISE (verrouillage immédiat)
        collecte.setStatut(StatutCollecte.SOUMISE);
        collecte.setDateSoumission(LocalDateTime.now());
        collecte.setMotifRejet(null);
        collecteRepository.save(collecte);

        // Passage des connaissances traditionnelles associées en statut EN_ATTENTE
        for (VertuDeLaPlante vertu : vertus) {
            vertu.setStatut(StatutValidation.EN_ATTENTE);
            vertuDeLaPlanteRepository.save(vertu);
        }

        log.info("Collecte ID {} soumise avec succès pour validation avec {} vertu(s) passée(s) en EN_ATTENTE",
                collecteId, vertus.size());

        return AgentSubmissionResultResponse.builder()
                .collecteId(collecte.getId())
                .statut(collecte.getStatut())
                .dateSoumission(collecte.getDateSoumission())
                .statutConnaissances(StatutValidation.EN_ATTENTE)
                .verrouillee(true)
                .message("Collecte soumise avec succès pour validation. Les données sont désormais verrouillées en attente d'examen administratif.")
                .build();
    }

    private void verifyCollecteOwnership(Collecte collecte, AgentCollecte currentAgent) {
        if (collecte.getAgentCollecte() == null || !collecte.getAgentCollecte().getId().equals(currentAgent.getId())) {
            throw new ForbiddenException("Accès refusé : cette collecte ne vous appartient pas.");
        }
    }

    private AgentCollecteRecapitulatifResponse buildRecapitulatif(Collecte collecte, List<VertuDeLaPlante> vertus) {
        AgentCollecte agent = collecte.getAgentCollecte();
        Source source = collecte.getSource();
        Localisation loc = collecte.getLocalisation();

        List<String> erreursBloquantes = new ArrayList<>();
        List<String> pointsDeVigilance = new ArrayList<>();

        // 1. Synthèse Plante & Botanique
        boolean planteAssociee = false;
        Long planteId = null;
        String planteNomScientifique = null;
        List<String> nomsVernaculaires = new ArrayList<>();

        if (!vertus.isEmpty() && vertus.get(0).getPlante() != null) {
            planteAssociee = true;
            Plante mainPlante = vertus.get(0).getPlante();
            planteId = mainPlante.getId();
            planteNomScientifique = mainPlante.getNomScientifique();

            if (mainPlante.getNomsPlante() != null && !mainPlante.getNomsPlante().isEmpty()) {
                nomsVernaculaires = mainPlante.getNomsPlante().stream()
                        .map(n -> n.getNom() + (n.getLangue() != null ? " (" + n.getLangue() + ")" : ""))
                        .toList();
            } else {
                pointsDeVigilance.add("Aucun nom vernaculaire local n'a encore été associé à cette plante.");
            }
        } else {
            erreursBloquantes.add("Aucune plante botanique n'a été associée à cette fiche de collecte.");
        }

        // 2. Synthèse Connaissances Traditionnelles
        boolean connaissanceRenseignee = false;
        List<String> usages = new ArrayList<>();
        List<String> parties = new ArrayList<>();
        List<String> modes = new ArrayList<>();
        List<String> precautions = new ArrayList<>();
        List<String> maladies = new ArrayList<>();

        for (VertuDeLaPlante v : vertus) {
            if (v.getUsageRapporte() != null && !v.getUsageRapporte().isBlank()) {
                connaissanceRenseignee = true;
                usages.add(v.getUsageRapporte());
            }
            if (v.getPartieUtilisee() != null && !v.getPartieUtilisee().isBlank()) parties.add(v.getPartieUtilisee());
            if (v.getPreparation() != null && !v.getPreparation().isBlank()) modes.add(v.getPreparation());
            if (v.getPrecaution() != null && !v.getPrecaution().isBlank()) precautions.add(v.getPrecaution());

            if (v.getPlante() != null && v.getPlante().getMaladies() != null) {
                v.getPlante().getMaladies().stream()
                        .map(Maladie::getNom)
                        .filter(m -> !maladies.contains(m))
                        .forEach(maladies::add);
            }
        }

        if (!connaissanceRenseignee) {
            erreursBloquantes.add("Aucune connaissance traditionnelle (usage rapporté sur le terrain) n'a été documentée.");
        }

        // 3. Synthèse Source (Thérapeute / Herboriste)
        boolean sourceRattachee = (source != null);
        if (!sourceRattachee) {
            pointsDeVigilance.add("Aucune source d'information de terrain (thérapeute ou herboriste) n'a été spécifiée.");
        }

        // 4. Synthèse Médias
        boolean photoPresente = (collecte.getPhotoUrl() != null && !collecte.getPhotoUrl().isBlank());
        boolean audioPresent = (collecte.getAudioUrl() != null && !collecte.getAudioUrl().isBlank());
        if (!photoPresente) {
            pointsDeVigilance.add("Aucune photo d'échantillon ou de plante n'a été jointe.");
        }
        if (!audioPresent) {
            pointsDeVigilance.add("Aucun enregistrement audio de témoignage n'a été joint.");
        }

        // 5. Synthèse Localisation
        boolean localisationRenseignee = (loc != null);
        String adresseFormatee = null;
        if (loc != null) {
            adresseFormatee = String.format("%s, %s, %s, %s",
                    loc.getLocalite(), loc.getCommune(), loc.getCercle(), loc.getRegion());
            if (loc.getLatitude() == null || loc.getLongitude() == null) {
                pointsDeVigilance.add("Les coordonnées GPS précises n'ont pas été renseignées.");
            }
        } else {
            erreursBloquantes.add("La localisation géographique du recueil de terrain n'a pas été renseignée.");
        }

        boolean conforme = erreursBloquantes.isEmpty();
        String prochaineEtape = conforme ? "SOUMISSION_AUTORISEE" : "COMPLETER_DOSSIER";

        return AgentCollecteRecapitulatifResponse.builder()
                .collecteId(collecte.getId())
                .dateCollecte(collecte.getDateCollecte())
                .description(collecte.getDescription())
                .statut(collecte.getStatut())
                .agentNomComplet(agent != null ? agent.getPrenom() + " " + agent.getNom() : null)
                .planteAssociee(planteAssociee)
                .planteId(planteId)
                .planteNomScientifique(planteNomScientifique)
                .nomsVernaculaires(nomsVernaculaires)
                .connaissanceRenseignee(connaissanceRenseignee)
                .nombreConnaissances(vertus.size())
                .usagesRapportes(usages)
                .partiesUtilisees(parties)
                .modesPreparation(modes)
                .precautions(precautions)
                .maladiesAssociees(maladies)
                .sourceRattachee(sourceRattachee)
                .sourceId(source != null ? source.getId() : null)
                .sourceNomComplet(source != null ? source.getPrenom() + " " + source.getNom() : null)
                .sourceSpecialite(source != null ? source.getSpecialite() : null)
                .mediasPresents(photoPresente || audioPresent)
                .photoUrl(collecte.getPhotoUrl())
                .audioUrl(collecte.getAudioUrl())
                .photoPresente(photoPresente)
                .audioPresent(audioPresent)
                .localisationRenseignee(localisationRenseignee)
                .region(loc != null ? loc.getRegion() : null)
                .cercle(loc != null ? loc.getCercle() : null)
                .commune(loc != null ? loc.getCommune() : null)
                .localite(loc != null ? loc.getLocalite() : null)
                .latitude(loc != null ? loc.getLatitude() : null)
                .longitude(loc != null ? loc.getLongitude() : null)
                .adresseFormatee(adresseFormatee)
                .conformePourSoumission(conforme)
                .erreursBloquantes(erreursBloquantes)
                .pointsDeVigilance(pointsDeVigilance)
                .prochaineEtape(prochaineEtape)
                .build();
    }
}
