package com.pharmacopee.ladafura.dto.admin.produit;

import java.util.List;

import com.pharmacopee.ladafura.enums.StatutProduit;

import jakarta.validation.constraints.PositiveOrZero;
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
public class AdminUpdateProduitRequest {

    private String nom;

    private String description;

    private String forme;

    private String composition;

    @PositiveOrZero(message = "Le prix doit être positif ou nul")
    private Double prix;

    private String photoUrl;

    private StatutProduit statut;

    private Long categorieId;

    private List<AdminCompositionRequestDto> compositions;
}
