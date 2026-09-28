package com.pharmacopee.ladafura.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationMaladieSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationPharmacopeeSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationPlanteSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationProduitSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationVernaculaireSearchItem;

@Component
public class PopulationRechercheMapper {

    public PopulationPlanteSearchItem toPlanteItem(Plante plante) {
        if (plante == null) return null;

        List<String> noms = plante.getNomsPlante() != null
                ? plante.getNomsPlante().stream()
                        .map(n -> n.getNom() + (n.getLangue() != null ? " (" + n.getLangue() + ")" : ""))
                        .toList()
                : Collections.emptyList();

        return PopulationPlanteSearchItem.builder()
                .id(plante.getId())
                .nomScientifique(plante.getNomScientifique())
                .description(plante.getDescription())
                .photoUrl(plante.getPhotoUrl())
                .nomsVernaculaires(noms)
                .build();
    }

    public PopulationVernaculaireSearchItem toVernaculaireItem(NomPlante nomPlante) {
        if (nomPlante == null) return null;

        return PopulationVernaculaireSearchItem.builder()
                .id(nomPlante.getId())
                .nom(nomPlante.getNom())
                .langue(nomPlante.getLangue())
                .planteId(nomPlante.getPlante() != null ? nomPlante.getPlante().getId() : null)
                .nomScientifiquePlante(nomPlante.getPlante() != null ? nomPlante.getPlante().getNomScientifique() : null)
                .build();
    }

    public PopulationMaladieSearchItem toMaladieItem(Maladie maladie) {
        if (maladie == null) return null;

        return PopulationMaladieSearchItem.builder()
                .id(maladie.getId())
                .nom(maladie.getNom())
                .description(maladie.getDescription())
                .nombrePlantesAssociees(maladie.getPlantes() != null ? maladie.getPlantes().size() : 0)
                .build();
    }

    public PopulationProduitSearchItem toProduitItem(Produit produit) {
        if (produit == null) return null;

        return PopulationProduitSearchItem.builder()
                .id(produit.getId())
                .nom(produit.getNom())
                .description(produit.getDescription())
                .forme(produit.getForme())
                .prix(produit.getPrix())
                .photoUrl(produit.getPhotoUrl())
                .categorie(produit.getCategorie() != null ? produit.getCategorie().getNom() : null)
                .build();
    }

    public PopulationPharmacopeeSearchItem toPharmacopeeItem(Pharmacopee pharmacopee) {
        if (pharmacopee == null) return null;

        PopulationPharmacopeeSearchItem.PopulationPharmacopeeSearchItemBuilder builder = PopulationPharmacopeeSearchItem.builder()
                .id(pharmacopee.getId())
                .nom(pharmacopee.getNom())
                .description(pharmacopee.getDescription())
                .telephone(pharmacopee.getTelephone());

        if (pharmacopee.getLocalisation() != null) {
            builder.region(pharmacopee.getLocalisation().getRegion())
                   .cercle(pharmacopee.getLocalisation().getCercle())
                   .commune(pharmacopee.getLocalisation().getCommune())
                   .localite(pharmacopee.getLocalisation().getLocalite())
                   .latitude(pharmacopee.getLocalisation().getLatitude())
                   .longitude(pharmacopee.getLocalisation().getLongitude());
        }

        return builder.build();
    }
}
