package com.pharmacopee.ladafura.dto.pharmacopee.caisse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.TypeCaisse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Caisse ou compte de trésorerie")
public class CaisseResponse {
    private Long id;
    private String nom;
    private TypeCaisse type;
    private String referenceCompte;
    private String description;
    private Boolean actif;
    private LocalDateTime dateCreation;
    @Schema(description = "Calculé, jamais stocké")
    private BigDecimal soldeTheorique;
    private Long sessionOuverteId;
}
