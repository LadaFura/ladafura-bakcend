package com.pharmacopee.ladafura.dto.admin.produit;

import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutProduit;

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
public class AdminProduitResponse {

    private Long id;
    private String nom;
    private String description;
    private String forme;
    private String composition;
    private String posologie;
    private Double prix;
    private String photoUrl;
    private StatutProduit statut;

    private Long categorieId;
    private String categorieNom;

    @Builder.Default
    private List<AdminCompositionProduitDto> compositions = new ArrayList<>();

    @Builder.Default
    private List<String> maladies = new ArrayList<>();

    @Builder.Default
    private List<Long> maladieIds = new ArrayList<>();

    private Integer nbDisponibilites;
    private Double noteMoyenne;
    private Integer nbAvis;

    @Builder.Default
    private List<AdminProduitDisponibiliteDto> pointsDeVente = new ArrayList<>();
}
