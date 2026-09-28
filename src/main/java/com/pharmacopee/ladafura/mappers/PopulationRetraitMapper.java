package com.pharmacopee.ladafura.mappers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationEstimationRetraitResponse;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationModeRetraitOptionDto;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationPharmacopeeRetraitOptionsResponse;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;

@Component
public class PopulationRetraitMapper {

    public String formatAdresse(Localisation loc) {
        if (loc == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (loc.getLocalite() != null && !loc.getLocalite().isBlank()) {
            sb.append(loc.getLocalite());
        }
        if (loc.getCommune() != null && !loc.getCommune().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(loc.getCommune());
        }
        if (loc.getCercle() != null && !loc.getCercle().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(loc.getCercle());
        }
        if (loc.getRegion() != null && !loc.getRegion().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(loc.getRegion());
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    public PopulationModeRetraitOptionDto toOptionDto(ModeRetrait mr, Pharmacopee ph) {
        if (mr == null) {
            return null;
        }

        boolean isPickup = mr.getType() == TypeModeRetrait.PICKUP;
        String adresseRetrait = (isPickup && ph != null) ? formatAdresse(ph.getLocalisation()) : null;
        double frais = isPickup ? 0.0 : (mr.getFrais() != null ? mr.getFrais() : 0.0);

        String libelle = isPickup ? "Retrait en officine (Pickup)" : "Livraison à domicile";
        String description = isPickup
                ? "Retrait gratuit au comptoir de l'officine sur présentation de la référence de commande."
                : "Livraison directe à votre domicile ou adresse indiquée lors de la commande.";

        return PopulationModeRetraitOptionDto.builder()
                .id(mr.getId())
                .type(mr.getType())
                .libelle(libelle)
                .actif(Boolean.TRUE.equals(mr.getActif()))
                .frais(frais)
                .gratuit(isPickup || frais == 0.0)
                .description(description)
                .adresseRetrait(adresseRetrait)
                .build();
    }

    public PopulationPharmacopeeRetraitOptionsResponse toPharmacopeeOptionsResponse(
            Pharmacopee ph, List<ModeRetrait> modes) {
        if (ph == null) {
            return null;
        }

        List<PopulationModeRetraitOptionDto> options = new ArrayList<>();
        boolean proposeLivraison = false;
        boolean proposePickup = false;
        Double fraisLivraison = null;

        if (modes != null) {
            for (ModeRetrait mr : modes) {
                PopulationModeRetraitOptionDto opt = toOptionDto(mr, ph);
                if (opt != null) {
                    options.add(opt);
                    if (Boolean.TRUE.equals(mr.getActif())) {
                        if (mr.getType() == TypeModeRetrait.LIVRAISON) {
                            proposeLivraison = true;
                            fraisLivraison = mr.getFrais();
                        } else if (mr.getType() == TypeModeRetrait.PICKUP) {
                            proposePickup = true;
                        }
                    }
                }
            }
        }

        return PopulationPharmacopeeRetraitOptionsResponse.builder()
                .pharmacopeeId(ph.getId())
                .nomPharmacopee(ph.getNom())
                .telephonePharmacopee(ph.getTelephone())
                .adressePharmacopee(formatAdresse(ph.getLocalisation()))
                .proposeLivraison(proposeLivraison)
                .fraisLivraison(fraisLivraison)
                .proposePickup(proposePickup)
                .options(options)
                .build();
    }

    public PopulationEstimationRetraitResponse toEstimationResponse(
            Pharmacopee ph, ModeRetrait mr, TypeModeRetrait typeDemande) {
        String nomPh = ph != null ? ph.getNom() : null;
        Long phId = ph != null ? ph.getId() : null;
        String adresseRetrait = (ph != null) ? formatAdresse(ph.getLocalisation()) : null;

        if (mr == null || !Boolean.TRUE.equals(mr.getActif())) {
            String nomMode = typeDemande == TypeModeRetrait.LIVRAISON ? "La livraison à domicile" : "Le retrait en officine";
            return PopulationEstimationRetraitResponse.builder()
                    .pharmacopeeId(phId)
                    .nomPharmacopee(nomPh)
                    .modeRetraitId(null)
                    .type(typeDemande)
                    .libelle(typeDemande == TypeModeRetrait.LIVRAISON ? "Livraison à domicile" : "Retrait en officine (Pickup)")
                    .eligible(false)
                    .frais(0.0)
                    .gratuit(false)
                    .adresseRetrait(typeDemande == TypeModeRetrait.PICKUP ? adresseRetrait : null)
                    .message(nomMode + " n'est actuellement pas proposée par cette pharmacopée.")
                    .build();
        }

        boolean isPickup = typeDemande == TypeModeRetrait.PICKUP;
        double frais = isPickup ? 0.0 : (mr.getFrais() != null ? mr.getFrais() : 0.0);
        String libelle = isPickup ? "Retrait en officine (Pickup)" : "Livraison à domicile";
        String message = isPickup
                ? "Retrait en officine disponible et gratuit au comptoir de " + nomPh + "."
                : "Livraison disponible au tarif de " + frais + " FCFA.";

        return PopulationEstimationRetraitResponse.builder()
                .pharmacopeeId(phId)
                .nomPharmacopee(nomPh)
                .modeRetraitId(mr.getId())
                .type(mr.getType())
                .libelle(libelle)
                .eligible(true)
                .frais(frais)
                .gratuit(isPickup || frais == 0.0)
                .adresseRetrait(isPickup ? adresseRetrait : null)
                .message(message)
                .build();
    }
}
