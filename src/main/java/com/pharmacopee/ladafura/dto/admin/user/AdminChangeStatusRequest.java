package com.pharmacopee.ladafura.dto.admin.user;

import com.pharmacopee.ladafura.enums.StatutUtilisateur;

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
public class AdminChangeStatusRequest {

    @NotNull(message = "Le statut est obligatoire")
    private StatutUtilisateur statut;

    private String motif;
}
