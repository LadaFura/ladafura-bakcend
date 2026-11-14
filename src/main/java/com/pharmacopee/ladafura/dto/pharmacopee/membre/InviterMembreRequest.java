package com.pharmacopee.ladafura.dto.pharmacopee.membre;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Invitation d'un utilisateur LADAFURA existant : renseigner l'email ou le téléphone exact")
public class InviterMembreRequest {

    @Email(message = "L'email n'est pas valide")
    @Schema(description = "Email exact du compte", example = "awa.sissoko@gmail.com")
    private String email;

    @Schema(description = "Téléphone exact du compte", example = "+223 70 00 00 00")
    private String telephone;

    @NotEmpty(message = "Au moins un rôle est obligatoire")
    @Schema(description = "Rôles attribués à l'acceptation")
    private List<Long> roleIds;

    @Size(max = 100, message = "Le poste ne doit pas dépasser 100 caractères")
    @Schema(description = "Intitulé affiché", example = "Vendeuse")
    private String poste;
}
