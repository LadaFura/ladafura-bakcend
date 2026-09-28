package com.pharmacopee.ladafura.dto.agent.plante;

import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.dto.admin.plante.NomPlanteDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
@Schema(description = "Données pour la création d'une nouvelle fiche plante par l'agent de terrain")
public class AgentCreatePlanteRequest {

    @NotBlank(message = "Le nom scientifique de la plante est obligatoire")
    @Size(min = 2, max = 150, message = "Le nom scientifique doit comporter entre 2 et 150 caractères")
    @Schema(description = "Nom scientifique (Genre et espèce)", example = "Combretum micranthum")
    private String nomScientifique;

    @Size(max = 4000, message = "La description ne doit pas dépasser 4000 caractères")
    @Schema(description = "Description des caractères distinctifs observés", example = "Arbuste grimpant très commun dans les savanes et plateaux rocheux.")
    private String description;

    @Schema(description = "URL ou chemin de la photo de référence", example = "https://storage.ladafura.ml/plantes/kinkeliba.jpg")
    private String photoUrl;

    @Builder.Default
    @Schema(description = "Noms vernaculaires locaux recueillis sur le terrain avec langue et pays")
    private List<@Valid NomPlanteDto> nomsVernaculaires = new ArrayList<>();
}
