package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.LignePanier;
import com.pharmacopee.ladafura.Models.Panier;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.panier.PopulationAddToCartRequest;
import com.pharmacopee.ladafura.dto.population.panier.PopulationPanierResponse;
import com.pharmacopee.ladafura.dto.population.panier.PopulationUpdateQuantityRequest;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationPanierMapper;
import com.pharmacopee.ladafura.repository.LignePanierRepository;
import com.pharmacopee.ladafura.repository.PanierRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationPanierService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PopulationPanierServiceImpl implements IPopulationPanierService {

    private final IPopulationAuthService populationAuthService;
    private final PanierRepository panierRepository;
    private final LignePanierRepository lignePanierRepository;
    private final ProduitRepository produitRepository;
    private final PopulationPanierMapper mapper;

    private Panier getOrCreatePanier(Utilisateur user) {
        return panierRepository.findByUtilisateurId(user.getId())
                .orElseGet(() -> {
                    log.info("Création d'un panier actif pour l'utilisateur ID: {}", user.getId());
                    Panier newPanier = Panier.builder()
                            .utilisateur(user)
                            .dateCreation(LocalDateTime.now())
                            .dateModification(LocalDateTime.now())
                            .lignes(new ArrayList<>())
                            .build();
                    return panierRepository.save(newPanier);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public PopulationPanierResponse getPanier() {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation du panier pour l'utilisateur ID: {}", user.getId());

        Panier panier = panierRepository.findByUtilisateurId(user.getId())
                .orElseGet(() -> Panier.builder()
                        .utilisateur(user)
                        .dateCreation(LocalDateTime.now())
                        .dateModification(LocalDateTime.now())
                        .lignes(new ArrayList<>())
                        .build());

        return mapper.toPanierResponse(panier);
    }

    @Override
    public PopulationPanierResponse ajouterProduitAuPanier(PopulationAddToCartRequest request) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Ajout du produit ID: {} (quantité: {}) au panier de l'utilisateur ID: {}",
                request.getProduitId(), request.getQuantite(), user.getId());

        Produit produit = produitRepository.findByIdAndStatut(request.getProduitId(), StatutProduit.VALIDE)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", request.getProduitId()));

        Panier panier = getOrCreatePanier(user);

        Optional<LignePanier> existingLigne = lignePanierRepository.findByPanierIdAndProduitId(panier.getId(), produit.getId());

        if (existingLigne.isPresent()) {
            LignePanier ligne = existingLigne.get();
            int nouvelleQuantite = ligne.getQuantite() + request.getQuantite();
            ligne.setQuantite(nouvelleQuantite);
            ligne.setSousTotal(ligne.getPrixUnitaire() * nouvelleQuantite);
            lignePanierRepository.save(ligne);
            log.info("Quantité incrémentée à {} pour le produit ID: {} dans le panier ID: {}",
                    nouvelleQuantite, produit.getId(), panier.getId());
        } else {
            double prixUnitaire = produit.getPrix() != null ? produit.getPrix() : 0.0;
            LignePanier nouvelleLigne = LignePanier.builder()
                    .panier(panier)
                    .produit(produit)
                    .quantite(request.getQuantite())
                    .prixUnitaire(prixUnitaire)
                    .sousTotal(prixUnitaire * request.getQuantite())
                    .build();
            panier.getLignes().add(nouvelleLigne);
            lignePanierRepository.save(nouvelleLigne);
            log.info("Nouvelle ligne créée pour le produit ID: {} dans le panier ID: {}",
                    produit.getId(), panier.getId());
        }

        panier.setDateModification(LocalDateTime.now());
        panierRepository.save(panier);

        return mapper.toPanierResponse(panier);
    }

    @Override
    public PopulationPanierResponse modifierQuantiteLigne(Long ligneId, PopulationUpdateQuantityRequest request) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Modification de la quantité de la ligne ID: {} (nouvelle quantité: {}) pour l'utilisateur ID: {}",
                ligneId, request.getQuantite(), user.getId());

        Panier panier = getOrCreatePanier(user);

        LignePanier ligne = lignePanierRepository.findByIdAndPanierId(ligneId, panier.getId())
                .orElseThrow(() -> new ResourceNotFoundException("LignePanier", "id", ligneId));

        if (request.getQuantite() <= 0) {
            panier.getLignes().remove(ligne);
            lignePanierRepository.delete(ligne);
            log.info("Ligne ID: {} supprimée du panier (quantité <= 0)", ligneId);
        } else {
            ligne.setQuantite(request.getQuantite());
            ligne.setSousTotal(ligne.getPrixUnitaire() * request.getQuantite());
            lignePanierRepository.save(ligne);
        }

        panier.setDateModification(LocalDateTime.now());
        panierRepository.save(panier);

        return mapper.toPanierResponse(panier);
    }

    @Override
    public PopulationPanierResponse supprimerLignePanier(Long ligneId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Suppression de la ligne ID: {} du panier pour l'utilisateur ID: {}", ligneId, user.getId());

        Panier panier = getOrCreatePanier(user);

        LignePanier ligne = lignePanierRepository.findByIdAndPanierId(ligneId, panier.getId())
                .orElseThrow(() -> new ResourceNotFoundException("LignePanier", "id", ligneId));

        panier.getLignes().remove(ligne);
        lignePanierRepository.delete(ligne);

        panier.setDateModification(LocalDateTime.now());
        panierRepository.save(panier);

        return mapper.toPanierResponse(panier);
    }

    @Override
    public void viderPanier() {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Vidage complet du panier pour l'utilisateur ID: {}", user.getId());

        Optional<Panier> optionalPanier = panierRepository.findByUtilisateurId(user.getId());
        if (optionalPanier.isPresent()) {
            Panier panier = optionalPanier.get();
            panier.getLignes().clear();
            lignePanierRepository.deleteByPanierId(panier.getId());
            panier.setDateModification(LocalDateTime.now());
            panierRepository.save(panier);
        }
    }
}
