package com.pharmacopee.ladafura.dto.admin.maladie;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
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
public class AdminMaladieRequest {

    @NotBlank(message = "Le nom de la maladie est obligatoire")
    private String nom;

    private String description;

    @Builder.Default
    private List<Long> planteIds = new ArrayList<>();
}
