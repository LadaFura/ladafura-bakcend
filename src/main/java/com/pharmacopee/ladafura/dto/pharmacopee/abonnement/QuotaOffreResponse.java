package com.pharmacopee.ladafura.dto.pharmacopee.abonnement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quota d'une offre")
public class QuotaOffreResponse {
    private Long limiteId;
    private String codeLimite;
    private String nom;
    private Integer valeurMaximale;
    private Boolean illimite;
}
