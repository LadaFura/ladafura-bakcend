package com.pharmacopee.ladafura.dto.pharmacopee.abonnement;

import java.math.BigDecimal;
import java.util.List;

import com.pharmacopee.ladafura.enums.TypeOffre;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Offre d'abonnement")
public class OffreResponse {
    private Long id;
    private String code;
    private String nom;
    private String description;
    private TypeOffre typeOffre;
    private Integer dureeMois;
    private BigDecimal prix;
    private String devise;
    private Integer joursGrace;
    private Boolean actif;
    private List<QuotaOffreResponse> quotas;
}
