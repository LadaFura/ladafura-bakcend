package com.pharmacopee.ladafura.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.EtudeScientifique;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.population.plante.PopulationConnaissanceTraditionnelleDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationEtudeScientifiqueDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteDetailResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.StatutValidation;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationPlanteMapper;
import com.pharmacopee.ladafura.mappers.PopulationProduitMapper;
import com.pharmacopee.ladafura.Models.CompositionProduit;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.repository.CompositionProduitRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.EtudeScientifiqueRepository;
import com.pharmacopee.ladafura.repository.NomPlanteRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.VertuDeLaPlanteRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationPlanteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PopulationPlanteServiceImpl implements IPopulationPlanteService {

    private final PlanteRepository planteRepository;
    private final VertuDeLaPlanteRepository vertuDeLaPlanteRepository;
    private final EtudeScientifiqueRepository etudeScientifiqueRepository;
    private final NomPlanteRepository nomPlanteRepository;
    private final CompositionProduitRepository compositionProduitRepository;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final PopulationPlanteMapper mapper;
    private final PopulationProduitMapper produitMapper;

    @Override
    public Page<PopulationPlanteSummaryResponse> listerPlantes(Pageable pageable) {
        log.info("Consultation du catalogue des plantes validées par la Population (page={}, size={})",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Plante> page = planteRepository.findByStatut(StatutPlante.VALIDE, pageable);

        return page.map(plante -> {
            int nbConnaissances = vertuDeLaPlanteRepository.findByPlanteIdAndStatut(plante.getId(), StatutValidation.VALIDE).size();
            int nbEtudes = etudeScientifiqueRepository.findByPlanteId(plante.getId()).size();
            return mapper.toSummaryResponse(plante, nbConnaissances, nbEtudes);
        });
    }

    @Override
    public PopulationPlanteDetailResponse getPlanteDetail(Long id) {
        log.info("Consultation de la fiche détaillée de la plante ID: {}", id);

        Plante plante = planteRepository.findByIdAndStatut(id, StatutPlante.VALIDE)
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", id));

        List<VertuDeLaPlante> vertusValidees = vertuDeLaPlanteRepository.findByPlanteIdAndStatut(id, StatutValidation.VALIDE);
        List<EtudeScientifique> etudes = etudeScientifiqueRepository.findByPlanteId(id);
        List<NomPlante> noms = nomPlanteRepository.findByPlanteId(id);

        return mapper.toDetailResponse(plante, vertusValidees, etudes, noms);
    }

    @Override
    public List<PopulationConnaissanceTraditionnelleDto> getConnaissancesByPlante(Long id) {
        log.info("Consultation des connaissances traditionnelles validées de la plante ID: {}", id);

        // Vérifier que la plante existe et est validée
        planteRepository.findByIdAndStatut(id, StatutPlante.VALIDE)
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", id));

        List<VertuDeLaPlante> vertus = vertuDeLaPlanteRepository.findByPlanteIdAndStatut(id, StatutValidation.VALIDE);
        return vertus.stream()
                .map(mapper::toConnaissanceDto)
                .toList();
    }

    @Override
    public List<PopulationEtudeScientifiqueDto> getEtudesByPlante(Long id) {
        log.info("Consultation des études scientifiques de la plante ID: {}", id);

        // Vérifier que la plante existe et est validée
        planteRepository.findByIdAndStatut(id, StatutPlante.VALIDE)
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", id));

        List<EtudeScientifique> etudes = etudeScientifiqueRepository.findByPlanteId(id);
        return etudes.stream()
                .map(mapper::toEtudeDto)
                .toList();
    }

    @Override
    public List<PopulationProduitSummaryResponse> getProduitsByPlante(Long id) {
        log.info("Consultation des produits traditionnels contenant la plante ID: {}", id);

        // Vérifier que la plante existe et est validée
        planteRepository.findByIdAndStatut(id, StatutPlante.VALIDE)
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", id));

        List<CompositionProduit> compositions = compositionProduitRepository.findByPlanteId(id);

        return compositions.stream()
                .map(CompositionProduit::getProduit)
                .filter(p -> p != null && StatutProduit.VALIDE.equals(p.getStatut()))
                .distinct()
                .map(produit -> {
                    List<DisponibiliteProduit> disponibilites = disponibiliteProduitRepository.findOffresValideesByProduitId(produit.getId());
                    return produitMapper.toSummaryResponse(produit, disponibilites, null, 0L);
                })
                .toList();
    }
}
