package com.pharmacopee.ladafura.dto.admin.produit;

import com.pharmacopee.ladafura.enums.StatutProduit;

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
public class AdminModerateProduitRequest {

    @NotNull(message = "L'action de modération (VALIDE, REJETE, ARCHIVE) est obligatoire")
    private StatutProduit action;

    private String motif;
}
