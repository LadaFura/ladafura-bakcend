package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.avis.PopulationAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationCreateAvisRequest;
import com.pharmacopee.ladafura.dto.population.avis.PopulationEligibiliteAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationUpdateAvisRequest;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationAvisMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.LigneCommandeRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAvisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PopulationAvisServiceImpl implements IPopulationAvisService {

    private final IPopulationAuthService populationAuthService;
    private final AvisRepository avisRepository;
    private final ProduitRepository produitRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final PopulationAvisMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PopulationEligibiliteAvisResponse verifierEligibiliteAvis(Long produitId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Vérification d'éligibilité avis pour l'utilisateur ID: {} et le produit ID: {}", user.getId(), produitId);

        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", produitId));

        boolean eligible = ligneCommandeRepository.hasUserPurchasedAndReceivedProduct(user.getId(), produitId);
        Optional<Avis> existingAvis = avisRepository.findByUtilisateurIdAndProduitId(user.getId(), produitId);

        return mapper.toEligibiliteResponse(produit, eligible, existingAvis);
    }

    @Override
    public PopulationAvisResponse creerAvis(PopulationCreateAvisRequest request) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Création d'un avis par l'utilisateur ID: {} pour le produit ID: {}", user.getId(), request.getProduitId());

        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", request.getProduitId()));

        boolean eligible = ligneCommandeRepository.hasUserPurchasedAndReceivedProduct(user.getId(), produit.getId());
        if (!eligible) {
            throw new BadRequestException("Vous devez avoir commandé et réceptionné ce produit (commande livrée ou retirée) pour pouvoir donner votre avis.");
        }

        if (avisRepository.existsByUtilisateurIdAndProduitId(user.getId(), produit.getId())) {
            throw new BadRequestException("Vous avez déjà déposé un avis pour ce produit. Vous pouvez modifier votre avis existant.");
        }

        Avis avis = Avis.builder()
                .note(request.getNote())
                .commentaire(request.getCommentaire())
                .dateAvis(LocalDateTime.now())
                .statut(StatutAvis.EN_ATTENTE)
                .utilisateur(user)
                .produit(produit)
                .build();

        Avis saved = avisRepository.save(avis);
        log.info("Avis ID: {} créé avec succès (en attente de modération) pour le produit ID: {}", saved.getId(), produit.getId());

        return mapper.toResponse(saved);
    }

    @Override
    public PopulationAvisResponse modifierAvis(Long avisId, PopulationUpdateAvisRequest request) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Modification de l'avis ID: {} par l'utilisateur ID: {}", avisId, user.getId());

        Avis avis = avisRepository.findByIdAndUtilisateurId(avisId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Avis", "id", avisId));

        avis.setNote(request.getNote());
        avis.setCommentaire(request.getCommentaire());
        avis.setDateAvis(LocalDateTime.now());
        avis.setStatut(StatutAvis.EN_ATTENTE); // Nécessite une nouvelle validation de modération

        Avis updated = avisRepository.save(avis);
        log.info("Avis ID: {} mis à jour avec succès", avisId);

        return mapper.toResponse(updated);
    }

    @Override
    public void supprimerAvis(Long avisId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Suppression de l'avis ID: {} par l'utilisateur ID: {}", avisId, user.getId());

        Avis avis = avisRepository.findByIdAndUtilisateurId(avisId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Avis", "id", avisId));

        avisRepository.delete(avis);
        log.info("Avis ID: {} supprimé avec succès", avisId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PopulationAvisResponse> getMesAvis(StatutAvis statut, Pageable pageable) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation des avis pour l'utilisateur ID: {} (filtre statut: {})", user.getId(), statut);

        Page<Avis> page = (statut != null)
                ? avisRepository.findByUtilisateurIdAndStatut(user.getId(), statut, pageable)
                : avisRepository.findByUtilisateurId(user.getId(), pageable);

        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PopulationAvisResponse getAvisDetail(Long avisId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation du détail de l'avis ID: {} pour l'utilisateur ID: {}", avisId, user.getId());

        Avis avis = avisRepository.findByIdAndUtilisateurId(avisId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Avis", "id", avisId));

        return mapper.toResponse(avis);
    }
}
