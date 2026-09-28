package com.pharmacopee.ladafura.mappers;

import java.util.List;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Favori;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriCheckResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriCountResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriItemResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutValidation;
import com.pharmacopee.ladafura.enums.TypeFavori;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.EtudeScientifiqueRepository;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.VertuDeLaPlanteRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PopulationFavoriMapper {

    private final PopulationPlanteMapper planteMapper;
    private final PopulationProduitMapper produitMapper;
    private final PopulationPharmacopeeMapper pharmacopeeMapper;

    private final VertuDeLaPlanteRepository vertuDeLaPlanteRepository;
    private final EtudeScientifiqueRepository etudeScientifiqueRepository;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final AvisRepository avisRepository;
    private final ModeRetraitRepository modeRetraitRepository;

    public PopulationFavoriItemResponse toItemResponse(Favori favori) {
        if (favori == null) {
            return null;
        }

        TypeFavori type = null;
        PopulationPlanteSummaryResponse planteSummary = null;
        PopulationProduitSummaryResponse produitSummary = null;
        PopulationPharmacopeeSummaryResponse phSummary = null;

        if (favori.getPlante() != null) {
            type = TypeFavori.PLANTE;
            planteSummary = mapPlanteSummary(favori.getPlante());
        } else if (favori.getProduit() != null) {
            type = TypeFavori.PRODUIT;
            produitSummary = mapProduitSummary(favori.getProduit());
        } else if (favori.getPharmacopee() != null) {
            type = TypeFavori.PHARMACOPEE;
            phSummary = mapPharmacopeeSummary(favori.getPharmacopee());
        }

        return PopulationFavoriItemResponse.builder()
                .id(favori.getId())
                .type(type)
                .dateAjout(favori.getDateAjout())
                .plante(planteSummary)
                .produit(produitSummary)
                .pharmacopee(phSummary)
                .build();
    }

    public PopulationPlanteSummaryResponse mapPlanteSummary(Plante plante) {
        if (plante == null) {
            return null;
        }
        int nbConnaissances = vertuDeLaPlanteRepository.findByPlanteIdAndStatut(plante.getId(), StatutValidation.VALIDE).size();
        int nbEtudes = etudeScientifiqueRepository.findByPlanteId(plante.getId()).size();
        return planteMapper.toSummaryResponse(plante, nbConnaissances, nbEtudes);
    }

    public PopulationProduitSummaryResponse mapProduitSummary(Produit produit) {
        if (produit == null) {
            return null;
        }
        List<DisponibiliteProduit> disponibilites = disponibiliteProduitRepository.findOffresValideesByProduitId(produit.getId());
        Double noteMoyenne = avisRepository.findAverageNoteByProduitIdAndStatut(produit.getId(), StatutAvis.PUBLIE);
        long nombreAvis = avisRepository.countByProduitIdAndStatut(produit.getId(), StatutAvis.PUBLIE);
        return produitMapper.toSummaryResponse(produit, disponibilites, noteMoyenne, nombreAvis);
    }

    public PopulationPharmacopeeSummaryResponse mapPharmacopeeSummary(Pharmacopee ph) {
        if (ph == null) {
            return null;
        }
        List<ModeRetrait> modes = modeRetraitRepository.findByPharmacopeeId(ph.getId());
        long nbProduits = disponibiliteProduitRepository.countProduitsValidesByPharmacopeeId(ph.getId());
        Double noteMoyenne = avisRepository.findAverageNoteByPharmacopeeIdAndStatut(ph.getId(), StatutAvis.PUBLIE);
        long nbAvis = avisRepository.countByPharmacopeeIdAndStatut(ph.getId(), StatutAvis.PUBLIE);
        return pharmacopeeMapper.toSummaryResponse(ph, modes, nbProduits, noteMoyenne, nbAvis);
    }

    public PopulationToggleFavoriResponse toToggleResponse(TypeFavori type, Long cibleId, boolean favori, Long favoriId, String message) {
        return PopulationToggleFavoriResponse.builder()
                .type(type)
                .cibleId(cibleId)
                .favori(favori)
                .favoriId(favoriId)
                .message(message)
                .build();
    }

    public PopulationFavoriCheckResponse toCheckResponse(TypeFavori type, Long cibleId, boolean favori, Long favoriId) {
        return PopulationFavoriCheckResponse.builder()
                .type(type)
                .cibleId(cibleId)
                .favori(favori)
                .favoriId(favoriId)
                .build();
    }

    public PopulationFavoriCountResponse toCountResponse(long total, long plantes, long produits, long pharmacopees) {
        return PopulationFavoriCountResponse.builder()
                .total(total)
                .totalPlantes(plantes)
                .totalProduits(produits)
                .totalPharmacopees(pharmacopees)
                .build();
    }
}
