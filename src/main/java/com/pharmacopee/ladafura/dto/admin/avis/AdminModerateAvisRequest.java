package com.pharmacopee.ladafura.dto.admin.avis;

import com.pharmacopee.ladafura.enums.StatutAvis;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminModerateAvisRequest {

    @NotNull(message = "L'action de modération (PUBLIE, MASQUE, REJETE) est obligatoire")
    private StatutAvis action;

    private String motif;
}
