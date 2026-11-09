package com.pharmacopee.ladafura.dto.pharmacopee.depense;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.CategorieDepense;
import com.pharmacopee.ladafura.enums.StatutEcriture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dépense de la pharmacopée")
public class DepenseResponse {
    private Long id;
    private String libelle;
    private CategorieDepense categorie;
    private BigDecimal montant;
    private LocalDateTime dateDepense;
    private String beneficiaire;
    private String justificatifUrl;
    private String observation;
    private StatutEcriture statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateAnnulation;
    private String motifAnnulation;
}
