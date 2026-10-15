package com.pharmacopee.ladafura.dto.admin.collecte;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de mise à jour complète d'une fiche de collecte par l'administrateur")
public class AdminUpdateCollecteRequest {

    @Schema(description = "Date et heure de la collecte sur le terrain", example = "2026-09-28T10:30:00")
    private LocalDateTime dateCollecte;

    @Schema(description = "Observations et description de terrain recueillies", example = "Spécimen de feuilles récoltées près de Sikasso.")
    private String description;

    @Schema(description = "URL de la photo du spécimen")
    private String photoUrl;

    @Schema(description = "URL de l'enregistrement audio")
    private String audioUrl;

    // Source (Praticien / Informateur)
    @Schema(description = "ID d'une source existante à rattacher")
    private Long sourceId;

    @Schema(description = "Nom complet de la source (pour mise à jour ou création rapide)")
    private String sourceNomComplet;

    @Schema(description = "Spécialité ou titre de la source", example = "Tradipraticien herboriste")
    private String sourceSpecialite;

    @Schema(description = "Numéro de téléphone de la source", example = "+223 76 00 00 00")
    private String sourceTelephone;

    @Schema(description = "Adresse de consultation de la source")
    private String sourceAdresse;

    // Localisation
    @Schema(description = "ID d'une localisation existante")
    private Long localisationId;

    @Schema(description = "Région administrative", example = "Sikasso")
    private String region;

    @Schema(description = "Cercle", example = "Kadiolo")
    private String cercle;

    @Schema(description = "Commune", example = "Finkolo")
    private String commune;

    @Schema(description = "Localité / Village", example = "Finkolo")
    private String localite;

    @Schema(description = "Latitude GPS", example = "11.2345")
    private Double latitude;

    @Schema(description = "Longitude GPS", example = "-5.6789")
    private Double longitude;

    // Connaissances traditionnelles / Vertus
    @Schema(description = "Liste des vertus et connaissances traditionnelles à mettre à jour ou ajouter")
    private List<AdminVertuItemRequest> vertus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminVertuItemRequest {
        private Long id;
        private Long planteId;
        private String usageRapporte;
        private String partieUtilisee;
        private String preparation;
        private String precaution;
        private String description;
    }
}

