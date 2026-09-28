package com.pharmacopee.ladafura.dto.pharmacopee.avis;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Avis et note moyenne pour un produit spécifique de l'officine")
public class PharmacopeeProduitAvisResponse {

    @Schema(description = "Identifiant du produit", example = "5")
    private Long produitId;

    @Schema(description = "Nom du produit", example = "Tisane Hépatite Kinkéliba")
    private String nomProduit;

    @Schema(description = "Note moyenne calculée sur les avis publiés (sur 5.0)", example = "4.8")
    private Double noteMoyenne;

    @Schema(description = "Nombre total d'avis publiés pour ce produit", example = "14")
    private long totalAvis;

    @Schema(description = "Liste paginée des avis publiés pour ce produit")
    private Page<PharmacopeeAvisItemResponse> avis;
}
