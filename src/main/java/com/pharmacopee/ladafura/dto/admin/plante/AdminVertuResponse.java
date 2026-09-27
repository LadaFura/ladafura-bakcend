package com.pharmacopee.ladafura.dto.admin.plante;

import com.pharmacopee.ladafura.enums.StatutValidation;
import lombok.*;

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
}
