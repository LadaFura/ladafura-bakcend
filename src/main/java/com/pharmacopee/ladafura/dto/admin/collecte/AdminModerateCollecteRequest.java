package com.pharmacopee.ladafura.dto.admin.collecte;

import com.pharmacopee.ladafura.enums.StatutCollecte;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminModerateCollecteRequest {

    @NotNull(message = "L'action de modération (VALIDEE ou REJETEE) est obligatoire")
    private StatutCollecte action;

    private String motifRejet;
}
