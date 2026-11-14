package com.pharmacopee.ladafura.dto.pharmacopee.vente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutVente;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Vente au comptoir")
public class VenteResponse {
    private Long id;
    private String numeroVente;
    private LocalDateTime dateVente;
    private StatutVente statut;
    private Long clientId;
    private String clientNom;
    private BigDecimal montantBrut;
    private BigDecimal montantRemise;
    private BigDecimal montantTotal;
    @Schema(description = "Somme des paiements valides")
    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;
    private String observation;
    private LocalDateTime dateValidation;
    private LocalDateTime dateAnnulation;
    private String motifAnnulation;
    private List<LigneVenteResponse> lignes;
}
