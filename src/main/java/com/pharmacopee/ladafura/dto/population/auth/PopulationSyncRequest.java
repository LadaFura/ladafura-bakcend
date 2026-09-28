package com.pharmacopee.ladafura.dto.population.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Données complémentaires pour synchroniser un compte créé via Firebase Auth direct")
public class PopulationSyncRequest {

    @Schema(description = "Nom de famille (si non présent dans le profil Firebase)", example = "Diarra")
    private String nom;

    @Schema(description = "Prénom (si non présent dans le profil Firebase)", example = "Fatoumata")
    private String prenom;

    @Schema(description = "Numéro de téléphone", example = "+223 70 12 34 56")
    private String telephone;
}
