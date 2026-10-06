package com.pharmacopee.ladafura.dto.pharmacopee.profil;

import com.pharmacopee.ladafura.dto.pharmacopee.localisation.PharmacopeeLocalisationRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
@Schema(description = "Données modifiables du profil d'une pharmacopée")
public class PharmacopeeUpdateProfileRequest {

    @NotBlank(message = "Le nom officiel de l'établissement est obligatoire")
    @Size(min = 2, max = 150, message = "Le nom doit comporter entre 2 et 150 caractères")
    @Schema(description = "Nom officiel de la pharmacopée", example = "Pharmacie Traditionnelle Mandé & Frères")
    private String nom;

    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    @Schema(description = "Description des activités et spécialités de l'officine", example = "Spécialisée dans les tisanes, pommades et macérats de plantes médicinales traditionnelles.")
    private String description;

    @NotBlank(message = "Le numéro de téléphone officiel de l'officine est obligatoire")
    @Size(min = 8, max = 25, message = "Le téléphone de l'officine doit comporter entre 8 et 25 caractères")
    @Schema(description = "Numéro de téléphone professionnel de l'officine", example = "+223 20 22 33 44")
    private String telephone;

    @Size(max = 25, message = "Le numéro de téléphone du responsable ne doit pas dépasser 25 caractères")
    @Schema(description = "Numéro de téléphone direct du responsable de l'officine", example = "+223 70 11 22 33")
    private String telephoneResponsable;

    @Valid
    @Schema(description = "Coordonnées de localisation géographique (optionnel)")
    private PharmacopeeLocalisationRequest localisation;

    @Schema(description = "URL de la photo ou bannière de l'établissement (optionnel)", example = "/uploads/pharmacopees/officine.jpg")
    private String photoUrl;
}
