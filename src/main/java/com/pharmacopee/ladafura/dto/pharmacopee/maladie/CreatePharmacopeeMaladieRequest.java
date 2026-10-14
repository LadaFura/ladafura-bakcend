package com.pharmacopee.ladafura.dto.pharmacopee.maladie;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePharmacopeeMaladieRequest {

    @NotBlank(message = "Le nom de la maladie est requis")
    private String nom;

    private String description;
}

