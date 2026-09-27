package com.pharmacopee.ladafura.dto.admin.plante;

import com.pharmacopee.ladafura.enums.StatutValidation;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
