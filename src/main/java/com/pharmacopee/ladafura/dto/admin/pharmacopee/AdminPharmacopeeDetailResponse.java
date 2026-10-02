package com.pharmacopee.ladafura.dto.admin.pharmacopee;

import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutPharmacopee;

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

    // Praticiens affectés (Praticien Principal + Collaborateurs)
    @Builder.Default
    private List<AdminPraticienAffilieResponse> praticiens = new ArrayList<>();

    private Integer nbPraticiens;
    private Integer nbProduits;
    private Integer nbCommandes;
}
