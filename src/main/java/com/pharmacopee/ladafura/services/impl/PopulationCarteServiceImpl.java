package com.pharmacopee.ladafura.services.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCarteDetailPharmacopeeResponse;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCartePharmacopeeItem;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCarteProduitItem;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationCarteMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationCarteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PopulationCarteServiceImpl implements IPopulationCarteService {

    private final PharmacopeeRepository pharmacopeeRepository;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final ProduitRepository produitRepository;
    private final ModeRetraitRepository modeRetraitRepository;
    private final AvisRepository avisRepository;
    private final PopulationCarteMapper mapper;

    @Override
    public List<PopulationCartePharmacopeeItem> getPharmacopeesSurCarte(
            String query, String region, String cercle, String commune,
            Double userLat, Double userLng, Double rayonKm) {

        log.info("Chargement cartographique des pharmacopées (query='{}', region='{}', lat={}, lng={}, rayonKm={})",
                query, region, userLat, userLng, rayonKm);

        Page<Pharmacopee> page = pharmacopeeRepository.searchPharmacopees(
                StatutPharmacopee.VALIDEE, query, region, cercle, commune, Pageable.unpaged());

        List<PopulationCartePharmacopeeItem> items = new ArrayList<>();

        for (Pharmacopee ph : page.getContent()) {
            if (ph.getLocalisation() != null
                    && ph.getLocalisation().getLatitude() != null
                    && ph.getLocalisation().getLongitude() != null) {

                Double distance = mapper.calculateDistance(
                        userLat, userLng,
                        ph.getLocalisation().getLatitude(),
                        ph.getLocalisation().getLongitude());

                if (rayonKm != null && distance != null && distance > rayonKm) {
                    continue; // Hors du rayon kilométrique souhaité
                }

                List<ModeRetrait> modes = modeRetraitRepository.findByPharmacopeeId(ph.getId());
                long nbProduits = disponibiliteProduitRepository.countProduitsValidesByPharmacopeeId(ph.getId());
                Double noteMoyenne = avisRepository.findAverageNoteByPharmacopeeIdAndStatut(ph.getId(), StatutAvis.PUBLIE);
                long nbAvis = avisRepository.countByPharmacopeeIdAndStatut(ph.getId(), StatutAvis.PUBLIE);

                items.add(mapper.toCartePharmacopeeItem(ph, modes, nbProduits, noteMoyenne, nbAvis, distance));
            }
        }

        if (userLat != null && userLng != null) {
            items.sort(Comparator.comparing(
                    item -> item.getDistanceKm() != null ? item.getDistanceKm() : Double.MAX_VALUE));
        }

        return items;
    }

    @Override
    public List<PopulationCarteProduitItem> localiserProduitSurCarte(
            Long produitId, String query,
            Double userLat, Double userLng, Double rayonKm,
            Boolean disponibleOnly) {

        log.info("Localisation produit sur carte (produitId={}, query='{}', lat={}, lng={}, rayonKm={}, disponibleOnly={})",
                produitId, query, userLat, userLng, rayonKm, disponibleOnly);

        List<DisponibiliteProduit> disponibilites = new ArrayList<>();

        if (produitId != null) {
            disponibilites.addAll(disponibiliteProduitRepository.findOffresValideesByProduitId(produitId));
        } else if (query != null && !query.isBlank()) {
            List<Produit> produits = produitRepository.searchTopByStatutAndKeyword(
                    StatutProduit.VALIDE, query, PageRequest.of(0, 5));
            for (Produit p : produits) {
                disponibilites.addAll(disponibiliteProduitRepository.findOffresValideesByProduitId(p.getId()));
            }
        } else {
            return List.of();
        }

        boolean filterDisponible = disponibleOnly == null || disponibleOnly;
        List<PopulationCarteProduitItem> items = new ArrayList<>();

        for (DisponibiliteProduit disp : disponibilites) {
            Pharmacopee ph = disp.getPharmacopee();
            if (ph == null || ph.getLocalisation() == null
                    || ph.getLocalisation().getLatitude() == null
                    || ph.getLocalisation().getLongitude() == null) {
                continue;
            }

            if (filterDisponible && (!Boolean.TRUE.equals(disp.getDisponible())
                    || disp.getQuantiteStock() == null || disp.getQuantiteStock() <= 0)) {
                continue;
            }

            Double distance = mapper.calculateDistance(
                    userLat, userLng,
                    ph.getLocalisation().getLatitude(),
                    ph.getLocalisation().getLongitude());

            if (rayonKm != null && distance != null && distance > rayonKm) {
                continue;
            }

            List<ModeRetrait> modes = modeRetraitRepository.findByPharmacopeeId(ph.getId());
            items.add(mapper.toCarteProduitItem(disp, modes, distance));
        }

        if (userLat != null && userLng != null) {
            items.sort(Comparator.comparing(
                    item -> item.getDistanceKm() != null ? item.getDistanceKm() : Double.MAX_VALUE));
        }

        return items;
    }

    @Override
    public PopulationCarteDetailPharmacopeeResponse getPharmacopeeCarteDetail(
            Long id, Double userLat, Double userLng) {

        log.info("Consultation du détail cartographique de la pharmacopée ID: {} (userLat={}, userLng={})",
                id, userLat, userLng);

        Pharmacopee ph = pharmacopeeRepository.findByIdAndStatut(id, StatutPharmacopee.VALIDEE)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", id));

        Double distance = null;
        if (ph.getLocalisation() != null
                && ph.getLocalisation().getLatitude() != null
                && ph.getLocalisation().getLongitude() != null) {
            distance = mapper.calculateDistance(
                    userLat, userLng,
                    ph.getLocalisation().getLatitude(),
                    ph.getLocalisation().getLongitude());
        }

        List<ModeRetrait> modes = modeRetraitRepository.findByPharmacopeeId(id);
        long nbProduits = disponibiliteProduitRepository.countProduitsValidesByPharmacopeeId(id);
        Double noteMoyenne = avisRepository.findAverageNoteByPharmacopeeIdAndStatut(id, StatutAvis.PUBLIE);
        long nbAvis = avisRepository.countByPharmacopeeIdAndStatut(id, StatutAvis.PUBLIE);

        return mapper.toCarteDetailResponse(ph, modes, nbProduits, noteMoyenne, nbAvis, distance);
    }
}
