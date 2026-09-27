package com.pharmacopee.ladafura.dto.admin.produit;

import lombok.*;

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
