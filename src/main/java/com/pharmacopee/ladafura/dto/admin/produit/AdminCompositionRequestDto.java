package com.pharmacopee.ladafura.dto.admin.produit;

import jakarta.validation.constraints.NotNull;
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
public class AdminCompositionRequestDto {

    @NotNull(message = "L'identifiant de la plante médicinale est obligatoire")
    private Long planteId;

    private Double quantite;

    private String unite;
}
