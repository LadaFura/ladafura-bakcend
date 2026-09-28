package com.pharmacopee.ladafura.mappers;

import java.util.stream.Collectors;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.CategorieProduit;
import com.pharmacopee.ladafura.Models.CompositionProduit;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.admin.produit.AdminCategorieResponse;
import com.pharmacopee.ladafura.dto.admin.produit.AdminCompositionProduitDto;
import com.pharmacopee.ladafura.dto.admin.produit.AdminProduitResponse;
import com.pharmacopee.ladafura.enums.StatutAvis;

@Mapper(componentModel = "spring")
public interface AdminProduitMapper {

    @Mapping(target = "categorieId", ignore = true)
    @Mapping(target = "categorieNom", ignore = true)
    @Mapping(target = "compositions", ignore = true)
    @Mapping(target = "nbDisponibilites", ignore = true)
    @Mapping(target = "noteMoyenne", ignore = true)
    @Mapping(target = "nbAvis", ignore = true)
    AdminProduitResponse toDto(Produit produit);

    @Mapping(source = "plante.id", target = "planteId")
    @Mapping(source = "plante.nomScientifique", target = "planteNomScientifique")
    AdminCompositionProduitDto toCompositionDto(CompositionProduit composition);

    @Mapping(target = "nbProduits", ignore = true)
    AdminCategorieResponse toCategorieDto(CategorieProduit categorie);

    @AfterMapping
    default void enrichProduitResponse(Produit produit, @MappingTarget AdminProduitResponse response) {
        if (produit.getCategorie() != null) {
            response.setCategorieId(produit.getCategorie().getId());
            response.setCategorieNom(produit.getCategorie().getNom());
        }
        if (produit.getCompositions() != null) {
            response.setCompositions(produit.getCompositions().stream()
                    .map(this::toCompositionDto)
                    .collect(Collectors.toList()));
        }
        response.setNbDisponibilites(produit.getDisponibilites() != null ? produit.getDisponibilites().size() : 0);
        if (produit.getAvis() != null && !produit.getAvis().isEmpty()) {
            response.setNbAvis(produit.getAvis().size());
            double avg = produit.getAvis().stream()
                    .filter(a -> a.getStatut() == StatutAvis.PUBLIE)
                    .mapToInt(Avis::getNote)
                    .average()
                    .orElse(0.0);
            response.setNoteMoyenne(Math.round(avg * 10.0) / 10.0);
        } else {
            response.setNbAvis(0);
            response.setNoteMoyenne(0.0);
        }
    }

    @AfterMapping
    default void enrichCategorieResponse(CategorieProduit categorie, @MappingTarget AdminCategorieResponse response) {
        response.setNbProduits(categorie.getProduits() != null ? categorie.getProduits().size() : 0);
    }
}
