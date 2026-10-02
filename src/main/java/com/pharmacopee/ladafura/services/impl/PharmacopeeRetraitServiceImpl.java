package com.pharmacopee.ladafura.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.pharmacopee.retrait.ConfigureModesRetraitGlobalRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.retrait.ModeRetraitResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.retrait.PharmacopeeModesRetraitSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.retrait.UpdateModeRetraitRequest;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeRetraitService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacopeeRetraitServiceImpl implements IPharmacopeeRetraitService {

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final ModeRetraitRepository modeRetraitRepository;

    @Override
    @Transactional(readOnly = true)
    public PharmacopeeModesRetraitSummaryResponse getModesRetrait() {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        log.info("Consultation des modes de retrait pour la pharmacopée ID {}", pharmacopee.getId());

        ModeRetrait livraison = getOrInitModeRetrait(pharmacopee, TypeModeRetrait.LIVRAISON);
        ModeRetrait pickup = getOrInitModeRetrait(pharmacopee, TypeModeRetrait.PICKUP);

        return buildSummary(pharmacopee, livraison, pickup);
    }

    @Override
    @Transactional(readOnly = true)
    public ModeRetraitResponse getModeRetrait(TypeModeRetrait type) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        log.info("Consultation du mode {} pour la pharmacopée ID {}", type, pharmacopee.getId());

        ModeRetrait mode = getOrInitModeRetrait(pharmacopee, type);
        return mapToResponse(mode);
    }

    @Override
    public ModeRetraitResponse updateModeRetrait(TypeModeRetrait type, UpdateModeRetraitRequest request) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        log.info("Mise à jour du mode {} pour la pharmacopée ID {} : actif={}, frais={}",
                type, pharmacopee.getId(), request.getActif(), request.getFrais());

        ModeRetrait mode = getOrInitModeRetrait(pharmacopee, type);
        mode.setActif(request.getActif());

        if (request.getFrais() != null) {
            mode.setFrais(request.getFrais());
        }

        ModeRetrait saved = modeRetraitRepository.save(mode);
        return mapToResponse(saved);
    }

    @Override
    public ModeRetraitResponse toggleModeRetrait(TypeModeRetrait type) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        log.info("Basculement on/off du mode {} pour la pharmacopée ID {}", type, pharmacopee.getId());

        ModeRetrait mode = getOrInitModeRetrait(pharmacopee, type);
        boolean nouvelEtat = !Boolean.TRUE.equals(mode.getActif());
        mode.setActif(nouvelEtat);

        ModeRetrait saved = modeRetraitRepository.save(mode);
        log.info("Nouveau statut pour {} de pharmacopée ID {} : actif={}", type, pharmacopee.getId(), nouvelEtat);
        return mapToResponse(saved);
    }

    @Override
    public PharmacopeeModesRetraitSummaryResponse configureModesRetraitGlobal(ConfigureModesRetraitGlobalRequest request) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        log.info("Configuration globale des modes de retrait pour la pharmacopée ID {} : livraisonActif={}, livraisonFrais={}, pickupActif={}, pickupFrais={}",
                pharmacopee.getId(), request.getLivraisonActif(), request.getLivraisonFrais(), request.getPickupActif(), request.getPickupFrais());

        // 1. Mise à jour ou création du mode LIVRAISON
        ModeRetrait livraison = getOrInitModeRetrait(pharmacopee, TypeModeRetrait.LIVRAISON);
        livraison.setActif(request.getLivraisonActif());
        if (request.getLivraisonFrais() != null) {
            livraison.setFrais(request.getLivraisonFrais());
        }
        livraison = modeRetraitRepository.save(livraison);

        // 2. Mise à jour ou création du mode PICKUP
        ModeRetrait pickup = getOrInitModeRetrait(pharmacopee, TypeModeRetrait.PICKUP);
        pickup.setActif(request.getPickupActif());
        if (request.getPickupFrais() != null) {
            pickup.setFrais(request.getPickupFrais());
        }
        pickup = modeRetraitRepository.save(pickup);

        return buildSummary(pharmacopee, livraison, pickup);
    }

    /**
     * Recherche le mode de retrait existant ou l'initialise avec des valeurs par défaut cohérentes.
     * Respecte la contrainte Pharmacopée 1 --- 0..2 ModeRetrait.
     */
    private ModeRetrait getOrInitModeRetrait(Pharmacopee pharmacopee, TypeModeRetrait type) {
        return modeRetraitRepository.findByPharmacopeeIdAndType(pharmacopee.getId(), type)
                .orElseGet(() -> {
                    log.info("Initialisation par défaut du mode {} pour la pharmacopée ID {}", type, pharmacopee.getId());
                    boolean defaultActif = (type == TypeModeRetrait.PICKUP); // Pickup actif par défaut, Livraison inactif par défaut
                    double defaultFrais = 0.0;

                    ModeRetrait nouveauMode = ModeRetrait.builder()
                            .pharmacopee(pharmacopee)
                            .type(type)
                            .actif(defaultActif)
                            .frais(defaultFrais)
                            .build();

                    return modeRetraitRepository.save(nouveauMode);
                });
    }

    private PharmacopeeModesRetraitSummaryResponse buildSummary(Pharmacopee pharmacopee, ModeRetrait livraison, ModeRetrait pickup) {
        ModeRetraitResponse livraisonResp = mapToResponse(livraison);
        ModeRetraitResponse pickupResp = mapToResponse(pickup);

        boolean auMoinsUn = Boolean.TRUE.equals(livraisonResp.getActif()) || Boolean.TRUE.equals(pickupResp.getActif());
        boolean lesDeux = Boolean.TRUE.equals(livraisonResp.getActif()) && Boolean.TRUE.equals(pickupResp.getActif());

        return PharmacopeeModesRetraitSummaryResponse.builder()
                .pharmacopeeId(pharmacopee.getId())
                .nomPharmacopee(pharmacopee.getNom())
                .livraison(livraisonResp)
                .pickup(pickupResp)
                .auMoinsUnModeActif(auMoinsUn)
                .tousLesDeuxActifs(lesDeux)
                .build();
    }

    private ModeRetraitResponse mapToResponse(ModeRetrait mode) {
        String libelle = mode.getType() == TypeModeRetrait.LIVRAISON
                ? "Livraison à domicile"
                : "Retrait en pharmacopée (Pickup)";

        return ModeRetraitResponse.builder()
                .id(mode.getId())
                .type(mode.getType())
                .libelle(libelle)
                .actif(Boolean.TRUE.equals(mode.getActif()))
                .frais(mode.getFrais() != null ? mode.getFrais() : 0.0)
                .build();
    }
}
