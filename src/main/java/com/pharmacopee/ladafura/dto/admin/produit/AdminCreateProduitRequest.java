package com.pharmacopee.ladafura.dto.admin.produit;

import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutProduit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AdminCreateProduitRequest {

    @NotBlank(message = "Le nom du produit ou remède est obligatoire")
    private String nom;

    private String description;

    private String forme;

    private String composition;

    @NotNull(message = "Le prix indicatif est obligatoire")
    @PositiveOrZero(message = "Le prix doit être positif ou nul")
    private Double prix;

    private String photoUrl;

    @Builder.Default
    private StatutProduit statut = StatutProduit.VALIDE;

    private Long categorieId;

    @Builder.Default
    private List<AdminCompositionRequestDto> compositions = new ArrayList<>();
}
