package com.pharmacopee.ladafura.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.dto.admin.avis.AdminAvisResponse;
import com.pharmacopee.ladafura.dto.admin.avis.AdminModerateAvisRequest;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AdminAvisMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminAvisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminAvisServiceImpl implements IAdminAvisService {

    private final AvisRepository avisRepository;
    private final AdminAvisMapper avisMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminAvisResponse> getAllAvis(StatutAvis statut, Long pharmacopeeId, Pageable pageable) {
        log.info("Récupération paginée des avis (statut={}, pharmacopeeId={})", statut, pharmacopeeId);
        Page<Avis> page;
        if (statut != null && pharmacopeeId != null) {
            page = avisRepository.findByPharmacopeeIdAndStatut(pharmacopeeId, statut, pageable);
        } else if (statut != null) {
            page = avisRepository.findByStatut(statut, pageable);
        } else if (pharmacopeeId != null) {
            page = avisRepository.findByPharmacopeeId(pharmacopeeId, pageable);
        } else {
            page = avisRepository.findAll(pageable);
        }

        return page.map(avisMapper::toDto);
    }

    @Override
    public AdminAvisResponse moderateAvis(Long id, AdminModerateAvisRequest request) {
        log.info("Modération de l'avis {} avec action {}", id, request.getAction());
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis", "id", id));

        avis.setStatut(request.getAction());
        Avis saved = avisRepository.save(avis);

        return avisMapper.toDto(saved);
    }

    @Override
    public void deleteAvis(Long id) {
        log.info("Suppression de l'avis {}", id);
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis", "id", id));

        avisRepository.delete(avis);
    }
}
