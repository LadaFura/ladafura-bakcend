package com.pharmacopee.ladafura.dto.agent.localisation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Détail de la localisation géographique d'une collecte de terrain")
public class AgentLocalisationResponse {

    @Schema(description = "Identifiant de la fiche de collecte associée", example = "12")
    private Long collecteId;

    @Schema(description = "Identifiant unique de la localisation", example = "5")
    private Long localisationId;

    @Schema(description = "Région administrative", example = "Sikasso")
    private String region;

    @Schema(description = "Cercle administratif", example = "Koutiala")
    private String cercle;

    @Schema(description = "Commune administrative", example = "Finkolo")
    private String commune;

    @Schema(description = "Localité ou village de recueil", example = "Finkolo Centre")
    private String localite;

    @Schema(description = "Coordonnée GPS latitude", example = "12.3812")
    private Double latitude;

    @Schema(description = "Coordonnée GPS longitude", example = "-5.4590")
    private Double longitude;

    @Schema(description = "Indique si des coordonnées GPS précises sont renseignées", example = "true")
    private boolean coordonneesGpsPresentes;

    @Schema(description = "Adresse administrative complète formatée", example = "Finkolo Centre, Finkolo, Koutiala, Sikasso")
    private String adresseFormatee;
}
