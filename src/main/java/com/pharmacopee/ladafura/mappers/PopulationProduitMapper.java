package com.pharmacopee.ladafura.mappers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.CompositionProduit;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.produit.PopulationCompositionItemDto;
import com.pharmacopee.ladafura.dto.population.produit.PopulationModeRetraitDto;
import com.pharmacopee.ladafura.dto.population.produit.PopulationOffrePharmacopeeDto;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitDetailResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitMaladieDto;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;

@Component
public class PopulationProduitMapper {

    public PopulationProduitSummaryResponse toSummaryResponse(
            Produit produit,
            List<DisponibiliteProduit> disponibilites,
            Double noteMoyenne,
            long nombreAvis) {

        if (produit == null) {
            return null;
        }

        List<String> plantes = produit.getCompositions() != null
                ? produit.getCompositions().stream()
                        .filter(c -> c.getPlante() != null)
                        .map(c -> c.getPlante().getNomScientifique())
                        .distinct()
                        .collect(Collectors.toList())
                : List.of();

        int nbPharmacopees = disponibilites != null ? disponibilites.size() : 0;
        boolean disponibleEnPharmacie = disponibilites != null && disponibilites.stream()
                .anyMatch(d -> Boolean.TRUE.equals(d.getDisponible()) && d.getQuantiteStock() != null && d.getQuantiteStock() > 0);

        return PopulationProduitSummaryResponse.builder()
                .id(produit.getId())
                .nom(produit.getNom())
                .description(produit.getDescription())
                .forme(produit.getForme())
                .prixIndicatif(produit.getPrix())
                .photoUrl(produit.getPhotoUrl())
                .categorieId(produit.getCategorie() != null ? produit.getCategorie().getId() : null)
                .categorieNom(produit.getCategorie() != null ? produit.getCategorie().getNom() : null)
                .plantesPrincipales(plantes)
                .nombrePharmacopees(nbPharmacopees)
                .disponibleEnPharmacie(disponibleEnPharmacie)
                .noteMoyenne(noteMoyenne)
                .nombreAvis(nombreAvis)
                .build();
    }

    public PopulationProduitDetailResponse toDetailResponse(
            Produit produit,
            List<CompositionProduit> compositions,
            List<DisponibiliteProduit> disponibilites,
            Map<Long, List<ModeRetrait>> modesRetraitByPhId,
            Double noteMoyenne,
            long nombreAvis) {

        if (produit == null) {
            return null;
        }

        // 1. Ingrédients végétaux
        List<PopulationCompositionItemDto> compDtos = new ArrayList<>();
        Set<Long> maladieIds = new HashSet<>();
        List<PopulationProduitMaladieDto> maladieDtos = new ArrayList<>();

        if (compositions != null) {
            for (CompositionProduit comp : compositions) {
                Plante plante = comp.getPlante();
                List<String> nomsVernaculaires = List.of();
                if (plante != null) {
                    if (plante.getNomsPlante() != null) {
                        nomsVernaculaires = plante.getNomsPlante().stream()
                                .map(NomPlante::getNom)
                                .collect(Collectors.toList());
                    }
                    if (plante.getMaladies() != null) {
                        for (Maladie m : plante.getMaladies()) {
                            if (m != null && maladieIds.add(m.getId())) {
                                maladieDtos.add(PopulationProduitMaladieDto.builder()
                                        .id(m.getId())
                                        .nom(m.getNom())
                                        .description(m.getDescription())
                                        .build());
                            }
                        }
                    }
                }

                compDtos.add(PopulationCompositionItemDto.builder()
                        .planteId(plante != null ? plante.getId() : null)
                        .nomScientifique(plante != null ? plante.getNomScientifique() : null)
                        .nomsVernaculaires(nomsVernaculaires)
                        .quantite(comp.getQuantite())
                        .unite(comp.getUnite())
                        .photoUrl(plante != null ? plante.getPhotoUrl() : null)
                        .build());
            }
        }

        // 2. Offres des officines de pharmacopées
        List<PopulationOffrePharmacopeeDto> offresDtos = new ArrayList<>();
        if (disponibilites != null) {
            for (DisponibiliteProduit disp : disponibilites) {
                Pharmacopee ph = disp.getPharmacopee();
                List<ModeRetrait> modes = ph != null && modesRetraitByPhId != null
                        ? modesRetraitByPhId.getOrDefault(ph.getId(), List.of())
                        : List.of();
                offresDtos.add(toOffreDto(disp, modes));
            }
        }

        return PopulationProduitDetailResponse.builder()
                .id(produit.getId())
                .nom(produit.getNom())
                .description(produit.getDescription())
                .forme(produit.getForme())
                .compositionTexte(produit.getComposition())
                .prixIndicatif(produit.getPrix())
                .photoUrl(produit.getPhotoUrl())
                .categorieId(produit.getCategorie() != null ? produit.getCategorie().getId() : null)
                .categorieNom(produit.getCategorie() != null ? produit.getCategorie().getNom() : null)
                .noteMoyenne(noteMoyenne)
                .nombreAvis(nombreAvis)
                .compositions(compDtos)
                .maladies(maladieDtos)
                .offresPharmacopees(offresDtos)
                .build();
    }

    public PopulationOffrePharmacopeeDto toOffreDto(DisponibiliteProduit disp, List<ModeRetrait> modesRetrait) {
        if (disp == null) {
            return null;
        }

        Pharmacopee ph = disp.getPharmacopee();
        Localisation loc = ph != null ? ph.getLocalisation() : null;

        List<PopulationModeRetraitDto> modesDto = modesRetrait != null
                ? modesRetrait.stream().map(this::toModeRetraitDto).collect(Collectors.toList())
                : List.of();

        Double prixEffectif = disp.getPrix() != null
                ? disp.getPrix()
                : (disp.getProduit() != null ? disp.getProduit().getPrix() : null);

        return PopulationOffrePharmacopeeDto.builder()
                .disponibiliteId(disp.getId())
                .pharmacopeeId(ph != null ? ph.getId() : null)
                .nomPharmacopee(ph != null ? ph.getNom() : null)
                .telephone(ph != null ? ph.getTelephone() : null)
                .region(loc != null ? loc.getRegion() : null)
                .cercle(loc != null ? loc.getCercle() : null)
                .commune(loc != null ? loc.getCommune() : null)
                .localite(loc != null ? loc.getLocalite() : null)
                .latitude(loc != null ? loc.getLatitude() : null)
                .longitude(loc != null ? loc.getLongitude() : null)
                .disponible(disp.getDisponible())
                .quantiteStock(disp.getQuantiteStock())
                .prix(prixEffectif)
                .modesRetrait(modesDto)
                .build();
    }

    public PopulationModeRetraitDto toModeRetraitDto(ModeRetrait mode) {
        if (mode == null) {
            return null;
        }
        return PopulationModeRetraitDto.builder()
                .id(mode.getId())
                .type(mode.getType() != null ? mode.getType().name() : null)
                .actif(mode.getActif())
                .frais(mode.getFrais())
                .build();
    }
}
