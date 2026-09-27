package com.pharmacopee.ladafura.dto.admin.avis;

import com.pharmacopee.ladafura.enums.StatutAvis;
import lombok.*;

import java.time.LocalDateTime;

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

    private Long produitId;
    private String nomProduit;
}
