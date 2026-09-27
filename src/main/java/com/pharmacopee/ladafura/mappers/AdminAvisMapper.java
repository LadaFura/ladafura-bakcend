package com.pharmacopee.ladafura.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.dto.admin.avis.AdminAvisResponse;

@Mapper(componentModel = "spring")
public interface AdminAvisMapper {

    @Mapping(target = "utilisateurId", ignore = true)
    @Mapping(target = "nomCompletUtilisateur", ignore = true)
    @Mapping(target = "emailUtilisateur", ignore = true)
    @Mapping(target = "produitId", ignore = true)
    @Mapping(target = "nomProduit", ignore = true)
    AdminAvisResponse toDto(Avis avis);

    @AfterMapping
    default void enrichAvisResponse(Avis avis, @MappingTarget AdminAvisResponse response) {
        if (avis.getUtilisateur() != null) {
            response.setUtilisateurId(avis.getUtilisateur().getId());
            response.setNomCompletUtilisateur(avis.getUtilisateur().getPrenom() + " " + avis.getUtilisateur().getNom());
            response.setEmailUtilisateur(avis.getUtilisateur().getEmail());
        }
        if (avis.getProduit() != null) {
            response.setProduitId(avis.getProduit().getId());
            response.setNomProduit(avis.getProduit().getNom());
        }
    }
}
