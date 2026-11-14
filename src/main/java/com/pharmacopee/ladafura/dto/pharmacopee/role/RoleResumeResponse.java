package com.pharmacopee.ladafura.dto.pharmacopee.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Rôle attribué à un membre")
public class RoleResumeResponse {
    private Long id;
    @Schema(example = "VENDEUR_CAISSIER")
    private String code;
    @Schema(example = "Vendeur-caissier")
    private String libelle;
}
