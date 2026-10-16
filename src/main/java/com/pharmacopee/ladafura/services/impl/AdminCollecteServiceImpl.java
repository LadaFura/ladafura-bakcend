package com.pharmacopee.ladafura.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteDetailResponse;
import com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.admin.collecte.AdminModerateCollecteRequest;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AdminCollecteMapper;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminCollecteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminCollecteServiceImpl implements IAdminCollecteService {

    private final CollecteRepository collecteRepository;
    private final AdminCollecteMapper collecteMapper;
    private final com.pharmacopee.ladafura.repository.SourceRepository sourceRepository;
    private final com.pharmacopee.ladafura.repository.LocalisationRepository localisationRepository;
    private final com.pharmacopee.ladafura.repository.VertuDeLaPlanteRepository vertuDeLaPlanteRepository;
    private final com.pharmacopee.ladafura.repository.PlanteRepository planteRepository;
    private final com.pharmacopee.ladafura.repository.AgentCollecteRepository agentCollecteRepository;
    private final com.pharmacopee.ladafura.services.interfaces.IAgentNotificationService agentNotificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCollecteSummaryResponse> getAllCollectes(StatutCollecte statut, Pageable pageable) {
        log.info("Récupération paginée des collectes (statut={})", statut);
        Page<Collecte> page = (statut != null)
                ? collecteRepository.findByStatut(statut, pageable)
                : collecteRepository.findAll(pageable);

        return page.map(collecteMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCollecteDetailResponse getCollecteById(Long id) {
        log.info("Consultation de la collecte {}", id);
        Collecte collecte = collecteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", id));

        return collecteMapper.toDetailDto(collecte);
    }

    @Override
    public AdminCollecteDetailResponse moderateCollecte(Long id, AdminModerateCollecteRequest request) {
        log.info("Modération de la collecte {} avec action {}", id, request.getAction());
        Collecte collecte = collecteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", id));

        StatutCollecte ancienStatut = collecte.getStatut();
        collecte.setStatut(request.getAction());
        if (request.getMotifRejet() != null) {
            collecte.setMotifRejet(request.getMotifRejet());
        } else if (request.getAction() == StatutCollecte.VALIDEE) {
            collecte.setMotifRejet(null);
        }

        Collecte saved = collecteRepository.save(collecte);

        // Synchronisation du statut des connaissances traditionnelles associées
        java.util.List<com.pharmacopee.ladafura.Models.VertuDeLaPlante> vertus = vertuDeLaPlanteRepository.findByCollecteId(saved.getId());
        if (vertus != null && !vertus.isEmpty()) {
            for (com.pharmacopee.ladafura.Models.VertuDeLaPlante v : vertus) {
                if (request.getAction() == StatutCollecte.VALIDEE) {
                    v.setStatut(com.pharmacopee.ladafura.enums.StatutValidation.VALIDE);
                    vertuDeLaPlanteRepository.save(v);
                } else if (request.getAction() == StatutCollecte.REJETEE) {
                    v.setStatut(com.pharmacopee.ladafura.enums.StatutValidation.REJETE);
                    vertuDeLaPlanteRepository.save(v);
                }
            }
        }

        // Déclenchement automatique de la notification à l'agent de collecte
        if (agentNotificationService != null) {
            if (request.getAction() == StatutCollecte.VALIDEE) {
                agentNotificationService.notifierValidationCollecte(saved);
            } else if (request.getAction() == StatutCollecte.REJETEE) {
                agentNotificationService.notifierRejetCollecte(saved, request.getMotifRejet());
            } else if (request.getAction() == StatutCollecte.EN_EXAMEN && request.getMotifRejet() != null && !request.getMotifRejet().isBlank()) {
                agentNotificationService.notifierDemandeCorrection(saved, request.getMotifRejet());
            } else if (ancienStatut != request.getAction()) {
                agentNotificationService.notifierChangementStatut(saved, ancienStatut, request.getAction());
            }
        }

        return collecteMapper.toDetailDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteStatsResponse getStats() {
        log.info("Calcul des statistiques globales des collectes terrain");
        long total = collecteRepository.count();
        long soumises = collecteRepository.countByStatut(StatutCollecte.SOUMISE);
        long enExamen = collecteRepository.countByStatut(StatutCollecte.EN_EXAMEN);
        long validees = collecteRepository.countByStatut(StatutCollecte.VALIDEE);
        long rejetees = collecteRepository.countByStatut(StatutCollecte.REJETEE);

        return com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteStatsResponse.builder()
                .total(total)
                .soumises(soumises)
                .enExamen(enExamen)
                .validees(validees)
                .rejetees(rejetees)
                .build();
    }

    @Override
    public AdminCollecteDetailResponse updatePhoto(Long id, String photoUrl) {
        log.info("Mise à jour de la photo pour la collecte {} : {}", id, photoUrl);
        Collecte collecte = collecteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", id));
        collecte.setPhotoUrl(photoUrl);
        Collecte saved = collecteRepository.save(collecte);
        return collecteMapper.toDetailDto(saved);
    }

    @Override
    public AdminCollecteDetailResponse updateAudio(Long id, String audioUrl) {
        log.info("Mise à jour de l'enregistrement audio pour la collecte {} : {}", id, audioUrl);
        Collecte collecte = collecteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", id));
        collecte.setAudioUrl(audioUrl);
        Collecte saved = collecteRepository.save(collecte);
        return collecteMapper.toDetailDto(saved);
    }

    @Override
    public AdminCollecteDetailResponse updateCollecte(Long id, com.pharmacopee.ladafura.dto.admin.collecte.AdminUpdateCollecteRequest request) {
        log.info("Mise à jour complète de la collecte ID: {} par l'administrateur", id);
        Collecte collecte = collecteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", id));

        if (request.getDateCollecte() != null) {
            collecte.setDateCollecte(request.getDateCollecte());
        }
        if (request.getDescription() != null) {
            collecte.setDescription(request.getDescription());
        }
        if (request.getPhotoUrl() != null) {
            collecte.setPhotoUrl(request.getPhotoUrl().isBlank() ? null : request.getPhotoUrl().trim());
        }
        if (request.getAudioUrl() != null) {
            collecte.setAudioUrl(request.getAudioUrl().isBlank() ? null : request.getAudioUrl().trim());
        }

        // 1. Source (Tradipraticien / Informateur)
        if (request.getSourceId() != null) {
            com.pharmacopee.ladafura.Models.Source source = sourceRepository.findById(request.getSourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Source", "id", request.getSourceId()));
            collecte.setSource(source);
        } else if (request.getSourceNomComplet() != null && !request.getSourceNomComplet().isBlank()) {
            com.pharmacopee.ladafura.Models.Source source = collecte.getSource();
            String nomComplet = request.getSourceNomComplet().trim();
            String[] parts = nomComplet.split(" ", 2);
            String prenom = parts[0];
            String nom = parts.length > 1 ? parts[1] : "";

            if (source != null) {
                source.setPrenom(prenom);
                source.setNom(nom);
                if (request.getSourceSpecialite() != null) source.setSpecialite(request.getSourceSpecialite());
                if (request.getSourceTelephone() != null) source.setTelephone(request.getSourceTelephone());
                if (request.getSourceAdresse() != null) source.setAdresse(request.getSourceAdresse());
                sourceRepository.save(source);
            } else {
                source = new com.pharmacopee.ladafura.Models.Source();
                source.setPrenom(prenom);
                source.setNom(nom);
                source.setSpecialite(request.getSourceSpecialite() != null ? request.getSourceSpecialite() : "Tradipraticien");
                source.setTelephone(request.getSourceTelephone());
                source.setAdresse(request.getSourceAdresse());
                com.pharmacopee.ladafura.Models.Source savedSource = sourceRepository.save(source);
                collecte.setSource(savedSource);
            }
        }

        // 2. Localisation géographique
        if (request.getLocalisationId() != null) {
            com.pharmacopee.ladafura.Models.Localisation loc = localisationRepository.findById(request.getLocalisationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localisation", "id", request.getLocalisationId()));
            collecte.setLocalisation(loc);
        } else if (request.getRegion() != null || request.getCercle() != null || request.getCommune() != null || request.getLocalite() != null) {
            com.pharmacopee.ladafura.Models.Localisation loc = collecte.getLocalisation();
            if (loc != null) {
                if (request.getRegion() != null) loc.setRegion(request.getRegion());
                if (request.getCercle() != null) loc.setCercle(request.getCercle());
                if (request.getCommune() != null) loc.setCommune(request.getCommune());
                if (request.getLocalite() != null) loc.setLocalite(request.getLocalite());
                if (request.getLatitude() != null) loc.setLatitude(request.getLatitude());
                if (request.getLongitude() != null) loc.setLongitude(request.getLongitude());
                localisationRepository.save(loc);
            } else {
                loc = com.pharmacopee.ladafura.Models.Localisation.builder()
                        .region(request.getRegion() != null ? request.getRegion() : "Sikasso")
                        .cercle(request.getCercle() != null ? request.getCercle() : "")
                        .commune(request.getCommune() != null ? request.getCommune() : "")
                        .localite(request.getLocalite() != null ? request.getLocalite() : "")
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .build();
                com.pharmacopee.ladafura.Models.Localisation savedLoc = localisationRepository.save(loc);
                collecte.setLocalisation(savedLoc);
            }
        }

        // 3. Connaissances traditionnelles / Vertus
        if (request.getVertus() != null && !request.getVertus().isEmpty()) {
            for (com.pharmacopee.ladafura.dto.admin.collecte.AdminUpdateCollecteRequest.AdminVertuItemRequest vReq : request.getVertus()) {
                if (vReq.getId() != null) {
                    com.pharmacopee.ladafura.Models.VertuDeLaPlante vertu = vertuDeLaPlanteRepository.findById(vReq.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("VertuDeLaPlante", "id", vReq.getId()));
                    if (vReq.getUsageRapporte() != null) vertu.setUsageRapporte(vReq.getUsageRapporte());
                    if (vReq.getPartieUtilisee() != null) vertu.setPartieUtilisee(vReq.getPartieUtilisee());
                    if (vReq.getPreparation() != null) vertu.setPreparation(vReq.getPreparation());
                    if (vReq.getPrecaution() != null) vertu.setPrecaution(vReq.getPrecaution());
                    if (vReq.getDescription() != null) vertu.setDescription(vReq.getDescription());
                    if (vReq.getPlanteId() != null) {
                        com.pharmacopee.ladafura.Models.Plante p = planteRepository.findById(vReq.getPlanteId())
                                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", vReq.getPlanteId()));
                        vertu.setPlante(p);
                    }
                    vertuDeLaPlanteRepository.save(vertu);
                } else if (vReq.getPlanteId() != null) {
                    com.pharmacopee.ladafura.Models.Plante p = planteRepository.findById(vReq.getPlanteId())
                            .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", vReq.getPlanteId()));
                    com.pharmacopee.ladafura.Models.VertuDeLaPlante vertu = com.pharmacopee.ladafura.Models.VertuDeLaPlante.builder()
                            .collecte(collecte)
                            .plante(p)
                            .usageRapporte(vReq.getUsageRapporte())
                            .partieUtilisee(vReq.getPartieUtilisee())
                            .preparation(vReq.getPreparation())
                            .precaution(vReq.getPrecaution())
                            .description(vReq.getDescription())
                            .statut(com.pharmacopee.ladafura.enums.StatutValidation.VALIDE)
                            .build();
                    vertuDeLaPlanteRepository.save(vertu);
                }
            }
        }

        boolean agentNewlyAssigned = false;
        if (request.getAgentId() != null) {
            Long currentAgentId = collecte.getAgentCollecte() != null ? collecte.getAgentCollecte().getId() : null;
            if (!request.getAgentId().equals(currentAgentId)) {
                com.pharmacopee.ladafura.Models.AgentCollecte nouvelAgent = agentCollecteRepository.findById(request.getAgentId())
                        .orElseThrow(() -> new ResourceNotFoundException("AgentCollecte", "id", request.getAgentId()));
                collecte.setAgentCollecte(nouvelAgent);
                agentNewlyAssigned = true;
            }
        }

        Collecte saved = collecteRepository.save(collecte);
        if (agentNewlyAssigned && agentNotificationService != null) {
            agentNotificationService.notifierAttributionCollecte(saved);
        }
        return collecteMapper.toDetailDto(saved);
    }
}
