package com.pharmacopee.ladafura.dto.admin.user;

import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChangeStatusRequest {

    @NotNull(message = "Le statut est obligatoire")
    private StatutUtilisateur statut;

    private String motif;
}
