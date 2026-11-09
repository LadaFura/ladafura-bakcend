package com.pharmacopee.ladafura.dto.pharmacopee.depense;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.CategorieDepense;
import com.pharmacopee.ladafura.enums.MethodePaiement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dépense d'exploitation, payée depuis une caisse ou un compte")
public class EnregistrerDepenseRequest {
    @NotBlank(message = "Le libellé est obligatoire")
    @Schema(example = "Facture EDM de novembre")
    private String libelle;
    @NotNull(message = "La catégorie est obligatoire")
    private CategorieDepense categorie;
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être positif")
    private BigDecimal montant;
    private LocalDateTime dateDepense;
    private String beneficiaire;
    @Schema(description = "URL issue de l'envoi de fichier existant")
    private String justificatifUrl;
    private String observation;
    @NotNull(message = "La caisse ou le compte est obligatoire")
    private Long caisseId;
    @NotNull(message = "Le mode de paiement est obligatoire")
    private MethodePaiement methode;
}
