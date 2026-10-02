package com.pharmacopee.ladafura.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.admin.user.AdminUserResponse;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {

    @Mapping(target = "matricule", ignore = true)
    @Mapping(target = "zoneCouverture", ignore = true)
    @Mapping(target = "specialite", ignore = true)
    @Mapping(target = "anneesExperience", ignore = true)
    @Mapping(target = "adresse", ignore = true)
    @Mapping(target = "estPraticienPrincipal", ignore = true)
    @Mapping(target = "typePharmacopee", ignore = true)
    @Mapping(target = "numeroAgrement", ignore = true)
    @Mapping(target = "pharmacopees", ignore = true)
    AdminUserResponse toDto(Utilisateur utilisateur);

    @AfterMapping
    default void populateSpecificFields(Utilisateur utilisateur, @MappingTarget AdminUserResponse response) {
        if (utilisateur instanceof AgentCollecte agent) {
            response.setMatricule(agent.getMatricule());
            response.setZoneCouverture(agent.getZoneCouverture());
        } else if (utilisateur instanceof Source source) {
            response.setSpecialite(source.getSpecialite());
            response.setAnneesExperience(source.getAnneesExperience());
            response.setAdresse(source.getAdresse());
        } else if (utilisateur instanceof com.pharmacopee.ladafura.Models.Praticien praticien) {
            response.setSpecialite(praticien.getSpecialite());
            response.setNumeroAgrement(praticien.getNumeroAgrement());
            boolean isPrincipal = praticien.getEstPraticienPrincipal() == null || praticien.getEstPraticienPrincipal();
            response.setEstPraticienPrincipal(isPrincipal);
            response.setTypePharmacopee(isPrincipal ? "Principale" : "Collaborateur");
        }
    }
}
