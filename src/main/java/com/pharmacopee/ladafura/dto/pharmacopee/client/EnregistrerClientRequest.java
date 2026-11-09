package com.pharmacopee.ladafura.dto.pharmacopee.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Création ou modification d'un client")
public class EnregistrerClientRequest {
    @NotBlank(message = "Le nom est obligatoire")
    @Schema(example = "Sidibé")
    private String nom;
    @Schema(example = "Oumar")
    private String prenom;
    @Size(max = 30, message = "Le téléphone ne doit pas dépasser 30 caractères")
    @Schema(description = "Unique dans la pharmacopée s'il est renseigné", example = "+223 70 00 00 00")
    private String telephone;
    private String adresse;
    private String observation;
}
