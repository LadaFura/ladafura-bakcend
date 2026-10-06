package com.pharmacopee.ladafura.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationGlobalSearchResponse;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationMaladieSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationPharmacopeeSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationPlanteSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationProduitSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationVernaculaireSearchItem;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.mappers.PopulationRechercheMapper;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.MaladieRepository;
import com.pharmacopee.ladafura.repository.NomPlanteRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationRechercheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PopulationRechercheServiceImpl implements IPopulationRechercheService {

    private final PlanteRepository planteRepository;
    private final NomPlanteRepository nomPlanteRepository;
    private final MaladieRepository maladieRepository;
    private final ProduitRepository produitRepository;
    private final PharmacopeeRepository pharmacopeeRepository;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final PopulationRechercheMapper populationRechercheMapper;

    @Override
    public PopulationGlobalSearchResponse rechercheGlobale(String query) {
        if (query == null || query.trim().isEmpty()) {
            return PopulationGlobalSearchResponse.builder()
                    .query("")
                    .totalResultats(0)
                    .plantes(Collections.emptyList())
                    .nomsVernaculaires(Collections.emptyList())
                    .maladies(Collections.emptyList())
                    .produits(Collections.emptyList())
                    .pharmacopees(Collections.emptyList())
                    .build();
        }

        String trimmedQuery = query.trim();
        log.info("Recherche globale intelligente universelle pour le terme : '{}'", trimmedQuery);

        Pageable topLimit = PageRequest.of(0, 10);

        // 1. Recherche parmi les plantes validées (nom scientifique, vernaculaire, description, maladie)
        List<PopulationPlanteSearchItem> plantes = planteRepository
                .searchTopByStatutAndKeyword(StatutPlante.VALIDE, trimmedQuery, topLimit)
                .stream()
                .map(populationRechercheMapper::toPlanteItem)
                .toList();

        // 2. Recherche parmi les noms vernaculaires
        List<PopulationVernaculaireSearchItem> nomsVernaculaires = nomPlanteRepository
                .findByNomContainingIgnoreCase(trimmedQuery, topLimit)
                .getContent()
                .stream()
                .map(populationRechercheMapper::toVernaculaireItem)
                .toList();

        // 3. Recherche parmi les maladies
        List<PopulationMaladieSearchItem> maladies = maladieRepository
                .findByNomContainingIgnoreCase(trimmedQuery, topLimit)
                .getContent()
                .stream()
                .map(populationRechercheMapper::toMaladieItem)
                .toList();

        // 4. Recherche INTELLIGENTE des PHARMACOPÉES (Résultat Principal)
        // La pharmacopée est le résultat principal qu'on recherche une maladie, un produit, une plante ou une officine.
        Map<Long, PopulationPharmacopeeSearchItem> pharmacopeesMap = new LinkedHashMap<>();

        // 4.a : Correspondance directe par nom / description / localisation de la pharmacopée
        List<Pharmacopee> directPharmas = pharmacopeeRepository
                .searchTopByStatutAndKeyword(StatutPharmacopee.VALIDEE, trimmedQuery, topLimit);
        for (Pharmacopee p : directPharmas) {
            PopulationPharmacopeeSearchItem item = populationRechercheMapper.toPharmacopeeItem(p);
            item.setMotifCorrespondance("Officine de pharmacopée");
            pharmacopeesMap.put(p.getId(), item);
        }

        // 4.b : Correspondance par Produit disponible (nom / description / composition)
        List<DisponibiliteProduit> dispoByProduit = disponibiliteProduitRepository
                .searchDisponibilitesByProduitKeyword(trimmedQuery);
        for (DisponibiliteProduit d : dispoByProduit) {
            Pharmacopee p = d.getPharmacopee();
            if (p == null) continue;
            PopulationPharmacopeeSearchItem item = pharmacopeesMap.computeIfAbsent(
                    p.getId(), id -> populationRechercheMapper.toPharmacopeeItem(p));
            String prodLabel = d.getProduit().getNom() + (d.getPrix() != null ? " (" + d.getPrix().intValue() + " FCFA)" : "");
            if (!item.getProduitsDisponibles().contains(prodLabel)) {
                item.getProduitsDisponibles().add(prodLabel);
            }
            if (item.getMotifCorrespondance() == null || item.getMotifCorrespondance().equals("Officine de pharmacopée")) {
                item.setMotifCorrespondance("Remède disponible : " + d.getProduit().getNom());
            }
        }

        // 4.c : Correspondance par Maladie / Symptôme (ex: Paludisme -> pharmacopées qui proposent ces produits)
        List<DisponibiliteProduit> dispoByMaladie = disponibiliteProduitRepository
                .searchDisponibilitesByMaladieKeyword(trimmedQuery);
        for (DisponibiliteProduit d : dispoByMaladie) {
            Pharmacopee p = d.getPharmacopee();
            if (p == null) continue;
            PopulationPharmacopeeSearchItem item = pharmacopeesMap.computeIfAbsent(
                    p.getId(), id -> populationRechercheMapper.toPharmacopeeItem(p));
            String prodLabel = d.getProduit().getNom() + (d.getPrix() != null ? " (" + d.getPrix().intValue() + " FCFA)" : "");
            if (!item.getProduitsDisponibles().contains(prodLabel)) {
                item.getProduitsDisponibles().add(prodLabel);
            }
            if (item.getMotifCorrespondance() == null || item.getMotifCorrespondance().equals("Officine de pharmacopée")) {
                item.setMotifCorrespondance("Propose des remèdes pour : " + trimmedQuery);
            }
        }

        // 4.d : Correspondance par Plante médicinale (produits à base de cette plante vendus en officine)
        List<DisponibiliteProduit> dispoByPlante = disponibiliteProduitRepository
                .searchDisponibilitesByPlanteKeyword(trimmedQuery);
        for (DisponibiliteProduit d : dispoByPlante) {
            Pharmacopee p = d.getPharmacopee();
            if (p == null) continue;
            PopulationPharmacopeeSearchItem item = pharmacopeesMap.computeIfAbsent(
                    p.getId(), id -> populationRechercheMapper.toPharmacopeeItem(p));
            String prodLabel = d.getProduit().getNom() + (d.getPrix() != null ? " (" + d.getPrix().intValue() + " FCFA)" : "");
            if (!item.getProduitsDisponibles().contains(prodLabel)) {
                item.getProduitsDisponibles().add(prodLabel);
            }
            if (item.getMotifCorrespondance() == null || item.getMotifCorrespondance().equals("Officine de pharmacopée")) {
                item.setMotifCorrespondance("Remède à base de : " + trimmedQuery);
            }
        }

        List<PopulationPharmacopeeSearchItem> pharmacopees = new ArrayList<>(pharmacopeesMap.values());

        // 5. Recherche parmi les produits validés (conservé pour conformité du contrat DTO)
        List<PopulationProduitSearchItem> produits = produitRepository
                .searchTopByStatutAndKeyword(StatutProduit.VALIDE, trimmedQuery, topLimit)
                .stream()
                .map(populationRechercheMapper::toProduitItem)
                .toList();

        int total = pharmacopees.size() + plantes.size();

        return PopulationGlobalSearchResponse.builder()
                .query(trimmedQuery)
                .totalResultats(total)
                .plantes(plantes)
                .nomsVernaculaires(nomsVernaculaires)
                .maladies(maladies)
                .produits(produits)
                .pharmacopees(pharmacopees)
                .build();
    }

    @Override
    public Page<PopulationPlanteSearchItem> rechercherPlantes(String query, Pageable pageable) {
        log.info("Recherche paginée de plantes (query='{}')", query);
        Page<Plante> page;
        if (query != null && !query.trim().isEmpty()) {
            page = planteRepository.searchByStatutAndKeyword(StatutPlante.VALIDE, query.trim(), pageable);
        } else {
            page = planteRepository.findByStatut(StatutPlante.VALIDE, pageable);
        }
        return page.map(populationRechercheMapper::toPlanteItem);
    }

    @Override
    public Page<PopulationVernaculaireSearchItem> rechercherNomsVernaculaires(String nom, String langue, Pageable pageable) {
        log.info("Recherche de noms vernaculaires (nom='{}', langue='{}')", nom, langue);
        String trimmedNom = (nom != null && !nom.trim().isEmpty()) ? nom.trim() : null;
        String trimmedLangue = (langue != null && !langue.trim().isEmpty()) ? langue.trim() : null;

        Page<NomPlante> page = nomPlanteRepository.searchNomsVernaculaires(trimmedNom, trimmedLangue, pageable);
        return page.map(populationRechercheMapper::toVernaculaireItem);
    }

    @Override
    public Page<PopulationMaladieSearchItem> rechercherMaladies(String query, Pageable pageable) {
        log.info("Recherche de maladies (query='{}')", query);
        Page<Maladie> page;
        if (query != null && !query.trim().isEmpty()) {
            page = maladieRepository.findByNomContainingIgnoreCase(query.trim(), pageable);
        } else {
            page = maladieRepository.findAll(pageable);
        }
        return page.map(populationRechercheMapper::toMaladieItem);
    }

    @Override
    public Page<PopulationProduitSearchItem> rechercherProduits(String query, Long categorieId, Double prixMax, Pageable pageable) {
        log.info("Recherche multicritère de produits (query='{}', categorieId={}, prixMax={})", query, categorieId, prixMax);
        String trimmedQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;

        Page<Produit> page = produitRepository.searchProduits(StatutProduit.VALIDE, trimmedQuery, categorieId, prixMax, pageable);
        return page.map(populationRechercheMapper::toProduitItem);
    }

    @Override
    public Page<PopulationPharmacopeeSearchItem> rechercherPharmacopees(String query, String region, String cercle, String commune, Pageable pageable) {
        log.info("Recherche multicritère de pharmacopées (query='{}', region='{}', cercle='{}', commune='{}')", query, region, cercle, commune);
        String trimmedQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String trimmedRegion = (region != null && !region.trim().isEmpty()) ? region.trim() : null;
        String trimmedCercle = (cercle != null && !cercle.trim().isEmpty()) ? cercle.trim() : null;
        String trimmedCommune = (commune != null && !commune.trim().isEmpty()) ? commune.trim() : null;

        Page<Pharmacopee> page = pharmacopeeRepository.searchPharmacopees(StatutPharmacopee.VALIDEE, trimmedQuery, trimmedRegion, trimmedCercle, trimmedCommune, pageable);
        return page.map(populationRechercheMapper::toPharmacopeeItem);
    }
}
