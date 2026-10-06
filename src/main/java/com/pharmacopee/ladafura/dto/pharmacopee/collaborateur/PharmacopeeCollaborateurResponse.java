package com.pharmacopee.ladafura.dto.pharmacopee.collaborateur;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutUtilisateur;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Détails d'un collaborateur rattaché à la pharmacopée")
public class PharmacopeeCollaborateurResponse {

    @Schema(description = "Identifiant du collaborateur", example = "15")
    private Long id;

    @Schema(description = "Nom", example = "Sissoko")
    private String nom;

    @Schema(description = "Prénom", example = "Awa")
    private String prenom;

    @Schema(description = "Email", example = "awa.sissoko@gmail.com")
    private String email;

    @Schema(description = "Téléphone", example = "+223 70 00 00 00")
    private String telephone;

    @Schema(description = "Spécialité ou rôle interne", example = "Assistante")
    private String specialite;

    @Schema(description = "Statut du compte (ACTIF, INACTIF)", example = "ACTIF")
    private StatutUtilisateur statut;

    @Schema(description = "Date d'ajout à l'officine")
    private LocalDateTime dateCreation;
}
