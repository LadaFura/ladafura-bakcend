package com.pharmacopee.ladafura.dto.pharmacopee.caisse;

import com.pharmacopee.ladafura.enums.TypeCaisse;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Création ou modification d'une caisse ou d'un compte")
public class EnregistrerCaisseRequest {
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    @Schema(example = "Caisse du comptoir")
    private String nom;
    @NotNull(message = "Le type est obligatoire")
    @Schema(description = "Ignoré en modification")
    private TypeCaisse type;
    @Size(max = 100, message = "La référence ne doit pas dépasser 100 caractères")
    @Schema(description = "Numéro Mobile Money ou bancaire")
    private String referenceCompte;
    private String description;
}
