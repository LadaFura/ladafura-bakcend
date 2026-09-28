package com.pharmacopee.ladafura.mappers;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteDetailResponse;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;

@Component
public class AgentCollecteMapper {

    public AgentCollecteSummaryResponse toSummaryDto(Collecte collecte) {
        if (collecte == null) return null;

        AgentCollecteSummaryResponse.AgentCollecteSummaryResponseBuilder builder = AgentCollecteSummaryResponse.builder()
                .id(collecte.getId())
                .dateCollecte(collecte.getDateCollecte())
                .description(collecte.getDescription())
                .statut(collecte.getStatut())
                .photoUrl(collecte.getPhotoUrl())
                .audioUrl(collecte.getAudioUrl())
                .dateSoumission(collecte.getDateSoumission())
                .motifRejet(collecte.getMotifRejet());

        if (collecte.getSource() != null) {
            builder.nomSource(collecte.getSource().getPrenom() + " " + collecte.getSource().getNom());
        }

        if (collecte.getLocalisation() != null) {
            builder.region(collecte.getLocalisation().getRegion())
                   .cercle(collecte.getLocalisation().getCercle())
                   .localite(collecte.getLocalisation().getLocalite());
        }

        builder.nombreVertus(collecte.getVertus() != null ? collecte.getVertus().size() : 0);

        if (collecte.getVertus() != null && !collecte.getVertus().isEmpty()) {
            VertuDeLaPlante firstVertu = collecte.getVertus().get(0);
            if (firstVertu != null && firstVertu.getPlante() != null) {
                builder.nomScientifiquePlante(firstVertu.getPlante().getNomScientifique());
            }
        }

        return builder.build();
    }

    public AgentCollecteDetailResponse toDetailDto(Collecte collecte) {
        if (collecte == null) return null;

        boolean canModifyOrSubmit = (collecte.getStatut() == StatutCollecte.BROUILLON || collecte.getStatut() == StatutCollecte.REJETEE);

        AgentCollecteDetailResponse.AgentCollecteDetailResponseBuilder builder = AgentCollecteDetailResponse.builder()
                .id(collecte.getId())
                .dateCollecte(collecte.getDateCollecte())
                .description(collecte.getDescription())
                .statut(collecte.getStatut())
                .photoUrl(collecte.getPhotoUrl())
                .audioUrl(collecte.getAudioUrl())
                .dateSoumission(collecte.getDateSoumission())
                .motifRejet(collecte.getMotifRejet())
                .modifiable(canModifyOrSubmit)
                .soumissible(canModifyOrSubmit);

        if (collecte.getAgentCollecte() != null) {
            builder.agentId(collecte.getAgentCollecte().getId())
                   .agentMatricule(collecte.getAgentCollecte().getMatricule())
                   .agentNomComplet(collecte.getAgentCollecte().getPrenom() + " " + collecte.getAgentCollecte().getNom());
        }

        if (collecte.getSource() != null) {
            builder.sourceId(collecte.getSource().getId())
                   .sourceNomComplet(collecte.getSource().getPrenom() + " " + collecte.getSource().getNom())
                   .sourceSpecialite(collecte.getSource().getSpecialite())
                   .sourceTelephone(collecte.getSource().getTelephone());
        }

        if (collecte.getLocalisation() != null) {
            builder.localisationId(collecte.getLocalisation().getId())
                   .region(collecte.getLocalisation().getRegion())
                   .cercle(collecte.getLocalisation().getCercle())
                   .commune(collecte.getLocalisation().getCommune())
                   .localite(collecte.getLocalisation().getLocalite())
                   .latitude(collecte.getLocalisation().getLatitude())
                   .longitude(collecte.getLocalisation().getLongitude());
        }

        builder.nombreVertus(collecte.getVertus() != null ? collecte.getVertus().size() : 0);

        return builder.build();
    }
}
