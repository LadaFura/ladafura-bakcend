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
        }
    }
}
