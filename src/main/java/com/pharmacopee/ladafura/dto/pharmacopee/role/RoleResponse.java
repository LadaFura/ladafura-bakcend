package com.pharmacopee.ladafura.dto.pharmacopee.role;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Rôle utilisable dans la pharmacopée, système ou personnalisé")
public class RoleResponse {
    private Long id;
    private String code;
    private String libelle;
    private String description;
    private Boolean systeme;
    private Boolean actif;
    @Schema(description = "Codes des permissions accordées")
    private List<String> permissions;
}
