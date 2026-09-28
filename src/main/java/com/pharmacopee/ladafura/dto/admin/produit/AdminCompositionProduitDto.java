package com.pharmacopee.ladafura.dto.admin.produit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCompositionProduitDto {

    private Long planteId;
    private String planteNomScientifique;
    private Double quantite;
    private String unite;
}
