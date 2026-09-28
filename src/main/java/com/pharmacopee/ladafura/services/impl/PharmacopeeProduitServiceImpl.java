package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.pharmacopee.produit.AssocierProduitRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.produit.CatalogueProduitItemResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.produit.PharmacopeeProduitResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.produit.UpdateProduitDisponibiliteRequest;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeProduitService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacopeeProduitServiceImpl implements IPharmacopeeProduitService {

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final ProduitRepository produitRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PharmacopeeProduitResponse> getMesProduits(String keyword, Long categorieId, Boolean disponible, Pageable pageable) {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        log.info("Consultation du catalogue pour la pharmacopée ID {} (disponible={})", pharmacopee.getId(), disponible);

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
    public PharmacopeeProduitResponse getMonProduit(Long produitId) {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        DisponibiliteProduit dispo = disponibiliteProduitRepository
                .findByPharmacopeeIdAndProduitId(pharmacopee.getId(), produitId)
                .orElseThrow(() -> new ResourceNotFoundException("DisponibiliteProduit", "produitId", produitId));

        return mapToResponse(dispo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CatalogueProduitItemResponse> getCatalogueGlobal(String keyword, Long categorieId, Pageable pageable) {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        log.info("Consultation du catalogue national validé par la pharmacopée ID {}", pharmacopee.getId());

        Page<Produit> page;
        if (keyword != null && !keyword.trim().isEmpty()) {
            page = produitRepository.findByNomContainingIgnoreCase(keyword.trim(), pageable);
        } else if (categorieId != null) {
            page = produitRepository.findByCategorieId(categorieId, pageable);
        } else {
            page = produitRepository.findByStatut(StatutProduit.VALIDE, pageable);
        }

        return page.map(p -> {
            boolean dejaAssocie = disponibiliteProduitRepository.existsByPharmacopeeIdAndProduitId(pharmacopee.getId(), p.getId());
            String catNom = (p.getCategorie() != null) ? p.getCategorie().getNom() : null;

            return CatalogueProduitItemResponse.builder()
                    .id(p.getId())
                    .nom(p.getNom())
                    .description(p.getDescription())
                    .forme(p.getForme())
                    .prix(p.getPrix())
                    .photoUrl(p.getPhotoUrl())
                    .categorieNom(catNom)
                    .dejaAssocie(dejaAssocie)
                    .build();
        });
    }

    @Override
    public PharmacopeeProduitResponse associerProduit(AssocierProduitRequest request) {
        // 1. Contrôle strict : la pharmacopée doit être VALIDEE par l'administration
        pharmacopeeAuthService.verifyPharmacopeeValidated();

        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        // 2. Vérification de l'existence du produit dans le catalogue national
        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", request.getProduitId()));

        if (produit.getStatut() != StatutProduit.VALIDE) {
            log.warn("Tentative d'association d'un produit non validé (ID: {}, Statut: {})", produit.getId(), produit.getStatut());
            throw new BadRequestException("Ce produit ne peut pas être associé car il n'est pas encore validé par l'administration.");
        }

        // 3. Unicité de l'association
        if (disponibiliteProduitRepository.existsByPharmacopeeIdAndProduitId(pharmacopee.getId(), produit.getId())) {
            throw new ConflictException("Le produit '" + produit.getNom() + "' est déjà associé à votre pharmacopée.");
        }

        DisponibiliteProduit dispo = DisponibiliteProduit.builder()
                .pharmacopee(pharmacopee)
                .produit(produit)
                .quantiteStock(request.getQuantiteStock() != null ? request.getQuantiteStock() : 0)
                .disponible(request.getDisponible() != null ? request.getDisponible() : true)
                .dateMiseAJour(LocalDateTime.now())
                .build();

        DisponibiliteProduit saved = disponibiliteProduitRepository.save(dispo);
        log.info("Produit ID {} ('{}') associé avec succès à la pharmacopée ID {}",
                produit.getId(), produit.getNom(), pharmacopee.getId());

        return mapToResponse(saved);
    }

    @Override
    public PharmacopeeProduitResponse updateDisponibilite(Long produitId, UpdateProduitDisponibiliteRequest request) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        DisponibiliteProduit dispo = disponibiliteProduitRepository
                .findByPharmacopeeIdAndProduitId(pharmacopee.getId(), produitId)
                .orElseThrow(() -> new ResourceNotFoundException("DisponibiliteProduit", "produitId", produitId));

        if (request.getQuantiteStock() != null) {
            dispo.setQuantiteStock(request.getQuantiteStock());
        }
        if (request.getDisponible() != null) {
            dispo.setDisponible(request.getDisponible());
        }
        dispo.setDateMiseAJour(LocalDateTime.now());

        DisponibiliteProduit updated = disponibiliteProduitRepository.save(dispo);
        log.info("Disponibilité du produit ID {} mise à jour pour la pharmacopée ID {} (stock={}, disponible={})",
                produitId, pharmacopee.getId(), updated.getQuantiteStock(), updated.getDisponible());

        return mapToResponse(updated);
    }

    @Override
    public void retirerProduit(Long produitId) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        DisponibiliteProduit dispo = disponibiliteProduitRepository
                .findByPharmacopeeIdAndProduitId(pharmacopee.getId(), produitId)
                .orElseThrow(() -> new ResourceNotFoundException("DisponibiliteProduit", "produitId", produitId));

        disponibiliteProduitRepository.delete(dispo);
        log.info("Produit ID {} retiré des disponibilités de la pharmacopée ID {}", produitId, pharmacopee.getId());
    }

    private PharmacopeeProduitResponse mapToResponse(DisponibiliteProduit dispo) {
        Produit p = dispo.getProduit();
        String catNom = (p.getCategorie() != null) ? p.getCategorie().getNom() : null;
        Long catId = (p.getCategorie() != null) ? p.getCategorie().getId() : null;

        return PharmacopeeProduitResponse.builder()
                .disponibiliteId(dispo.getId())
                .produitId(p.getId())
                .nom(p.getNom())
                .description(p.getDescription())
                .forme(p.getForme())
                .prix(p.getPrix())
                .photoUrl(p.getPhotoUrl())
                .categorieNom(catNom)
                .categorieId(catId)
                .quantiteStock(dispo.getQuantiteStock())
                .disponible(dispo.getDisponible())
                .dateMiseAJour(dispo.getDateMiseAJour())
                .build();
    }
}
