package com.pharmacopee.ladafura.dto.agent.profil;

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
@Schema(description = "Données modifiables du profil de l'Agent de Collecte (coordonnées et zone d'affectation)")
public class AgentUpdateProfileRequest {

    @NotBlank(message = "Le nom de famille est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit comporter entre 2 et 100 caractères")
    @Schema(description = "Nom de famille de l'agent", example = "Coulibaly")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit comporter entre 2 et 100 caractères")
    @Schema(description = "Prénom de l'agent", example = "Oumar")
    private String prenom;

    @Size(max = 25, message = "Le numéro de téléphone ne doit pas dépasser 25 caractères")
    @Schema(description = "Numéro de téléphone professionnel", example = "+223 75 00 11 22")
    private String telephone;

    @Size(max = 255, message = "La zone de couverture ne doit pas dépasser 255 caractères")
    @Schema(description = "Zone géographique d'intervention sur le terrain", example = "Région de Sikasso - Cercles de Koutiala et Bougouni")
    private String zoneCouverture;
}
