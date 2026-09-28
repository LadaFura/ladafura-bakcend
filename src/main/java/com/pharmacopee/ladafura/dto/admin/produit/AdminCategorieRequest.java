package com.pharmacopee.ladafura.dto.admin.produit;

import jakarta.validation.constraints.NotBlank;
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
public class AdminCategorieRequest {

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    private String nom;

    private String description;

    @Builder.Default
    private Boolean statut = true;
}
