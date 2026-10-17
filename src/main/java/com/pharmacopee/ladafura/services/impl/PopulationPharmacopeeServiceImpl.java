package com.pharmacopee.ladafura.services.impl;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeDetailResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeModeRetraitDto;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeProduitItemResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationPharmacopeeMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationPharmacopeeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PopulationPharmacopeeServiceImpl implements IPopulationPharmacopeeService {

    private static final int MAX_NEARBY_PHARMACOPEES = 10;
    private static final double DEFAULT_BAMAKO_LAT = 12.6392;
    private static final double DEFAULT_BAMAKO_LNG = -8.0029;

    private final PharmacopeeRepository pharmacopeeRepository;
    private final ModeRetraitRepository modeRetraitRepository;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final AvisRepository avisRepository;
    private final PopulationPharmacopeeMapper mapper;

    @Override
    public Page<PopulationPharmacopeeSummaryResponse> listerPharmacopees(
            String keyword, String region, String cercle, String commune, Pageable pageable) {

        log.info("Recherche des pharmacopées agréées (keyword='{}', region='{}', cercle='{}', commune='{}', page={}, size={})",
                keyword, region, cercle, commune, pageable.getPageNumber(), pageable.getPageSize());

        Page<Pharmacopee> page = pharmacopeeRepository.searchPharmacopees(
                StatutPharmacopee.VALIDEE, keyword, region, cercle, commune, pageable);

        return page.map(this::mapToSummary);
    }

    @Override
    public List<PopulationPharmacopeeSummaryResponse> listerPharmacopeesProximite(
            Double latitude, Double longitude, int limit) {

        int safeLimit = Math.min(Math.max(limit, 1), MAX_NEARBY_PHARMACOPEES);
        double refLat = latitude != null ? latitude : DEFAULT_BAMAKO_LAT;
        double refLng = longitude != null ? longitude : DEFAULT_BAMAKO_LNG;

        log.info("Recherche des {} pharmacopées agréées les plus proches de ({}, {})", safeLimit, refLat, refLng);

        List<Pharmacopee> validees = pharmacopeeRepository.findByStatut(StatutPharmacopee.VALIDEE);

        return validees.stream()
                .map(this::mapToSummary)
                .sorted(Comparator.comparingDouble(ph -> calculateDistanceKm(
                        refLat, refLng,
                        ph.getLatitude() != null ? ph.getLatitude() : DEFAULT_BAMAKO_LAT,
                        ph.getLongitude() != null ? ph.getLongitude() : DEFAULT_BAMAKO_LNG)))
                .limit(safeLimit)
                .toList();
    }

    private PopulationPharmacopeeSummaryResponse mapToSummary(Pharmacopee ph) {
        List<ModeRetrait> modes = modeRetraitRepository.findByPharmacopeeId(ph.getId());
        long nbProduits = disponibiliteProduitRepository.countProduitsValidesByPharmacopeeId(ph.getId());
        Double noteMoyenne = avisRepository.findAverageNoteByPharmacopeeIdAndStatut(ph.getId(), StatutAvis.PUBLIE);
        long nbAvis = avisRepository.countByPharmacopeeIdAndStatut(ph.getId(), StatutAvis.PUBLIE);
        return mapper.toSummaryResponse(ph, modes, nbProduits, noteMoyenne, nbAvis);
    }

    private static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    @Override
    public PopulationPharmacopeeDetailResponse getPharmacopeeDetail(Long id) {
        log.info("Consultation de la fiche détaillée de la pharmacopée ID: {}", id);

        Pharmacopee ph = pharmacopeeRepository.findByIdAndStatut(id, StatutPharmacopee.VALIDEE)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", id));

        List<ModeRetrait> modes = modeRetraitRepository.findByPharmacopeeId(id);
        long nbProduits = disponibiliteProduitRepository.countProduitsValidesByPharmacopeeId(id);
        Double noteMoyenne = avisRepository.findAverageNoteByPharmacopeeIdAndStatut(id, StatutAvis.PUBLIE);
        long nbAvis = avisRepository.countByPharmacopeeIdAndStatut(id, StatutAvis.PUBLIE);

        return mapper.toDetailResponse(ph, modes, nbProduits, noteMoyenne, nbAvis);
    }

    @Override
    public Page<PopulationPharmacopeeProduitItemResponse> getProduitsByPharmacopee(
            Long id, Boolean disponibleOnly, Pageable pageable) {

        log.info("Consultation des produits de la pharmacopée ID: {} (disponibleOnly={}, page={}, size={})",
                id, disponibleOnly, pageable.getPageNumber(), pageable.getPageSize());

        pharmacopeeRepository.findByIdAndStatut(id, StatutPharmacopee.VALIDEE)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", id));

        Page<DisponibiliteProduit> page = disponibiliteProduitRepository.findProduitsByPharmacopeeId(
                id, disponibleOnly, pageable);

        return page.map(mapper::toProduitItemResponse);
    }

    @Override
    public List<PopulationPharmacopeeModeRetraitDto> getModesRetraitByPharmacopee(Long id) {
        log.info("Consultation des modes de retrait de la pharmacopée ID: {}", id);

        pharmacopeeRepository.findByIdAndStatut(id, StatutPharmacopee.VALIDEE)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", id));

        List<ModeRetrait> modes = modeRetraitRepository.findByPharmacopeeId(id);
        return modes.stream()
                .map(mapper::toModeRetraitDto)
                .toList();
    }
}
