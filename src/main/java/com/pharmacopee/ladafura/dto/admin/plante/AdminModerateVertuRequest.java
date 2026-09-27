package com.pharmacopee.ladafura.dto.admin.plante;

import com.pharmacopee.ladafura.enums.StatutValidation;

import jakarta.validation.constraints.NotNull;
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
public class AdminModerateVertuRequest {

    @NotNull(message = "L'action de validation (VALIDE ou REJETE) est obligatoire")
    private StatutValidation action;

    private String motif;
}
