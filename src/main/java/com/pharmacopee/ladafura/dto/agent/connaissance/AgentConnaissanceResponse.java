package com.pharmacopee.ladafura.dto.agent.connaissance;

import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutValidation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Fiche descriptive d'une connaissance traditionnelle recueillie sur le terrain")
public class AgentConnaissanceResponse {

    @Schema(description = "Identifiant unique de l'enregistrement de connaissance traditionnelle (Vertu)", example = "10")
    private Long id;

    @Schema(description = "Identifiant de la fiche de collecte de rattachement", example = "12")
    private Long collecteId;

    @Schema(description = "Identifiant de la plante concernée", example = "5")
    private Long planteId;

    @Schema(description = "Nom scientifique de la plante", example = "Combretum micranthum")
    private String planteNomScientifique;

    @Builder.Default
    @Schema(description = "Noms vernaculaires de la plante", example = "[\"Kinkéliba (Bambara)\", \"Sekeu (Soninké)\"]")
    private List<String> nomsVernaculaires = new ArrayList<>();

    @Schema(description = "Usage traditionnel rapporté sur le terrain", example = "Utilisé traditionnellement en infusion pour soulager les fièvres paludéennes.")
    private String usageRapporte;

    @Schema(description = "Partie ou organe de la plante utilisé", example = "Feuilles séchées")
    private String partieUtilisee;

    @Schema(description = "Mode de préparation traditionnel", example = "Décoction de 20g dans 1L d'eau")
    private String preparation;

    @Schema(description = "Précautions d'emploi ou contre-indications rapportées", example = "Déconseillé aux femmes enceintes")
    private String precaution;

    @Schema(description = "Observations de terrain complémentaires", example = "Recueilli auprès du praticien traditionnel de Finkolo")
    private String description;

    @Schema(description = "Statut de modération de la connaissance (BROUILLON, EN_ATTENTE, VALIDE, REJETE)", example = "BROUILLON")
    private StatutValidation statut;

    // Informations Source (Thérapeute / Herboriste)
    @Schema(description = "Identifiant de la source interrogée", example = "3")
    private Long sourceId;

    @Schema(description = "Nom complet de la source", example = "Amadou Diarra")
    private String sourceNomComplet;

    @Schema(description = "Spécialité de la source", example = "Herboriste traditionnel")
    private String sourceSpecialite;

    // Maladies / Symptômes associés
    @Builder.Default
    @Schema(description = "Maladies ou symptômes ciblés par la plante selon le savoir traditionnel rapporté", example = "[\"Paludisme\", \"Maux d'estomac\"]")
    private List<String> maladiesAssociees = new ArrayList<>();

    // Clause explicite de distinction avec les études scientifiques
    @Builder.Default
    @Schema(description = "Atteste s'il s'agit d'une preuve scientifique validée. Toujours false pour un savoir traditionnel empirique de terrain.", example = "false")
    private boolean preuveScientifique = false;

    @Builder.Default
    @Schema(description = "Type d'information enregistrée", example = "CONNAISSANCE_TRADITIONNELLE")
    private String typeInformation = "CONNAISSANCE_TRADITIONNELLE";

    @Builder.Default
    @Schema(description = "Avertissement légal et éthique", example = "Information issue du savoir traditionnel local rapporté sur le terrain. Ne constitue ni une preuve clinique ou scientifique, ni une prescription médicale.")
    private String avertissementLegal = "Information issue du savoir traditionnel local rapporté sur le terrain. Ne constitue ni une preuve clinique ou scientifique, ni une prescription médicale.";
}
