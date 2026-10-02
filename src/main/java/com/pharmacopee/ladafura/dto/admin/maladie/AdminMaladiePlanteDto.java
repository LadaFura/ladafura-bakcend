package com.pharmacopee.ladafura.dto.admin.maladie;

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
public class AdminMaladiePlanteDto {
    private Long id;
    private String nomScientifique;
    private String nomVernaculaire;
}
