package com.pharmacopee.ladafura.dto.admin.pharmacopee;

import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPharmacopeeDetailResponse {

    private Long id;
    private String nom;
    private String description;
    private String telephone;
    private StatutPharmacopee statut;

    // Localisation géographique
    private String region;
    private String cercle;
    private String commune;
    private String localite;
    private Double latitude;
    private Double longitude;

    // Compte propriétaire
    private Long utilisateurId;
    private String nomCompletProprietaire;
    private String emailProprietaire;

    // Paramètres logistiques
    @Builder.Default
    private List<AdminModeRetraitResponse> modesRetrait = new ArrayList<>();

    private Integer nbProduits;
    private Integer nbCommandes;
}
