package com.pharmacopee.ladafura.dto.pharmacopee.achat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutAchat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Achat de la pharmacopée")
public class AchatResponse {
    private Long id;
    private String numeroAchat;
    private LocalDateTime dateAchat;
    private Long fournisseurId;
    private String fournisseurNom;
    private StatutAchat statut;
    private BigDecimal montantTotal;
    private BigDecimal montantRemise;
    @Schema(description = "Somme des règlements valides")
    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;
    private String observation;
    private LocalDateTime dateValidation;
    private LocalDateTime dateAnnulation;
    private String motifAnnulation;
    private List<LigneAchatResponse> lignes;
}
