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
@Schema(description = "Permission du catalogue, portée pharmacopée")
public class PermissionResponse {
    @Schema(example = "VENTES_CREER")
    private String code;
    private String libelle;
    private String description;
    private String module;
}
