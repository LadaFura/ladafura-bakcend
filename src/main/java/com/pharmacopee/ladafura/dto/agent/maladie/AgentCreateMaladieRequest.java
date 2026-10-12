package com.pharmacopee.ladafura.dto.agent.maladie;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentCreateMaladieRequest {

    @NotBlank(message = "Le nom de la pathologie ou symptôme est obligatoire")
    @Size(min = 2, max = 150, message = "Le nom doit comporter entre 2 et 150 caractères")
    private String nom;

    @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
    private String description;
}

