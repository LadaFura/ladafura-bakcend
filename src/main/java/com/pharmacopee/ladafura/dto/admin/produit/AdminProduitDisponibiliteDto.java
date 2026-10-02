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
public class AdminProduitDisponibiliteDto {

    private Long id;
    private Long pharmacopeeId;
    private String pharmacopeeNom;
    private String telephone;
    private String commune;
    private String region;
    private Integer quantiteStock;
    private Double prix;
    private Boolean disponible;
}
