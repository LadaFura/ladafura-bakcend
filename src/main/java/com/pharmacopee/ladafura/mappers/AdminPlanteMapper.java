package com.pharmacopee.ladafura.mappers;

import java.util.stream.Collectors;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.admin.plante.AdminPlanteResponse;
import com.pharmacopee.ladafura.dto.admin.plante.AdminVertuResponse;
import com.pharmacopee.ladafura.dto.admin.plante.NomPlanteDto;

@Mapper(componentModel = "spring")
public interface AdminPlanteMapper {

    @Mapping(target = "nomsVernaculaires", ignore = true)
    @Mapping(target = "maladies", ignore = true)
    @Mapping(target = "nbEtudes", ignore = true)
    @Mapping(target = "nbVertus", ignore = true)
    AdminPlanteResponse toDto(Plante plante);

    NomPlanteDto toNomPlanteDto(NomPlante nomPlante);

    @Mapping(target = "plante", ignore = true)
    NomPlante toNomPlanteEntity(NomPlanteDto dto);

    @Mapping(source = "plante.id", target = "planteId")
    @Mapping(source = "plante.nomScientifique", target = "planteNomScientifique")
    @Mapping(target = "collecteId", ignore = true)
    @Mapping(target = "sourceNomComplet", ignore = true)
    @Mapping(target = "sourceSpecialite", ignore = true)
    @Mapping(target = "localisation", ignore = true)
    AdminVertuResponse toVertuDto(VertuDeLaPlante vertu);

    @AfterMapping
    default void enrichPlanteResponse(Plante plante, @MappingTarget AdminPlanteResponse response) {
        if (plante.getNomsPlante() != null) {
            response.setNomsVernaculaires(plante.getNomsPlante().stream()
                    .map(this::toNomPlanteDto)
                    .collect(Collectors.toList()));
        }
        if (plante.getMaladies() != null) {
            response.setMaladies(plante.getMaladies().stream()
                    .map(Maladie::getNom)
                    .collect(Collectors.toList()));
        }
        response.setNbEtudes(plante.getEtudesScientifiques() != null ? plante.getEtudesScientifiques().size() : 0);
        response.setNbVertus(plante.getVertus() != null ? plante.getVertus().size() : 0);
    }

    @AfterMapping
    default void enrichVertu(VertuDeLaPlante vertu, @MappingTarget AdminVertuResponse dto) {
        if (vertu.getCollecte() != null) {
            dto.setCollecteId(vertu.getCollecte().getId());
            if (vertu.getCollecte().getSource() != null) {
                dto.setSourceNomComplet(vertu.getCollecte().getSource().getPrenom() + " " + vertu.getCollecte().getSource().getNom());
                dto.setSourceSpecialite(vertu.getCollecte().getSource().getSpecialite());
            }
            if (vertu.getCollecte().getLocalisation() != null) {
                String loc = vertu.getCollecte().getLocalisation().getLocalite();
                if (vertu.getCollecte().getLocalisation().getRegion() != null) {
                    loc += " (" + vertu.getCollecte().getLocalisation().getRegion() + ")";
                }
                dto.setLocalisation(loc);
            }
        }
    }
}
