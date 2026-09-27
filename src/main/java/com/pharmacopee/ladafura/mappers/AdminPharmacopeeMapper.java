package com.pharmacopee.ladafura.mappers;

import java.util.stream.Collectors;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminModeRetraitResponse;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminPharmacopeeDetailResponse;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminPharmacopeeSummaryResponse;

@Mapper(componentModel = "spring")
public interface AdminPharmacopeeMapper {

    @Mapping(target = "region", ignore = true)
    @Mapping(target = "cercle", ignore = true)
    @Mapping(target = "commune", ignore = true)
    @Mapping(target = "localite", ignore = true)
    @Mapping(target = "nomProprietaire", ignore = true)
    @Mapping(target = "nbProduits", ignore = true)
    AdminPharmacopeeSummaryResponse toSummaryDto(Pharmacopee pharmacopee);

    @Mapping(target = "region", ignore = true)
    @Mapping(target = "cercle", ignore = true)
    @Mapping(target = "commune", ignore = true)
    @Mapping(target = "localite", ignore = true)
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    @Mapping(target = "utilisateurId", ignore = true)
    @Mapping(target = "nomCompletProprietaire", ignore = true)
    @Mapping(target = "emailProprietaire", ignore = true)
    @Mapping(target = "modesRetrait", ignore = true)
    @Mapping(target = "nbProduits", ignore = true)
    @Mapping(target = "nbCommandes", ignore = true)
    AdminPharmacopeeDetailResponse toDetailDto(Pharmacopee pharmacopee);

    AdminModeRetraitResponse toModeRetraitDto(ModeRetrait modeRetrait);

    @AfterMapping
    default void enrichSummary(Pharmacopee pharmacopee, @MappingTarget AdminPharmacopeeSummaryResponse summary) {
        if (pharmacopee.getLocalisation() != null) {
            summary.setRegion(pharmacopee.getLocalisation().getRegion());
            summary.setCercle(pharmacopee.getLocalisation().getCercle());
            summary.setCommune(pharmacopee.getLocalisation().getCommune());
            summary.setLocalite(pharmacopee.getLocalisation().getLocalite());
        }
        if (pharmacopee.getUtilisateur() != null) {
            summary.setNomProprietaire(pharmacopee.getUtilisateur().getPrenom() + " " + pharmacopee.getUtilisateur().getNom());
        }
        summary.setNbProduits(pharmacopee.getDisponibilites() != null ? pharmacopee.getDisponibilites().size() : 0);
    }

    @AfterMapping
    default void enrichDetail(Pharmacopee pharmacopee, @MappingTarget AdminPharmacopeeDetailResponse detail) {
        if (pharmacopee.getLocalisation() != null) {
            detail.setRegion(pharmacopee.getLocalisation().getRegion());
            detail.setCercle(pharmacopee.getLocalisation().getCercle());
            detail.setCommune(pharmacopee.getLocalisation().getCommune());
            detail.setLocalite(pharmacopee.getLocalisation().getLocalite());
            detail.setLatitude(pharmacopee.getLocalisation().getLatitude());
            detail.setLongitude(pharmacopee.getLocalisation().getLongitude());
        }
        if (pharmacopee.getUtilisateur() != null) {
            detail.setUtilisateurId(pharmacopee.getUtilisateur().getId());
            detail.setNomCompletProprietaire(pharmacopee.getUtilisateur().getPrenom() + " " + pharmacopee.getUtilisateur().getNom());
            detail.setEmailProprietaire(pharmacopee.getUtilisateur().getEmail());
        }
        if (pharmacopee.getModesRetrait() != null) {
            detail.setModesRetrait(pharmacopee.getModesRetrait().stream()
                    .map(this::toModeRetraitDto)
                    .collect(Collectors.toList()));
        }
        detail.setNbProduits(pharmacopee.getDisponibilites() != null ? pharmacopee.getDisponibilites().size() : 0);
        detail.setNbCommandes(pharmacopee.getCommandes() != null ? pharmacopee.getCommandes().size() : 0);
    }
}
