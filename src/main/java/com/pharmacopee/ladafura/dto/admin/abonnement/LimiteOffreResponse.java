package com.pharmacopee.ladafura.dto.admin.abonnement;

import com.pharmacopee.ladafura.enums.ModeCalculLimite;
import com.pharmacopee.ladafura.enums.PeriodiciteLimite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Limite du catalogue")
public class LimiteOffreResponse {
    private Long id;
    private String codeLimite;
    private String nom;
    private String description;
    private PeriodiciteLimite periodicite;
    private ModeCalculLimite modeCalcul;
}
