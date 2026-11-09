package com.pharmacopee.ladafura.dto.pharmacopee.tresorerie;

import java.math.BigDecimal;

import com.pharmacopee.ladafura.enums.CategorieDepense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dépenses d'une catégorie sur la période")
public class DepenseCategorieResponse {
    private CategorieDepense categorie;
    private BigDecimal montant;
}
