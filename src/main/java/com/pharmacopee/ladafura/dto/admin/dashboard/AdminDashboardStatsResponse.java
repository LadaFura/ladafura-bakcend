package com.pharmacopee.ladafura.dto.admin.dashboard;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardStatsResponse {

    // Utilisateurs & Écosystème
    private Long totalUtilisateurs;
    private Long nbAgentsCollecte;
    private Long nbSources;
    private Long nbPharmacopees;
    private Long nbCitoyens;

    // Base de connaissances
    private Long totalPlantes;
    private Long totalMaladies;
    private Long totalEtudesScientifiques;

    // Alertes de modération & workflow
    private Long collectesEnAttente;
    private Long collectesValidees;
    private Long pharmacopeesEnAttente;
    private Long produitsEnAttente;
    private Long avisEnAttente;
}
