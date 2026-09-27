package com.pharmacopee.ladafura.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteDetailResponse;
import com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteSummaryResponse;

@Mapper(componentModel = "spring", uses = {AdminPlanteMapper.class})
public interface AdminCollecteMapper {

    @Mapping(target = "nomCompletAgent", ignore = true)
    @Mapping(target = "nomCompletSource", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "cercle", ignore = true)
    @Mapping(target = "localite", ignore = true)
    @Mapping(target = "nbVertus", ignore = true)
    AdminCollecteSummaryResponse toSummaryDto(Collecte collecte);

    @Mapping(target = "agentId", ignore = true)
    @Mapping(target = "agentNomComplet", ignore = true)
    @Mapping(target = "agentMatricule", ignore = true)
    @Mapping(target = "agentTelephone", ignore = true)
    @Mapping(target = "sourceId", ignore = true)
    @Mapping(target = "sourceNomComplet", ignore = true)
    @Mapping(target = "sourceSpecialite", ignore = true)
    @Mapping(target = "sourceTelephone", ignore = true)
    @Mapping(target = "sourceAdresse", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "cercle", ignore = true)
    @Mapping(target = "commune", ignore = true)
    @Mapping(target = "localite", ignore = true)
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    AdminCollecteDetailResponse toDetailDto(Collecte collecte);

    @AfterMapping
    default void enrichSummary(Collecte collecte, @MappingTarget AdminCollecteSummaryResponse summary) {
        if (collecte.getAgentCollecte() != null) {
            summary.setNomCompletAgent(collecte.getAgentCollecte().getPrenom() + " " + collecte.getAgentCollecte().getNom());
        }
        if (collecte.getSource() != null) {
            summary.setNomCompletSource(collecte.getSource().getPrenom() + " " + collecte.getSource().getNom());
        }
        if (collecte.getLocalisation() != null) {
            summary.setRegion(collecte.getLocalisation().getRegion());
            summary.setCercle(collecte.getLocalisation().getCercle());
            summary.setLocalite(collecte.getLocalisation().getLocalite());
        }
        summary.setNbVertus(collecte.getVertus() != null ? collecte.getVertus().size() : 0);
    }

    @AfterMapping
    default void enrichDetail(Collecte collecte, @MappingTarget AdminCollecteDetailResponse detail) {
        if (collecte.getAgentCollecte() != null) {
            detail.setAgentId(collecte.getAgentCollecte().getId());
            detail.setAgentNomComplet(collecte.getAgentCollecte().getPrenom() + " " + collecte.getAgentCollecte().getNom());
            detail.setAgentMatricule(collecte.getAgentCollecte().getMatricule());
            detail.setAgentTelephone(collecte.getAgentCollecte().getTelephone());
        }
        if (collecte.getSource() != null) {
            detail.setSourceId(collecte.getSource().getId());
            detail.setSourceNomComplet(collecte.getSource().getPrenom() + " " + collecte.getSource().getNom());
            detail.setSourceSpecialite(collecte.getSource().getSpecialite());
            detail.setSourceTelephone(collecte.getSource().getTelephone());
            detail.setSourceAdresse(collecte.getSource().getAdresse());
        }
        if (collecte.getLocalisation() != null) {
            detail.setRegion(collecte.getLocalisation().getRegion());
            detail.setCercle(collecte.getLocalisation().getCercle());
            detail.setCommune(collecte.getLocalisation().getCommune());
            detail.setLocalite(collecte.getLocalisation().getLocalite());
            detail.setLatitude(collecte.getLocalisation().getLatitude());
            detail.setLongitude(collecte.getLocalisation().getLongitude());
        }
    }
}
