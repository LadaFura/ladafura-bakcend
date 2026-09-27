package com.pharmacopee.ladafura.dto.admin.collecte;

import com.pharmacopee.ladafura.enums.StatutCollecte;

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
public class AdminModerateCollecteRequest {

    @NotNull(message = "L'action de modération (VALIDEE ou REJETEE) est obligatoire")
    private StatutCollecte action;

    private String motifRejet;
}
