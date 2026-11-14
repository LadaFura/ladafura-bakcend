package com.pharmacopee.ladafura.dto.admin.abonnement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nouvelle valeur d'un paramètre")
public class ModifierParametreRequest {
    @NotBlank(message = "La valeur est obligatoire")
    private String valeur;
}
