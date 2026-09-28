package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.PharmacopeeStockResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.PharmacopeeStockSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.UpdatePrixRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.UpdateStockCompletRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.UpdateStockRequest;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeStockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacopeeStockServiceImpl implements IPharmacopeeStockService {

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PharmacopeeStockResponse> getStocks(Boolean disponible, Boolean enRupture, Pageable pageable) {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        log.info("Consultation paginée des stocks pour la pharmacopée ID {} (disponible={})",
                pharmacopee.getId(), disponible);

        Page<DisponibiliteProduit> page;
        if (disponible != null) {
            page = disponibiliteProduitRepository.findByPharmacopeeIdAndDisponible(pharmacopee.getId(), disponible, pageable);
        } else {
            page = disponibiliteProduitRepository.findByPharmacopeeId(pharmacopee.getId(), pageable);
        }

        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacopeeStockSummaryResponse getSummary() {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        List<DisponibiliteProduit> list = disponibiliteProduitRepository.findByPharmacopeeId(pharmacopee.getId());

        int totalReferences = list.size();
        int totalEnStock = (int) list.stream()
                .filter(d -> d.getQuantiteStock() != null && d.getQuantiteStock() > 0)
                .count();
        int totalRupture = (int) list.stream()
                .filter(d -> d.getQuantiteStock() == null || d.getQuantiteStock() <= 0)
                .count();
        int totalActifsVente = (int) list.stream()
                .filter(d -> Boolean.TRUE.equals(d.getDisponible()))
                .count();
        int totalDesactives = totalReferences - totalActifsVente;

        double valeurTotaleStock = list.stream().mapToDouble(d -> {
            double prixEffectif = (d.getPrix() != null)
                    ? d.getPrix()
                    : ((d.getProduit() != null && d.getProduit().getPrix() != null) ? d.getProduit().getPrix() : 0.0);
            int qte = (d.getQuantiteStock() != null) ? d.getQuantiteStock() : 0;
            return prixEffectif * qte;
        }).sum();

        log.info("Synthèse inventaire calculée pour la pharmacopée ID {}: {} références, {} en stock, valeur: {} FCFA",
                pharmacopee.getId(), totalReferences, totalEnStock, valeurTotaleStock);

        return PharmacopeeStockSummaryResponse.builder()
                .totalReferences(totalReferences)
                .totalEnStock(totalEnStock)
                .totalRupture(totalRupture)
                .totalActifsVente(totalActifsVente)
                .totalDesactives(totalDesactives)
                .valeurTotaleStock(valeurTotaleStock)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacopeeStockResponse getStockByProduit(Long produitId) {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        DisponibiliteProduit dispo = disponibiliteProduitRepository
                .findByPharmacopeeIdAndProduitId(pharmacopee.getId(), produitId)
                .orElseThrow(() -> new ResourceNotFoundException("DisponibiliteProduit", "produitId", produitId));

        return mapToResponse(dispo);
    }

    @Override
    public PharmacopeeStockResponse updateQuantiteStock(Long produitId, UpdateStockRequest request) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        DisponibiliteProduit dispo = disponibiliteProduitRepository
                .findByPharmacopeeIdAndProduitId(pharmacopee.getId(), produitId)
                .orElseThrow(() -> new ResourceNotFoundException("DisponibiliteProduit", "produitId", produitId));

        dispo.setQuantiteStock(request.getQuantiteStock());
        dispo.setDateMiseAJour(LocalDateTime.now());

        DisponibiliteProduit saved = disponibiliteProduitRepository.save(dispo);
        log.info("Stock du produit ID {} mis à jour: {} unités pour pharmacopée ID {}",
                produitId, request.getQuantiteStock(), pharmacopee.getId());

        return mapToResponse(saved);
    }

    @Override
    public PharmacopeeStockResponse updatePrix(Long produitId, UpdatePrixRequest request) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        DisponibiliteProduit dispo = disponibiliteProduitRepository
                .findByPharmacopeeIdAndProduitId(pharmacopee.getId(), produitId)
                .orElseThrow(() -> new ResourceNotFoundException("DisponibiliteProduit", "produitId", produitId));

        dispo.setPrix(request.getPrix());
        dispo.setDateMiseAJour(LocalDateTime.now());

        DisponibiliteProduit saved = disponibiliteProduitRepository.save(dispo);
        log.info("Tarification du produit ID {} mise à jour (prix: {}) pour pharmacopée ID {}",
                produitId, request.getPrix(), pharmacopee.getId());

        return mapToResponse(saved);
    }

    @Override
    public PharmacopeeStockResponse toggleDisponibilite(Long produitId) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        DisponibiliteProduit dispo = disponibiliteProduitRepository
                .findByPharmacopeeIdAndProduitId(pharmacopee.getId(), produitId)
                .orElseThrow(() -> new ResourceNotFoundException("DisponibiliteProduit", "produitId", produitId));

        boolean nouveauStatut = !Boolean.TRUE.equals(dispo.getDisponible());
        dispo.setDisponible(nouveauStatut);
        dispo.setDateMiseAJour(LocalDateTime.now());

        DisponibiliteProduit saved = disponibiliteProduitRepository.save(dispo);
        log.info("Bascule de disponibilité pour le produit ID {}: désormais {} pour pharmacopée ID {}",
                produitId, nouveauStatut, pharmacopee.getId());

        return mapToResponse(saved);
    }

    @Override
    public PharmacopeeStockResponse updateStockComplet(Long produitId, UpdateStockCompletRequest request) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        DisponibiliteProduit dispo = disponibiliteProduitRepository
                .findByPharmacopeeIdAndProduitId(pharmacopee.getId(), produitId)
                .orElseThrow(() -> new ResourceNotFoundException("DisponibiliteProduit", "produitId", produitId));

        if (request.getQuantiteStock() != null) {
            dispo.setQuantiteStock(request.getQuantiteStock());
        }
        if (request.getPrix() != null) {
            dispo.setPrix(request.getPrix());
        }
        if (request.getDisponible() != null) {
            dispo.setDisponible(request.getDisponible());
        }
        dispo.setDateMiseAJour(LocalDateTime.now());

        DisponibiliteProduit saved = disponibiliteProduitRepository.save(dispo);
        log.info("Mise à jour complète du stock/prix pour le produit ID {} dans pharmacopée ID {}",
                produitId, pharmacopee.getId());

        return mapToResponse(saved);
    }

    private PharmacopeeStockResponse mapToResponse(DisponibiliteProduit dispo) {
        Produit p = dispo.getProduit();
        String nom = (p != null) ? p.getNom() : null;
        String forme = (p != null) ? p.getForme() : null;
        double prixNational = (p != null && p.getPrix() != null) ? p.getPrix() : 0.0;
        Double prixSpecifique = dispo.getPrix();
        double prixVente = (prixSpecifique != null) ? prixSpecifique : prixNational;
        boolean prixPersonnalise = (prixSpecifique != null);
        int quantite = (dispo.getQuantiteStock() != null) ? dispo.getQuantiteStock() : 0;
        boolean enRupture = (quantite <= 0);

        return PharmacopeeStockResponse.builder()
                .disponibiliteId(dispo.getId())
                .produitId(p != null ? p.getId() : null)
                .nomProduit(nom)
                .forme(forme)
                .prixNational(prixNational)
                .prixVente(prixVente)
                .prixPersonnalise(prixPersonnalise)
                .quantiteStock(quantite)
                .disponible(dispo.getDisponible())
                .enRupture(enRupture)
                .dateMiseAJour(dispo.getDateMiseAJour())
                .build();
    }
}
