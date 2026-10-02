package com.pharmacopee.ladafura.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.CategorieProduit;
import com.pharmacopee.ladafura.Models.CompositionProduit;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.admin.produit.AdminCategorieRequest;
import com.pharmacopee.ladafura.dto.admin.produit.AdminCategorieResponse;
import com.pharmacopee.ladafura.dto.admin.produit.AdminCompositionRequestDto;
import com.pharmacopee.ladafura.dto.admin.produit.AdminCreateProduitRequest;
import com.pharmacopee.ladafura.dto.admin.produit.AdminModerateProduitRequest;
import com.pharmacopee.ladafura.dto.admin.produit.AdminProduitResponse;
import com.pharmacopee.ladafura.dto.admin.produit.AdminUpdateProduitRequest;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AdminProduitMapper;
import com.pharmacopee.ladafura.repository.CategorieProduitRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminProduitService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminProduitServiceImpl implements IAdminProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieProduitRepository categorieProduitRepository;
    private final PlanteRepository planteRepository;
    private final AdminProduitMapper produitMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminProduitResponse> getAllProduits(StatutProduit statut, Long categorieId, Pageable pageable) {
        log.info("Récupération paginée des produits (statut={}, categorieId={})", statut, categorieId);
        Page<Produit> page;
        if (statut != null) {
            page = produitRepository.findByStatut(statut, pageable);
        } else if (categorieId != null) {
            page = produitRepository.findByCategorieId(categorieId, pageable);
        } else {
            page = produitRepository.findAll(pageable);
        }

        return page.map(produitMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProduitResponse getProduitById(Long id) {
        log.info("Consultation du produit {}", id);
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", id));

        return produitMapper.toDto(produit);
    }

    @Override
    public AdminProduitResponse createProduit(AdminCreateProduitRequest request) {
        log.info("Création d'un nouveau produit : {}", request.getNom());

        CategorieProduit categorie = null;
        if (request.getCategorieId() != null) {
            categorie = categorieProduitRepository.findById(request.getCategorieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie Produit", "id", request.getCategorieId()));
        }

        Produit produit = Produit.builder()
                .nom(request.getNom().trim())
                .description(request.getDescription())
                .forme(request.getForme())
                .composition(request.getComposition())
                .prix(request.getPrix())
                .photoUrl(request.getPhotoUrl())
                .statut(request.getStatut() != null ? request.getStatut() : StatutProduit.VALIDE)
                .categorie(categorie)
                .compositions(new ArrayList<>())
                .build();

        if (request.getCompositions() != null && !request.getCompositions().isEmpty()) {
            for (AdminCompositionRequestDto compReq : request.getCompositions()) {
                Plante plante = planteRepository.findById(compReq.getPlanteId())
                        .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", compReq.getPlanteId()));
                CompositionProduit cp = CompositionProduit.builder()
                        .produit(produit)
                        .plante(plante)
                        .quantite(compReq.getQuantite())
                        .unite(compReq.getUnite())
                        .build();
                produit.getCompositions().add(cp);
            }
        }

        Produit saved = produitRepository.save(produit);
        return produitMapper.toDto(saved);
    }

    @Override
    public AdminProduitResponse updateProduit(Long id, AdminUpdateProduitRequest request) {
        log.info("Mise à jour du produit {}", id);
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", id));

        if (request.getNom() != null) produit.setNom(request.getNom().trim());
        if (request.getDescription() != null) produit.setDescription(request.getDescription());
        if (request.getForme() != null) produit.setForme(request.getForme());
        if (request.getComposition() != null) produit.setComposition(request.getComposition());
        if (request.getPrix() != null) produit.setPrix(request.getPrix());
        if (request.getPhotoUrl() != null) produit.setPhotoUrl(request.getPhotoUrl());
        if (request.getStatut() != null) produit.setStatut(request.getStatut());

        if (request.getCategorieId() != null) {
            CategorieProduit categorie = categorieProduitRepository.findById(request.getCategorieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie Produit", "id", request.getCategorieId()));
            produit.setCategorie(categorie);
        }

        if (request.getCompositions() != null) {
            produit.getCompositions().clear();
            for (AdminCompositionRequestDto compReq : request.getCompositions()) {
                Plante plante = planteRepository.findById(compReq.getPlanteId())
                        .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", compReq.getPlanteId()));
                CompositionProduit cp = CompositionProduit.builder()
                        .produit(produit)
                        .plante(plante)
                        .quantite(compReq.getQuantite())
                        .unite(compReq.getUnite())
                        .build();
                produit.getCompositions().add(cp);
            }
        }

        Produit saved = produitRepository.save(produit);
        return produitMapper.toDto(saved);
    }

    @Override
    public void deleteProduit(Long id) {
        log.info("Suppression du produit {}", id);
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", id));

        produitRepository.delete(produit);
    }

    @Override
    public AdminProduitResponse moderateProduit(Long id, AdminModerateProduitRequest request) {
        log.info("Modération du produit {} : nouveau statut {}", id, request.getAction());
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", id));

        produit.setStatut(request.getAction());
        Produit saved = produitRepository.save(produit);

        return produitMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminCategorieResponse> getAllCategories() {
        log.info("Récupération de toutes les catégories de produits");
        return categorieProduitRepository.findAll().stream()
                .map(produitMapper::toCategorieDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminCategorieResponse createCategorie(AdminCategorieRequest request) {
        log.info("Création d'une nouvelle catégorie : {}", request.getNom());
        CategorieProduit categorie = CategorieProduit.builder()
                .nom(request.getNom().trim())
                .description(request.getDescription())
                .statut(request.getStatut() != null ? request.getStatut() : true)
                .build();

        CategorieProduit saved = categorieProduitRepository.save(categorie);
        return produitMapper.toCategorieDto(saved);
    }

    @Override
    public AdminCategorieResponse updateCategorie(Long id, AdminCategorieRequest request) {
        log.info("Mise à jour de la catégorie {}", id);
        CategorieProduit categorie = categorieProduitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie Produit", "id", id));

        categorie.setNom(request.getNom().trim());
        categorie.setDescription(request.getDescription());
        if (request.getStatut() != null) {
            categorie.setStatut(request.getStatut());
        }

        CategorieProduit updated = categorieProduitRepository.save(categorie);
        return produitMapper.toCategorieDto(updated);
    }

    @Override
    public void deleteCategorie(Long id) {
        log.info("Suppression de la catégorie {}", id);
        CategorieProduit categorie = categorieProduitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie Produit", "id", id));

        long nbProduits = produitRepository.countByCategorieId(id);
        if (nbProduits > 0) {
            throw new BadRequestException("Impossible de supprimer la catégorie : " + nbProduits + " produit(s) y sont rattaché(s).");
        }

        categorieProduitRepository.delete(categorie);
    }
}
