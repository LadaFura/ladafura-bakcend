package com.pharmacopee.ladafura.services.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.CompositionProduit;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.produit.PopulationOffrePharmacopeeDto;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitDetailResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationProduitMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.CompositionProduitRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationProduitService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PopulationProduitServiceImpl implements IPopulationProduitService {

    private final ProduitRepository produitRepository;
    private final CompositionProduitRepository compositionProduitRepository;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final ModeRetraitRepository modeRetraitRepository;
    private final AvisRepository avisRepository;
    private final PopulationProduitMapper mapper;

    @Override
    public Page<PopulationProduitSummaryResponse> listerProduits(
            String keyword, Long categorieId, Double prixMax, Pageable pageable) {

        log.info("Consultation du catalogue des produits (keyword='{}', categorieId={}, prixMax={}, page={}, size={})",
                keyword, categorieId, prixMax, pageable.getPageNumber(), pageable.getPageSize());

        Page<Produit> page = produitRepository.searchProduits(
                StatutProduit.VALIDE, keyword, categorieId, prixMax, pageable);

        return page.map(produit -> {
            List<DisponibiliteProduit> disponibilites = disponibiliteProduitRepository.findOffresValideesByProduitId(produit.getId());
            Double noteMoyenne = avisRepository.findAverageNoteByProduitIdAndStatut(produit.getId(), StatutAvis.PUBLIE);
            long nombreAvis = avisRepository.countByProduitIdAndStatut(produit.getId(), StatutAvis.PUBLIE);
            return mapper.toSummaryResponse(produit, disponibilites, noteMoyenne, nombreAvis);
        });
    }

    @Override
    public PopulationProduitDetailResponse getProduitDetail(Long id) {
        log.info("Consultation détaillée du produit ID: {}", id);

        Produit produit = produitRepository.findByIdAndStatut(id, StatutProduit.VALIDE)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", id));

        List<CompositionProduit> compositions = compositionProduitRepository.findByProduitId(id);
        List<DisponibiliteProduit> disponibilites = disponibiliteProduitRepository.findOffresValideesByProduitId(id);

        Map<Long, List<ModeRetrait>> modesRetraitByPhId = disponibilites.stream()
                .filter(d -> d.getPharmacopee() != null)
                .map(d -> d.getPharmacopee().getId())
                .distinct()
                .collect(Collectors.toMap(
                        phId -> phId,
                        modeRetraitRepository::findByPharmacopeeId,
                        (existing, replacement) -> existing
                ));

        Double noteMoyenne = avisRepository.findAverageNoteByProduitIdAndStatut(id, StatutAvis.PUBLIE);
        long nombreAvis = avisRepository.countByProduitIdAndStatut(id, StatutAvis.PUBLIE);

        return mapper.toDetailResponse(produit, compositions, disponibilites, modesRetraitByPhId, noteMoyenne, nombreAvis);
    }

    @Override
    public List<PopulationOffrePharmacopeeDto> getOffresByProduit(Long id) {
        log.info("Consultation des offres des officines pour le produit ID: {}", id);

        // Vérifier que le produit existe et est validé
        produitRepository.findByIdAndStatut(id, StatutProduit.VALIDE)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", id));

        List<DisponibiliteProduit> disponibilites = disponibiliteProduitRepository.findOffresValideesByProduitId(id);

        return disponibilites.stream()
                .map(d -> {
                    List<ModeRetrait> modes = d.getPharmacopee() != null
                            ? modeRetraitRepository.findByPharmacopeeId(d.getPharmacopee().getId())
                            : List.of();
                    return mapper.toOffreDto(d, modes);
                })
                .toList();
    }
}
