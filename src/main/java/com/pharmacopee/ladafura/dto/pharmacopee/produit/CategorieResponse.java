package com.pharmacopee.ladafura.dto.pharmacopee.produit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategorieResponse {
    private Long id;
    private String nom;
}
