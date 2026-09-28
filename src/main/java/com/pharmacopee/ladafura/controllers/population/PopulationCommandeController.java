package com.pharmacopee.ladafura.controllers.population;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeDetailResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeRecapitulatifRequest;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeRecapitulatifResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeStatutResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCreateCommandeRequest;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.services.interfaces.IPopulationCommandeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/commandes")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_POPULATION')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Population - Commandes", description = "Endpoints de passage de commande, simulation de récapitulatif, suivi du statut et historique")
public class PopulationCommandeController {

    private final IPopulationCommandeService populationCommandeService;

    @PostMapping("/recapitulatif")
    @Operation(summary = "Consulter le récapitulatif avant confirmation de la commande",
               description = "Simule la commande à partir du panier actif et vérifie la disponibilité des stocks et les frais applicables sans persister la commande.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Récapitulatif généré avec succès"),
        @ApiResponse(responseCode = "400", description = "Panier vide, produit indisponible ou mode de retrait invalide"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Pharmacopée ou mode de retrait introuvable")
    })
    public ResponseEntity<PopulationCommandeRecapitulatifResponse> getRecapitulatif(
            @Valid @RequestBody PopulationCommandeRecapitulatifRequest request) {
        log.info("Requête POST /api/v1/population/commandes/recapitulatif reçue");
        return ResponseEntity.ok(populationCommandeService.getRecapitulatif(request));
    }

    @PostMapping
    @Operation(summary = "Passer et confirmer définitivement une commande",
               description = "Transforme le panier actif en commande officielle, décrémente les stocks en officine et vide le panier de l'utilisateur.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Commande passée et enregistrée avec succès"),
        @ApiResponse(responseCode = "400", description = "Erreur de validation des données, stock insuffisant ou adresse de livraison manquante"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Ressource introuvable")
    })
    public ResponseEntity<PopulationCommandeDetailResponse> passerCommande(
            @Valid @RequestBody PopulationCreateCommandeRequest request) {
        log.info("Requête POST /api/v1/population/commandes reçue");
        PopulationCommandeDetailResponse response = populationCommandeService.passerCommande(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Consulter l'historique de ses commandes",
               description = "Retourne la liste paginée des commandes passées par l'utilisateur connecté, triées par date décroissante.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historique des commandes récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<Page<PopulationCommandeSummaryResponse>> getHistorique(
            @Parameter(description = "Filtrer optionnellement par statut de commande")
            @RequestParam(required = false) StatutCommande statut,
            @PageableDefault(size = 10, sort = "dateCommande", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Requête GET /api/v1/population/commandes reçue (statut={})", statut);
        return ResponseEntity.ok(populationCommandeService.getHistoriqueCommandes(statut, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le détail complet d'une commande",
               description = "Fournit les informations détaillées, les articles commandés, le mode de retrait et les coordonnées de la pharmacopée.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Détail de la commande récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Commande introuvable ou n'appartenant pas à l'utilisateur")
    })
    public ResponseEntity<PopulationCommandeDetailResponse> getCommandeDetail(@PathVariable Long id) {
        log.info("Requête GET /api/v1/population/commandes/{} reçue", id);
        return ResponseEntity.ok(populationCommandeService.getCommandeDetail(id));
    }

    @GetMapping("/{id}/statut")
    @Operation(summary = "Suivre en direct le statut d'une commande",
               description = "Renvoie l'état d'avancement synthétique avec un message explicatif adapté au statut actuel.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statut de la commande récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Commande introuvable")
    })
    public ResponseEntity<PopulationCommandeStatutResponse> getCommandeStatut(@PathVariable Long id) {
        log.info("Requête GET /api/v1/population/commandes/{}/statut reçue", id);
        return ResponseEntity.ok(populationCommandeService.getCommandeStatut(id));
    }

    @PostMapping("/{id}/annuler")
    @Operation(summary = "Annuler une commande en cours",
               description = "Permet au client d'annuler sa commande tant qu'elle est au statut EN_ATTENTE ou CONFIRMEE. Réintègre les stocks automatiquement.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Commande annulée avec succès"),
        @ApiResponse(responseCode = "400", description = "La commande ne peut plus être annulée (déjà préparée, expédiée ou annulée)"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Commande introuvable")
    })
    public ResponseEntity<PopulationCommandeDetailResponse> annulerCommande(@PathVariable Long id) {
        log.info("Requête POST /api/v1/population/commandes/{}/annuler reçue", id);
        return ResponseEntity.ok(populationCommandeService.annulerCommande(id));
    }
}
