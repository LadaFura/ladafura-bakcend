package com.pharmacopee.ladafura.dto.pharmacopee.localisation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Détails géographiques et positionnement GPS de la pharmacopée")
public class PharmacopeeLocalisationResponse {

    @Schema(description = "Identifiant unique de la localisation", example = "3")
    private Long id;

    @Schema(description = "Identifiant de la pharmacopée associée", example = "1")
    private Long pharmacopeeId;

    @Schema(description = "Nom de la pharmacopée", example = "Pharmacie Traditionnelle Mandé")
    private String nomPharmacopee;

    @Schema(description = "Région administrative", example = "Bamako")
    private String region;

    @Schema(description = "Cercle ou préfecture", example = "Bamako")
    private String cercle;

    @Schema(description = "Commune de rattachement", example = "Commune IV")
    private String commune;

    @Schema(description = "Localité, quartier ou adresse", example = "Lafiabougou, Rue 384, Porte 12")
    private String localite;

    @Schema(description = "Latitude GPS", example = "12.6392")
    private Double latitude;

    @Schema(description = "Longitude GPS", example = "-8.0029")
    private Double longitude;

    @Schema(description = "Indique si des coordonnées GPS valides sont renseignées pour la cartographie", example = "true")
    private boolean geolocalisee;

    @Schema(description = "Adresse formatée complète", example = "Lafiabougou, Rue 384, Porte 12, Commune IV, Bamako, Région de Bamako")
    private String adresseComplete;
}
