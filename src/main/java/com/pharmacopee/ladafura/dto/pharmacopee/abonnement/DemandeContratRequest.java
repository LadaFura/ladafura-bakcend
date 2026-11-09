package com.pharmacopee.ladafura.dto.pharmacopee.abonnement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Demande de contrat personnalisé")
public class DemandeContratRequest {
    @Min(value = 1, message = "La durée doit être positive")
    private Integer dureeSouhaiteeMois;
    @NotBlank(message = "Le nom du contact est obligatoire")
    @Schema(example = "Awa Sissoko")
    private String nomContact;
    @NotBlank(message = "Le téléphone est obligatoire")
    @Schema(example = "+223 70 00 00 00")
    private String telephone;
    @Email(message = "L'email n'est pas valide")
    private String email;
    @NotBlank(message = "La description du besoin est obligatoire")
    private String descriptionBesoin;
}
