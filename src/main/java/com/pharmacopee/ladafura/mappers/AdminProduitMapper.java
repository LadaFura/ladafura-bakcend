package com.pharmacopee.ladafura.mappers;

import java.util.stream.Collectors;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.pharmacopee.ladafura.Models.CategorieProduit;
import com.pharmacopee.ladafura.Models.CompositionProduit;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.admin.produit.AdminCategorieResponse;
import com.pharmacopee.ladafura.dto.admin.produit.AdminCompositionProduitDto;
import com.pharmacopee.ladafura.dto.admin.produit.AdminProduitDisponibiliteDto;
import com.pharmacopee.ladafura.dto.admin.produit.AdminProduitResponse;

@Mapper(componentModel = "spring")
public interface AdminProduitMapper {

    @Mapping(target = "categorieId", ignore = true)
    @Mapping(target = "categorieNom", ignore = true)
    @Mapping(target = "compositions", ignore = true)
    @Mapping(target = "nbDisponibilites", ignore = true)
    @Mapping(target = "noteMoyenne", ignore = true)
    @Mapping(target = "nbAvis", ignore = true)
    @Mapping(target = "pointsDeVente", ignore = true)
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
        if (produit.getDisponibilites() != null && !produit.getDisponibilites().isEmpty()) {
            response.setNbDisponibilites(produit.getDisponibilites().size());
            response.setPointsDeVente(produit.getDisponibilites().stream()
                    .map(d -> AdminProduitDisponibiliteDto.builder()
                            .id(d.getId())
                            .pharmacopeeId(d.getPharmacopee() != null ? d.getPharmacopee().getId() : null)
                            .pharmacopeeNom(d.getPharmacopee() != null ? d.getPharmacopee().getNom() : null)
                            .telephone(d.getPharmacopee() != null ? d.getPharmacopee().getTelephone() : null)
                            .commune(d.getPharmacopee() != null && d.getPharmacopee().getLocalisation() != null ? d.getPharmacopee().getLocalisation().getCommune() : null)
                            .region(d.getPharmacopee() != null && d.getPharmacopee().getLocalisation() != null ? d.getPharmacopee().getLocalisation().getRegion() : null)
                            .quantiteStock(d.getQuantiteStock())
                            .prix(d.getPrix() != null ? d.getPrix() : produit.getPrix())
                            .disponible(d.getDisponible())
                            .build())
                    .collect(Collectors.toList()));
        } else {
            response.setNbDisponibilites(0);
        }
        response.setNbAvis(0);
        response.setNoteMoyenne(0.0);
        
    }

    @AfterMapping
    default void enrichCategorieResponse(CategorieProduit categorie, @MappingTarget AdminCategorieResponse response) {
        response.setNbProduits(categorie.getProduits() != null ? categorie.getProduits().size() : 0);
    }
}
