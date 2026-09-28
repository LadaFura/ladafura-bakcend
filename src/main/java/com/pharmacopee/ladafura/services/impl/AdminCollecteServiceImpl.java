package com.pharmacopee.ladafura.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteDetailResponse;
import com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.admin.collecte.AdminModerateCollecteRequest;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AdminCollecteMapper;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminCollecteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminCollecteServiceImpl implements IAdminCollecteService {

    private final CollecteRepository collecteRepository;
    private final AdminCollecteMapper collecteMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCollecteSummaryResponse> getAllCollectes(StatutCollecte statut, Pageable pageable) {
        log.info("Récupération paginée des collectes (statut={})", statut);
        Page<Collecte> page = (statut != null)
                ? collecteRepository.findByStatut(statut, pageable)
                : collecteRepository.findAll(pageable);

        return page.map(collecteMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCollecteDetailResponse getCollecteById(Long id) {
        log.info("Consultation de la collecte {}", id);
        Collecte collecte = collecteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", id));

        return collecteMapper.toDetailDto(collecte);
    }

    @Override
    public AdminCollecteDetailResponse moderateCollecte(Long id, AdminModerateCollecteRequest request) {
        log.info("Modération de la collecte {} avec action {}", id, request.getAction());
        Collecte collecte = collecteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", id));

        collecte.setStatut(request.getAction());
        if (request.getMotifRejet() != null) {
            collecte.setMotifRejet(request.getMotifRejet());
        }

        Collecte saved = collecteRepository.save(collecte);
        return collecteMapper.toDetailDto(saved);
    }
}
