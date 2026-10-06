package com.pharmacopee.ladafura.dto.pharmacopee.plante;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePharmacopeePlanteRequest {
    @NotBlank(message = "Le nom de la plante est requis")
    private String nom;
}
