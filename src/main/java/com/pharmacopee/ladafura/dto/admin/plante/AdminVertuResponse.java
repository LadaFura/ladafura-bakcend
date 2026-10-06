package com.pharmacopee.ladafura.dto.admin.plante;

import com.pharmacopee.ladafura.enums.StatutValidation;

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
public class AdminVertuResponse {

    private Long id;
    private String usageRapporte;
    private String partieUtilisee;
    private String preparation;
    private String precaution;
    private String description;
    private StatutValidation statut;

    private Long planteId;
    private String planteNomScientifique;

    private Long collecteId;
    private String sourceNomComplet;
    private String sourceSpecialite;
    private String localisation;
}
