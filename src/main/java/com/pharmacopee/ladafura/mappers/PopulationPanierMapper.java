package com.pharmacopee.ladafura.mappers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.LignePanier;
import com.pharmacopee.ladafura.Models.Panier;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.panier.PopulationLignePanierResponse;
import com.pharmacopee.ladafura.dto.population.panier.PopulationPanierResponse;
import com.pharmacopee.ladafura.enums.StatutProduit;

@Component
public class PopulationPanierMapper {

    public PopulationPanierResponse toPanierResponse(Panier panier) {
        if (panier == null) {
            return PopulationPanierResponse.builder()
                    .panierId(null)
                    .nombreArticles(0)
                    .montantTotal(0.0)
                    .dateModification(null)
                    .lignes(List.of())
                    .build();
        }

        List<PopulationLignePanierResponse> lignesDto = new ArrayList<>();
        int totalArticles = 0;
        double montantTotal = 0.0;

        if (panier.getLignes() != null) {
            for (LignePanier ligne : panier.getLignes()) {
                PopulationLignePanierResponse itemDto = toLigneResponse(ligne);
                if (itemDto != null) {
                    lignesDto.add(itemDto);
                    totalArticles += (ligne.getQuantite() != null ? ligne.getQuantite() : 0);
                    montantTotal += (ligne.getSousTotal() != null ? ligne.getSousTotal() : 0.0);
                }
            }
        }

        return PopulationPanierResponse.builder()
                .panierId(panier.getId())
                .nombreArticles(totalArticles)
                .montantTotal(Math.round(montantTotal * 100.0) / 100.0)
                .dateModification(panier.getDateModification())
                .lignes(lignesDto)
                .build();
    }

    public PopulationLignePanierResponse toLigneResponse(LignePanier ligne) {
        if (ligne == null) {
            return null;
        }

        Produit produit = ligne.getProduit();
        boolean disponible = produit != null && produit.getStatut() == StatutProduit.VALIDE;

        return PopulationLignePanierResponse.builder()
                .ligneId(ligne.getId())
                .produitId(produit != null ? produit.getId() : null)
                .nomProduit(produit != null ? produit.getNom() : null)
                .forme(produit != null ? produit.getForme() : null)
                .photoUrl(produit != null ? produit.getPhotoUrl() : null)
                .prixUnitaire(ligne.getPrixUnitaire())
                .quantite(ligne.getQuantite())
                .sousTotal(ligne.getSousTotal())
                .disponible(disponible)
                .build();
    }
}
