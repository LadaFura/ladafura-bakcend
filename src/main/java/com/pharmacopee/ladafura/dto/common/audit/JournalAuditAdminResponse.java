package com.pharmacopee.ladafura.dto.common.audit;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.TypeActionAudit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entrée du journal d'audit, vue de l'administrateur")
public class JournalAuditAdminResponse {
    private Long id;
    @Schema(description = "Absent pour une action de niveau plateforme")
    private Long pharmacopeeId;
    private String pharmacopeeNom;
    private TypeActionAudit action;
    private OrigineAction origine;
    private String typeEntite;
    private Long entiteId;
    private String ancienneValeur;
    private String nouvelleValeur;
    private String description;
    private String auteur;
    private String adresseIp;
    private LocalDateTime dateAction;
}
