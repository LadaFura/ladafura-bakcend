package com.pharmacopee.ladafura.dto.agent.submission;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
@Schema(description = "Dossier récapitulatif complet d'une collecte avant soumission pour validation")
public class AgentCollecteRecapitulatifResponse {

    // 1. Généralités Collecte
    @Schema(description = "Identifiant de la fiche de collecte", example = "12")
    private Long collecteId;

    @Schema(description = "Date de réalisation de la collecte sur le terrain", example = "2026-09-28T10:30:00")
    private LocalDateTime dateCollecte;

    @Schema(description = "Description générale des observations", example = "Mission de collecte dans le cercle de Koutiala.")
    private String description;

    @Schema(description = "Statut actuel de la fiche", example = "BROUILLON")
    private StatutCollecte statut;

    @Schema(description = "Nom complet de l'agent créateur", example = "Oumar Coulibaly")
    private String agentNomComplet;

    // 2. Synthèse Plante & Botanique
    @Schema(description = "Indique si au moins une plante est associée à la collecte", example = "true")
    private boolean planteAssociee;

    @Schema(description = "Identifiant de la plante principale", example = "3")
    private Long planteId;

    @Schema(description = "Nom scientifique de la plante", example = "Combretum micranthum")
    private String planteNomScientifique;

    @Builder.Default
    @Schema(description = "Noms vernaculaires recueillis pour la plante", example = "[\"Kinkéliba (Bambara)\", \"Sekeu (Soninké)\"]")
    private List<String> nomsVernaculaires = new ArrayList<>();

    // 3. Synthèse Connaissances Traditionnelles
    @Schema(description = "Indique si une connaissance traditionnelle a été renseignée", example = "true")
    private boolean connaissanceRenseignee;

    @Schema(description = "Nombre de connaissances traditionnelles (vertus) documentées", example = "1")
    private int nombreConnaissances;

    @Builder.Default
    @Schema(description = "Usages traditionnels rapportés sur le terrain", example = "[\"Traitement traditionnel des fièvres palustres\"]")
    private List<String> usagesRapportes = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Parties de plantes utilisées", example = "[\"Feuilles séchées\"]")
    private List<String> partiesUtilisees = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Modes de préparation rapportés", example = "[\"Décoction 15 min\"]")
    private List<String> modesPreparation = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Précautions d'emploi et contre-indications", example = "[\"Déconseillé aux femmes enceintes\"]")
    private List<String> precautions = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Maladies ou symptômes ciblés selon la tradition", example = "[\"Paludisme\"]")
    private List<String> maladiesAssociees = new ArrayList<>();

    // 4. Synthèse Source (Thérapeute / Herboriste)
    @Schema(description = "Indique si une source d'information de terrain est rattachée", example = "true")
    private boolean sourceRattachee;

    @Schema(description = "Identifiant de la source", example = "5")
    private Long sourceId;

    @Schema(description = "Nom complet de la source", example = "Amadou Diarra")
    private String sourceNomComplet;

    @Schema(description = "Spécialité de la source", example = "Herboriste traditionnel")
    private String sourceSpecialite;

    // 5. Synthèse Médias (Photos / Audios)
    @Schema(description = "Indique si au moins un média (photo ou audio) est présent", example = "true")
    private boolean mediasPresents;

    @Schema(description = "URL de la photo d'échantillon", example = "/uploads/collectes/photos/kinkeliba.jpg")
    private String photoUrl;

    @Schema(description = "URL de l'enregistrement audio de témoignage", example = "/uploads/collectes/audios/temoignage.mp3")
    private String audioUrl;

    @Schema(description = "Indique si une photo est présente", example = "true")
    private boolean photoPresente;

    @Schema(description = "Indique si un enregistrement audio est présent", example = "true")
    private boolean audioPresent;

    // 6. Synthèse Localisation
    @Schema(description = "Indique si la localisation géographique est définie", example = "true")
    private boolean localisationRenseignee;

    @Schema(description = "Région administrative", example = "Sikasso")
    private String region;

    @Schema(description = "Cercle administratif", example = "Koutiala")
    private String cercle;

    @Schema(description = "Commune administrative", example = "Finkolo")
    private String commune;

    @Schema(description = "Localité ou village", example = "Finkolo Centre")
    private String localite;

    @Schema(description = "Coordonnée latitude GPS", example = "12.3812")
    private Double latitude;

    @Schema(description = "Coordonnée longitude GPS", example = "-5.4590")
    private Double longitude;

    @Schema(description = "Adresse complète formatée", example = "Finkolo Centre, Finkolo, Koutiala, Sikasso")
    private String adresseFormatee;

    // 7. Bilan de Conformité & Checklist de Soumission
    @Schema(description = "Atteste si la collecte remplit tous les critères obligatoires pour soumission", example = "true")
    private boolean conformePourSoumission;

    @Builder.Default
    @Schema(description = "Liste des erreurs bloquantes empêchant la soumission", example = "[]")
    private List<String> erreursBloquantes = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Points de vigilance ou recommandations non bloquantes", example = "[\"Aucun enregistrement audio n'a été joint\"]")
    private List<String> pointsDeVigilance = new ArrayList<>();

    @Schema(description = "Prochaine étape conseillée dans le workflow", example = "SOUMISSION_AUTORISEE")
    private String prochaineEtape;
}
