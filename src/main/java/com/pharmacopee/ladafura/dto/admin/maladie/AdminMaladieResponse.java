package com.pharmacopee.ladafura.dto.admin.maladie;

import java.util.ArrayList;
import java.util.List;

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
public class AdminMaladieResponse {

    private Long id;
    private String nom;
    private String description;
    private Integer nbPlantes;

    @Builder.Default
    private List<AdminMaladiePlanteDto> plantes = new ArrayList<>();
}
