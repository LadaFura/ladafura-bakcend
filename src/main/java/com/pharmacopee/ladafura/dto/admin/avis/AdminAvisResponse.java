package com.pharmacopee.ladafura.dto.admin.avis;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutAvis;

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
public class AdminAvisResponse {

    private Long id;
    private Integer note;
    private String commentaire;
    private LocalDateTime dateAvis;
    private StatutAvis statut;

    private Long utilisateurId;
    private String nomCompletUtilisateur;
    private String emailUtilisateur;

    private Long pharmacopeeId;
    private String nomPharmacopee;
    private String reponseOfficine;
    private LocalDateTime dateReponse;
}
