package com.pharmacopee.ladafura.dto.pharmacopee.commande;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aperçu synthétique d'une commande dans la liste de l'officine")
public class PharmacopeeCommandeItemResponse {

    @Schema(description = "Identifiant unique de la commande", example = "10")
    private Long id;

    @Schema(description = "Numéro de référence officiel de la commande", example = "CMD-2026-00042")
    private String numero;

    @Schema(description = "Date et heure de passage de commande")
    private LocalDateTime dateCommande;

    @Schema(description = "Statut d'avancement de la commande", example = "CONFIRMEE")
    private StatutCommande statut;

    @Schema(description = "Total des produits hors frais de livraison (FCFA)", example = "5000.0")
    private Double totalProduit;

    @Schema(description = "Montant des frais de livraison (FCFA)", example = "2000.0")
    private Double montantLivraison;

    @Schema(description = "Montant total TTC de la commande (FCFA)", example = "7000.0")
    private Double montantTotal;

    @Schema(description = "Mode de mise à disposition choisi : LIVRAISON ou PICKUP", example = "LIVRAISON")
    private TypeModeRetrait typeRetrait;

    @Schema(description = "Nom complet du client", example = "Amadou Traoré")
    private String clientNomComplet;

    @Schema(description = "Téléphone de contact du client", example = "+223 76 12 34 56")
    private String clientTelephone;

    @Schema(description = "Nombre d'articles distincts commandés", example = "2")
    private int nombreArticles;
}
