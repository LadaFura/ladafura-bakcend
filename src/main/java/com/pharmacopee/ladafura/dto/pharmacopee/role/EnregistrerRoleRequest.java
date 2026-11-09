package com.pharmacopee.ladafura.dto.pharmacopee.role;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Création ou modification d'un rôle personnalisé")
public class EnregistrerRoleRequest {

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 50, message = "Le libellé ne doit pas dépasser 50 caractères")
    @Schema(example = "Vendeur de nuit")
    private String libelle;

    @Schema(example = "Ventes au comptoir en soirée")
    private String description;

    @NotEmpty(message = "Au moins une permission est obligatoire")
    @Schema(description = "Codes de permissions de portée PHARMACOPEE")
    private Set<String> permissionCodes;
}
