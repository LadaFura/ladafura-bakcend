package com.pharmacopee.ladafura.dto.pharmacopee.membre;

import java.time.LocalDateTime;
import java.util.List;

import com.pharmacopee.ladafura.dto.pharmacopee.role.RoleResumeResponse;
import com.pharmacopee.ladafura.enums.MotifSuspensionMembre;
import com.pharmacopee.ladafura.enums.StatutMembre;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Membre d'une pharmacopée. Tant qu'il est INVITE, seul un contact masqué est exposé.")
public class MembreResponse {
    private Long id;
    private Long utilisateurId;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    @Schema(description = "Email masqué, renseigné seulement pour un membre INVITE", example = "a***@gmail.com")
    private String contactMasque;
    private StatutMembre statut;
    private MotifSuspensionMembre motifSuspension;
    private Boolean estProprietaire;
    private String poste;
    private LocalDateTime dateInvitation;
    private LocalDateTime dateExpirationInvitation;
    private LocalDateTime dateAdhesion;
    private LocalDateTime dateDepart;
    private List<RoleResumeResponse> roles;
}
