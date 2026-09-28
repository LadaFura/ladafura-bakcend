package com.pharmacopee.ladafura.mappers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeDetailResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeLocalisationDto;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeModeRetraitDto;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeProduitItemResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;

@Component
public class PopulationPharmacopeeMapper {

    public PopulationPharmacopeeSummaryResponse toSummaryResponse(
            Pharmacopee pharmacopee,
            List<ModeRetrait> modesRetrait,
            long nombreProduits,
            Double noteMoyenne,
            long nombreAvis) {

        if (pharmacopee == null) {
            return null;
        }

        Localisation loc = pharmacopee.getLocalisation();
        boolean proposeLivraison = modesRetrait != null && modesRetrait.stream()
                .anyMatch(m -> m.getType() == TypeModeRetrait.LIVRAISON && Boolean.TRUE.equals(m.getActif()));
        boolean proposePickup = modesRetrait != null && modesRetrait.stream()
                .anyMatch(m -> m.getType() == TypeModeRetrait.PICKUP && Boolean.TRUE.equals(m.getActif()));

        return PopulationPharmacopeeSummaryResponse.builder()
                .id(pharmacopee.getId())
                .nom(pharmacopee.getNom())
                .description(pharmacopee.getDescription())
                .telephone(pharmacopee.getTelephone())
                .region(loc != null ? loc.getRegion() : null)
                .cercle(loc != null ? loc.getCercle() : null)
                .commune(loc != null ? loc.getCommune() : null)
                .localite(loc != null ? loc.getLocalite() : null)
                .latitude(loc != null ? loc.getLatitude() : null)
                .longitude(loc != null ? loc.getLongitude() : null)
                .proposeLivraison(proposeLivraison)
                .proposePickup(proposePickup)
                .nombreProduits(nombreProduits)
                .noteMoyenne(noteMoyenne)
                .nombreAvis(nombreAvis)
                .build();
    }

    public PopulationPharmacopeeDetailResponse toDetailResponse(
            Pharmacopee pharmacopee,
            List<ModeRetrait> modesRetrait,
            long nombreProduits,
            Double noteMoyenne,
            long nombreAvis) {

        if (pharmacopee == null) {
            return null;
        }

        List<PopulationPharmacopeeModeRetraitDto> modesDto = modesRetrait != null
                ? modesRetrait.stream().map(this::toModeRetraitDto).collect(Collectors.toList())
                : List.of();

        String email = pharmacopee.getUtilisateur() != null ? pharmacopee.getUtilisateur().getEmail() : null;

        return PopulationPharmacopeeDetailResponse.builder()
                .id(pharmacopee.getId())
                .nom(pharmacopee.getNom())
                .description(pharmacopee.getDescription())
                .telephone(pharmacopee.getTelephone())
                .email(email)
                .localisation(toLocalisationDto(pharmacopee.getLocalisation()))
                .modesRetrait(modesDto)
                .nombreProduits(nombreProduits)
                .noteMoyenne(noteMoyenne)
                .nombreAvis(nombreAvis)
                .build();
    }

    public PopulationPharmacopeeProduitItemResponse toProduitItemResponse(DisponibiliteProduit disp) {
        if (disp == null) {
            return null;
        }

        Produit p = disp.getProduit();
        Double prixEffectif = disp.getPrix() != null
                ? disp.getPrix()
                : (p != null ? p.getPrix() : null);

        return PopulationPharmacopeeProduitItemResponse.builder()
                .disponibiliteId(disp.getId())
                .produitId(p != null ? p.getId() : null)
                .nom(p != null ? p.getNom() : null)
                .description(p != null ? p.getDescription() : null)
                .forme(p != null ? p.getForme() : null)
                .prix(prixEffectif)
                .photoUrl(p != null ? p.getPhotoUrl() : null)
                .categorieId(p != null && p.getCategorie() != null ? p.getCategorie().getId() : null)
                .categorieNom(p != null && p.getCategorie() != null ? p.getCategorie().getNom() : null)
                .disponible(disp.getDisponible())
                .quantiteStock(disp.getQuantiteStock())
                .build();
    }

    public PopulationPharmacopeeModeRetraitDto toModeRetraitDto(ModeRetrait mode) {
        if (mode == null) {
            return null;
        }
        return PopulationPharmacopeeModeRetraitDto.builder()
                .id(mode.getId())
                .type(mode.getType() != null ? mode.getType().name() : null)
                .actif(mode.getActif())
                .frais(mode.getFrais())
                .build();
    }

    public PopulationPharmacopeeLocalisationDto toLocalisationDto(Localisation loc) {
        if (loc == null) {
            return null;
        }
        return PopulationPharmacopeeLocalisationDto.builder()
                .region(loc.getRegion())
                .cercle(loc.getCercle())
                .commune(loc.getCommune())
                .localite(loc.getLocalite())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .build();
    }
}
