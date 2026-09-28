package com.pharmacopee.ladafura.dto.admin.avis;

import com.pharmacopee.ladafura.enums.StatutAvis;

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
public class AdminModerateAvisRequest {

    @NotNull(message = "L'action de modération (PUBLIE, MASQUE, REJETE) est obligatoire")
    private StatutAvis action;

    private String motif;
}
