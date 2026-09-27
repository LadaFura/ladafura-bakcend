package com.pharmacopee.ladafura.dto.admin.produit;

import lombok.*;

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
