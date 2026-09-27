package com.pharmacopee.ladafura.dto.admin.produit;

import com.pharmacopee.ladafura.enums.StatutProduit;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    private Double prix;
    private String photoUrl;
    private StatutProduit statut;

    private Long categorieId;
    private String categorieNom;

    @Builder.Default
    private List<AdminCompositionProduitDto> compositions = new ArrayList<>();

    private Integer nbDisponibilites;
    private Double noteMoyenne;
    private Integer nbAvis;
}
