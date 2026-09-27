package com.pharmacopee.ladafura.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.pharmacopee.ladafura.Models.EtudeScientifique;
import com.pharmacopee.ladafura.dto.admin.etude.AdminEtudeRequest;
import com.pharmacopee.ladafura.dto.admin.etude.AdminEtudeResponse;

@Mapper(componentModel = "spring")
public interface AdminEtudeMapper {

    @Mapping(target = "planteId", ignore = true)
    @Mapping(target = "planteNomScientifique", ignore = true)
    AdminEtudeResponse toDto(EtudeScientifique etude);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plante", ignore = true)
    EtudeScientifique toEntity(AdminEtudeRequest request);

    @AfterMapping
    default void enrichEtudeResponse(EtudeScientifique etude, @MappingTarget AdminEtudeResponse response) {
        if (etude.getPlante() != null) {
            response.setPlanteId(etude.getPlante().getId());
            response.setPlanteNomScientifique(etude.getPlante().getNomScientifique());
        }
    }
}
