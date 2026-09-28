package com.pharmacopee.ladafura.dto.admin.plante;

import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutPlante;

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
public class AdminPlanteResponse {

    private Long id;
    private String nomScientifique;
    private String description;
    private String photoUrl;
    private String image;
    private StatutPlante statut;

    @Builder.Default
    private List<NomPlanteDto> nomsVernaculaires = new ArrayList<>();

    @Builder.Default
    private List<String> maladies = new ArrayList<>();

    private Integer nbEtudes;
    private Integer nbVertus;
}
