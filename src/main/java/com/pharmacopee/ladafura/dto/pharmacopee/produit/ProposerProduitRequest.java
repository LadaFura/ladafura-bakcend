package com.pharmacopee.ladafura.dto.pharmacopee.produit;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProposerProduitRequest {

    @NotBlank(message = "Le nom du produit est requis")
    private String nom;

    private String description;

    @NotBlank(message = "La forme du produit est requise")
    private String forme;

    private List<Long> planteIds;

    private List<PharmacopeeCompositionRequestDto> compositions;

    private String photoUrl;

    @NotNull(message = "Le prix de base est requis")
    @Positive(message = "Le prix doit être positif")
    private Double prix;

    @NotNull(message = "La catégorie est requise")
    private Long categorieId;
    
    // Facultatif: l'utilisateur peut également initialiser son stock dès la proposition
    @Positive(message = "Le stock initial doit être positif")
    private Integer quantiteInitiale = 0;
}
