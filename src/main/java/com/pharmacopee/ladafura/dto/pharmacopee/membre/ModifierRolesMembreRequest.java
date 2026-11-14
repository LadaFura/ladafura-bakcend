package com.pharmacopee.ladafura.dto.pharmacopee.membre;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nouvelle liste complète des rôles d'un membre")
public class ModifierRolesMembreRequest {

    @NotEmpty(message = "Au moins un rôle est obligatoire")
    private List<Long> roleIds;
}
