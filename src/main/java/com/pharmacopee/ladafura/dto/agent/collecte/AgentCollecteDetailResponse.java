package com.pharmacopee.ladafura.dto.agent.collecte;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutCollecte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Détail complet d'une fiche de collecte et de son cycle de validation")
public class AgentCollecteDetailResponse {

    @Schema(description = "Identifiant unique de la collecte", example = "12")
    private Long id;

    @Schema(description = "Date de réalisation de la collecte sur le terrain", example = "2026-09-28T10:30:00")
    private LocalDateTime dateCollecte;

    @Schema(description = "Description détaillée des observations", example = "Observation de terrain sur les feuilles et écorces récoltées près de Finkolo.")
    private String description;

    @Schema(description = "Statut actuel dans le workflow de modération", example = "BROUILLON")
    private StatutCollecte statut;

    @Schema(description = "URL de la photo de l'échantillon ou de la plante", example = "https://storage.ladafura.ml/collectes/photos/img_12.jpg")
    private String photoUrl;

    @Schema(description = "URL de l'enregistrement audio de témoignage", example = "https://storage.ladafura.ml/collectes/audios/rec_12.mp3")
    private String audioUrl;

    @Schema(description = "Date et heure de soumission pour validation", example = "2026-09-28T14:00:00")
    private LocalDateTime dateSoumission;

    @Schema(description = "Motif communiqué par l'administrateur en cas de rejet", example = "Photos trop floues, merci de téléverser une prise de vue macro.")
    private String motifRejet;

    // Informations Agent
    @Schema(description = "Identifiant de l'agent créateur", example = "4")
    private Long agentId;

    @Schema(description = "Matricule de l'agent", example = "AGT-2026-004")
    private String agentMatricule;

    @Schema(description = "Nom complet de l'agent de collecte", example = "Oumar Coulibaly")
    private String agentNomComplet;

    // Informations Source (si associée)
    @Schema(description = "Identifiant de la source interrogée", example = "3")
    private Long sourceId;

    @Schema(description = "Nom complet de la source (thérapeute / herboriste)", example = "Amadou Diarra")
    private String sourceNomComplet;

    @Schema(description = "Spécialité ou domaine de compétence de la source", example = "Herboriste traditionnel")
    private String sourceSpecialite;

    @Schema(description = "Numéro de contact de la source", example = "+223 70 22 33 44")
    private String sourceTelephone;

    // Informations Localisation (si associée)
    @Schema(description = "Identifiant de la localisation géographique", example = "5")
    private Long localisationId;

    @Schema(description = "Région administrative", example = "Sikasso")
    private String region;

    @Schema(description = "Cercle administratif", example = "Koutiala")
    private String cercle;

    @Schema(description = "Commune administrative", example = "Finkolo")
    private String commune;

    @Schema(description = "Localité ou village de recueil", example = "Finkolo Centre")
    private String localite;

    @Schema(description = "Coordonnée latitude GPS", example = "12.3812")
    private Double latitude;

    @Schema(description = "Coordonnée longitude GPS", example = "-5.4590")
    private Double longitude;

    @Schema(description = "Nombre de vertus ou connaissances rattachées à cette fiche", example = "2")
    private int nombreVertus;

    @Schema(description = "Indique si la collecte est encore modifiable par l'agent (statut BROUILLON ou REJETEE)", example = "true")
    private boolean modifiable;

    @Schema(description = "Indique si la collecte peut être soumise à validation (statut BROUILLON ou REJETEE)", example = "true")
    private boolean soumissible;
}
