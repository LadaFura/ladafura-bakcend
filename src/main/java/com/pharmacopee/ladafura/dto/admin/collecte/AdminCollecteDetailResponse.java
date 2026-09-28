package com.pharmacopee.ladafura.dto.admin.collecte;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.dto.admin.plante.AdminVertuResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;

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
public class AdminCollecteDetailResponse {

    private Long id;
    private LocalDateTime dateCollecte;
    private String description;
    private StatutCollecte statut;
    private String photoUrl;
    private String audioUrl;
    private LocalDateTime dateSoumission;
    private String motifRejet;

    // Informations Agent
    private Long agentId;
    private String agentNomComplet;
    private String agentMatricule;
    private String agentTelephone;

    // Informations Source (Thérapeute / Herboriste)
    private Long sourceId;
    private String sourceNomComplet;
    private String sourceSpecialite;
    private String sourceTelephone;
    private String sourceAdresse;

    // Localisation géographique
    private String region;
    private String cercle;
    private String commune;
    private String localite;
    private Double latitude;
    private Double longitude;

    // Connaissances traditionnelles documentées
    @Builder.Default
    private List<AdminVertuResponse> vertus = new ArrayList<>();
}
