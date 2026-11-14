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
@Schema(description = "Entrée du journal d'audit d'une pharmacopée")
public class JournalAuditResponse {
    private Long id;
    private TypeActionAudit action;
    private OrigineAction origine;
    private String typeEntite;
    private Long entiteId;
    private String ancienneValeur;
    private String nouvelleValeur;
    private String description;
    @Schema(description = "Absent pour une action du système")
    private String auteur;
    private LocalDateTime dateAction;
}
