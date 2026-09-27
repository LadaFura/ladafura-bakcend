package com.pharmacopee.ladafura.dto.admin.plante;

import com.pharmacopee.ladafura.enums.StatutPlante;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPlanteResponse {

    private Long id;
    private String nomScientifique;
    private String description;
    private String photoUrl;
    private StatutPlante statut;

    @Builder.Default
    private List<NomPlanteDto> nomsVernaculaires = new ArrayList<>();

    @Builder.Default
    private List<String> maladies = new ArrayList<>();

    private Integer nbEtudes;
    private Integer nbVertus;
}
