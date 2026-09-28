package com.pharmacopee.ladafura.dto.agent.vernaculaire;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Données pour l'enregistrement ou la mise à jour d'un nom vernaculaire recueilli sur le terrain")
public class AgentNomVernaculaireRequest {

    @NotBlank(message = "Le nom vernaculaire est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom vernaculaire doit comporter entre 2 et 100 caractères")
    @Schema(description = "Nom vernaculaire ou appellation locale", example = "Kinkéliba")
    private String nom;

    @NotBlank(message = "La langue locale est obligatoire")
    @Size(min = 2, max = 50, message = "La langue doit comporter entre 2 et 50 caractères")
    @Schema(description = "Langue locale ou dialecte utilisé", example = "Bambara")
    private String langue;

    @Builder.Default
    @Size(max = 50, message = "Le nom du pays ne doit pas dépasser 50 caractères")
    @Schema(description = "Pays d'usage du nom vernaculaire", example = "Mali")
    private String pays = "Mali";
}
