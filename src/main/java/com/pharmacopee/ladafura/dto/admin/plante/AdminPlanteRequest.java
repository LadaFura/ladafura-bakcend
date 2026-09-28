package com.pharmacopee.ladafura.dto.admin.plante;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.pharmacopee.ladafura.enums.StatutPlante;

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
public class AdminPlanteRequest {

    @NotBlank(message = "Le nom scientifique est obligatoire")
    private String nomScientifique;

    private String description;

    private String photoUrl;

    @Builder.Default
    private StatutPlante statut = StatutPlante.BROUILLON;

    @Builder.Default
    private List<NomPlanteDto> nomsVernaculaires = new ArrayList<>();

    @Builder.Default
    private Set<Long> maladieIds = new HashSet<>();
}
