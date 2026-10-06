package com.pharmacopee.ladafura.mappers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCarteDetailPharmacopeeResponse;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCartePharmacopeeItem;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCarteProduitItem;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeLocalisationDto;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeModeRetraitDto;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;

@Component
public class PopulationCarteMapper {

    /**
     * Calcul de la distance géodésique en kilomètres entre deux coordonnées GPS (formule de Haversine).
     */
    public Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }
        final int R = 6371; // Rayon de la Terre en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return Math.round(distance * 100.0) / 100.0; // arrondi à 2 chiffres après la virgule
    }

    public PopulationCartePharmacopeeItem toCartePharmacopeeItem(
            Pharmacopee pharmacopee,
            List<ModeRetrait> modesRetrait,
            long nombreProduits,
            Double noteMoyenne,
            long nombreAvis,
            Double distanceKm) {

        if (pharmacopee == null) {
            return null;
        }

        Localisation loc = pharmacopee.getLocalisation();
        boolean proposeLivraison = modesRetrait != null && modesRetrait.stream()
                .anyMatch(m -> m.getType() == TypeModeRetrait.LIVRAISON && Boolean.TRUE.equals(m.getActif()));
        boolean proposePickup = modesRetrait != null && modesRetrait.stream()
                .anyMatch(m -> m.getType() == TypeModeRetrait.PICKUP && Boolean.TRUE.equals(m.getActif()));

        return PopulationCartePharmacopeeItem.builder()
                .pharmacopeeId(pharmacopee.getId())
                .nom(pharmacopee.getNom())
                .description(pharmacopee.getDescription())
                .telephone(pharmacopee.getTelephone())
                .region(loc != null ? loc.getRegion() : null)
                .cercle(loc != null ? loc.getCercle() : null)
                .commune(loc != null ? loc.getCommune() : null)
                .localite(loc != null ? loc.getLocalite() : null)
                .latitude(loc != null ? loc.getLatitude() : null)
                .longitude(loc != null ? loc.getLongitude() : null)
                .distanceKm(distanceKm)
                .photoUrl(pharmacopee.getPhotoUrl())
                .proposeLivraison(proposeLivraison)
                .proposePickup(proposePickup)
                .nombreProduits(nombreProduits)
                .noteMoyenne(noteMoyenne)
                .nombreAvis(nombreAvis)
                .build();
    }

    public PopulationCarteProduitItem toCarteProduitItem(
            DisponibiliteProduit disp,
            List<ModeRetrait> modesRetrait,
            Double distanceKm) {

        if (disp == null) {
            return null;
        }

        Pharmacopee ph = disp.getPharmacopee();
        Localisation loc = ph != null ? ph.getLocalisation() : null;
        Produit p = disp.getProduit();

        List<PopulationPharmacopeeModeRetraitDto> modesDto = modesRetrait != null
                ? modesRetrait.stream().map(this::toModeRetraitDto).collect(Collectors.toList())
                : List.of();

        Double prixEffectif = disp.getPrix() != null
                ? disp.getPrix()
                : (p != null ? p.getPrix() : null);

        return PopulationCarteProduitItem.builder()
                .pharmacopeeId(ph != null ? ph.getId() : null)
                .nomPharmacopee(ph != null ? ph.getNom() : null)
                .telephone(ph != null ? ph.getTelephone() : null)
                .region(loc != null ? loc.getRegion() : null)
                .cercle(loc != null ? loc.getCercle() : null)
                .commune(loc != null ? loc.getCommune() : null)
                .localite(loc != null ? loc.getLocalite() : null)
                .latitude(loc != null ? loc.getLatitude() : null)
                .longitude(loc != null ? loc.getLongitude() : null)
                .distanceKm(distanceKm)
                .produitId(p != null ? p.getId() : null)
                .nomProduit(p != null ? p.getNom() : null)
                .forme(p != null ? p.getForme() : null)
                .prix(prixEffectif)
                .disponible(disp.getDisponible())
                .quantiteStock(disp.getQuantiteStock())
                .modesRetrait(modesDto)
                .build();
    }

    public PopulationCarteDetailPharmacopeeResponse toCarteDetailResponse(
            Pharmacopee pharmacopee,
            List<ModeRetrait> modesRetrait,
            long nombreProduits,
            Double noteMoyenne,
            long nombreAvis,
            Double distanceKm) {

        if (pharmacopee == null) {
            return null;
        }

        List<PopulationPharmacopeeModeRetraitDto> modesDto = modesRetrait != null
                ? modesRetrait.stream().map(this::toModeRetraitDto).collect(Collectors.toList())
                : List.of();

        String email = pharmacopee.getUtilisateur() != null ? pharmacopee.getUtilisateur().getEmail() : null;

        return PopulationCarteDetailPharmacopeeResponse.builder()
                .pharmacopeeId(pharmacopee.getId())
                .nom(pharmacopee.getNom())
                .description(pharmacopee.getDescription())
                .telephone(pharmacopee.getTelephone())
                .email(email)
                .photoUrl(pharmacopee.getPhotoUrl())
                .localisation(toLocalisationDto(pharmacopee.getLocalisation()))
                .distanceKm(distanceKm)
                .modesRetrait(modesDto)
                .nombreProduits(nombreProduits)
                .noteMoyenne(noteMoyenne)
                .nombreAvis(nombreAvis)
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
