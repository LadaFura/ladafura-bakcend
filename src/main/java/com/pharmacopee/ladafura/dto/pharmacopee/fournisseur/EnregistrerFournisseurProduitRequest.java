package com.pharmacopee.ladafura.dto.pharmacopee.fournisseur;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Produit proposé par un fournisseur (information, pas une commande)")
public class EnregistrerFournisseurProduitRequest {
    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;
    @Size(max = 100, message = "La référence ne doit pas dépasser 100 caractères")
    private String referenceFournisseur;
    @DecimalMin(value = "0", message = "Le prix ne peut pas être négatif")
    private BigDecimal prixIndicatif;
    @Min(value = 0, message = "Le délai ne peut pas être négatif")
    @Schema(description = "Délai en jours", example = "5")
    private Integer delaiApprovisionnement;
    private Boolean disponible;
    private String observation;
}
