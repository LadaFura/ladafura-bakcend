package com.pharmacopee.ladafura.dto.pharmacopee.commande;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Coordonnées de l'acheteur / client")
public class ClientInfoResponse {

    @Schema(description = "Identifiant de l'utilisateur", example = "12")
    private Long id;

    @Schema(description = "Nom de famille", example = "Traoré")
    private String nom;

    @Schema(description = "Prénom", example = "Amadou")
    private String prenom;

    @Schema(description = "Nom complet", example = "Amadou Traoré")
    private String nomComplet;

    @Schema(description = "Adresse email", example = "amadou.traore@gmail.com")
    private String email;

    @Schema(description = "Numéro de téléphone", example = "+223 76 12 34 56")
    private String telephone;
}
