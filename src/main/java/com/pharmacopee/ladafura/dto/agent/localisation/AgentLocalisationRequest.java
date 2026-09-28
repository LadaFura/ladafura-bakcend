package com.pharmacopee.ladafura.dto.agent.localisation;

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
@Schema(description = "Requête d'enregistrement ou mise à jour de la localisation géographique d'une collecte de terrain")
public class AgentLocalisationRequest {

    @NotBlank(message = "La région administrative est obligatoire")
    @Size(max = 100, message = "La région ne peut pas dépasser 100 caractères")
    @Schema(description = "Région administrative de recueil", example = "Sikasso", requiredMode = Schema.RequiredMode.REQUIRED)
    private String region;

    @NotBlank(message = "Le cercle administratif est obligatoire")
    @Size(max = 100, message = "Le cercle ne peut pas dépasser 100 caractères")
    @Schema(description = "Cercle administratif", example = "Koutiala", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cercle;

    @NotBlank(message = "La commune est obligatoire")
    @Size(max = 100, message = "La commune ne peut pas dépasser 100 caractères")
    @Schema(description = "Commune administrative", example = "Finkolo", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commune;

    @NotBlank(message = "La localité ou village est obligatoire")
    @Size(max = 100, message = "La localité ne peut pas dépasser 100 caractères")
    @Schema(description = "Village, hameau ou localité précise du recueil de terrain", example = "Finkolo Centre", requiredMode = Schema.RequiredMode.REQUIRED)
    private String localite;

    @DecimalMin(value = "-90.0", message = "La latitude doit être supérieure ou égale à -90.0")
    @DecimalMax(value = "90.0", message = "La latitude doit être inférieure ou égale à 90.0")
    @Schema(description = "Coordonnée latitude GPS (WGS84)", example = "12.3812")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "La longitude doit être supérieure ou égale à -180.0")
    @DecimalMax(value = "180.0", message = "La longitude doit être inférieure ou égale à 180.0")
    @Schema(description = "Coordonnée longitude GPS (WGS84)", example = "-5.4590")
    private Double longitude;
}
