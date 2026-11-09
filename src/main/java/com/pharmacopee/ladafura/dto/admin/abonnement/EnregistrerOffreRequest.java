package com.pharmacopee.ladafura.dto.admin.abonnement;

import java.math.BigDecimal;

import com.pharmacopee.ladafura.enums.TypeOffre;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Création ou modification d'une offre ; la durée découle du type")
public class EnregistrerOffreRequest {
    @NotBlank(message = "Le nom est obligatoire")
    @Schema(example = "Offre mensuelle")
    private String nom;
    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 50, message = "Le code ne doit pas dépasser 50 caractères")
    @Schema(description = "Ignoré en modification", example = "MENSUELLE_PRO")
    private String code;
    private String description;
    @NotNull(message = "Le type est obligatoire")
    @Schema(description = "Ignoré en modification")
    private TypeOffre typeOffre;
    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0", message = "Le prix ne peut pas être négatif")
    @Schema(example = "10000")
    private BigDecimal prix;
    @Min(value = 0, message = "Les jours de grâce ne peuvent pas être négatifs")
    @Schema(example = "7")
    private Integer joursGrace;
}
