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
public class AdminCategorieResponse {

    private Long id;
    private String nom;
    private String description;
    private Boolean statut;
    private Integer nbProduits;
}
