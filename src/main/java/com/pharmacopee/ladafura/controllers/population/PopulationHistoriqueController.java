package com.pharmacopee.ladafura.controllers.population;

import java.time.LocalDateTime;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.population.historique.PopulationDepensesSyntheseResponse;
import com.pharmacopee.ladafura.dto.population.historique.PopulationJournalActiviteItem;
import com.pharmacopee.ladafura.dto.population.historique.PopulationProduitAcheteItem;
import com.pharmacopee.ladafura.dto.population.historique.PopulationReleveActiviteResponse;
import com.pharmacopee.ladafura.enums.TypeEvenementHistorique;
import com.pharmacopee.ladafura.services.interfaces.IPopulationHistoriqueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/historique")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_POPULATION')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Population - Historique & Activité", description = "Endpoints de traçabilité, journal d'activité consolidé, analyse des dépenses et historique d'achats")
public class PopulationHistoriqueController {

    private final IPopulationHistoriqueService historiqueService;

    @GetMapping("/activite")
    @Operation(summary = "Consulter le journal chronologique d'activité du compte",
               description = "Renvoie le flux d'événements unifié (commandes, paiements, avis déposés, favoris, notifications) avec filtres par type et période.")
    @ApiResponse(responseCode = "200", description = "Journal d'activité récupéré avec succès")
    public ResponseEntity<Page<PopulationJournalActiviteItem>> getJournalActivite(
            @Parameter(description = "Filtrer par type d'événement (COMMANDE, PAIEMENT, AVIS, FAVORI, NOTIFICATION)")
            @RequestParam(required = false) TypeEvenementHistorique type,
            @Parameter(description = "Date de début (format ISO: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @Parameter(description = "Date de fin (format ISO: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        log.info("Consultation journal d'activité (type={}, dateDebut={}, dateFin={})", type, dateDebut, dateFin);
        return ResponseEntity.ok(historiqueService.getJournalActivite(type, dateDebut, dateFin, pageable));
    }

    @GetMapping("/depenses")
    @Operation(summary = "Consulter la synthèse analytique des dépenses financières",
               description = "Renvoie le montant total dépensé, le panier moyen, l'évolution mensuelle et la répartition par mode de paiement.")
    @ApiResponse(responseCode = "200", description = "Synthèse financière calculée avec succès")
    public ResponseEntity<PopulationDepensesSyntheseResponse> getSyntheseDepenses() {
        log.info("Consultation de la synthèse analytique des dépenses");
        return ResponseEntity.ok(historiqueService.getSyntheseDepenses());
    }

    @GetMapping("/produits-achetes")
    @Operation(summary = "Consulter l'historique des produits achetés et reçus",
               description = "Renvoie la liste consolidée des produits distincts commandés et livrés/retirés, avec cumul des quantités, montants et statut d'évaluation.")
    @ApiResponse(responseCode = "200", description = "Historique des produits achetés récupéré avec succès")
    public ResponseEntity<Page<PopulationProduitAcheteItem>> getHistoriqueProduitsAchetes(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        log.info("Consultation de l'historique des produits achetés");
        return ResponseEntity.ok(historiqueService.getHistoriqueProduitsAchetes(pageable));
    }

    @GetMapping("/releve")
    @Operation(summary = "Générer un relevé consolidé d'activité du compte client",
               description = "Renvoie une synthèse globale du compte (commandes, dépenses, avis, favoris) et les 10 dernières actions pour relevé ou attestation.")
    @ApiResponse(responseCode = "200", description = "Relevé généré avec succès")
    public ResponseEntity<PopulationReleveActiviteResponse> getReleveActivite() {
        log.info("Génération du relevé d'activité du compte");
        return ResponseEntity.ok(historiqueService.getReleveActivite());
    }
}
