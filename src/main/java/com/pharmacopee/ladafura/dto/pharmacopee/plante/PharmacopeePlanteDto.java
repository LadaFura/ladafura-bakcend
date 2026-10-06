package com.pharmacopee.ladafura.dto.pharmacopee.plante;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PharmacopeePlanteDto {
    private Long id;
    private String nomScientifique;
    private String nomVulgaire; // A comma separated string of local names
}
