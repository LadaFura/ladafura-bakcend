package com.pharmacopee.ladafura.dto.pharmacopee.localisation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
@Schema(description = "Données géographiques et coordonnées GPS pour la localisation de la pharmacopée")
public class PharmacopeeLocalisationRequest {

    @NotBlank(message = "La région administrative est obligatoire")
    @Size(max = 100, message = "La région ne doit pas dépasser 100 caractères")
    @Schema(description = "Région administrative au Mali", example = "Bamako")
    private String region;

    @NotBlank(message = "Le cercle est obligatoire")
    @Size(max = 100, message = "Le cercle ne doit pas dépasser 100 caractères")
    @Schema(description = "Cercle ou préfecture", example = "Bamako")
    private String cercle;

    @NotBlank(message = "La commune est obligatoire")
    @Size(max = 100, message = "La commune ne doit pas dépasser 100 caractères")
    @Schema(description = "Commune de rattachement", example = "Commune IV")
    private String commune;

    @NotBlank(message = "La localité, quartier ou rue est obligatoire")
    @Size(max = 150, message = "La localité ne doit pas dépasser 150 caractères")
    @Schema(description = "Quartier, village ou repère précis", example = "Lafiabougou, Rue 384, Porte 12")
    private String localite;

    @DecimalMin(value = "-90.0", message = "La latitude doit être supérieure ou égale à -90.0")
    @DecimalMax(value = "90.0", message = "La latitude doit être inférieure ou égale à 90.0")
    @Schema(description = "Coordonnée GPS : Latitude (degré décimal)", example = "12.6392")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "La longitude doit être supérieure ou égale à -180.0")
    @DecimalMax(value = "180.0", message = "La longitude doit être inférieure ou égale à 180.0")
    @Schema(description = "Coordonnée GPS : Longitude (degré décimal)", example = "-8.0029")
    private Double longitude;
}
