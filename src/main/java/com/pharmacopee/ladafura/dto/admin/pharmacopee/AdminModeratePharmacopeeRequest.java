package com.pharmacopee.ladafura.dto.admin.pharmacopee;

import com.pharmacopee.ladafura.enums.StatutPharmacopee;

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
public class AdminModeratePharmacopeeRequest {

    @NotNull(message = "L'action de modération (VALIDEE, SUSPENDUE, REJETEE) est obligatoire")
    private StatutPharmacopee action;

    private String motif;
}
