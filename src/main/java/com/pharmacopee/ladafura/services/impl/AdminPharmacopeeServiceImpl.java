package com.pharmacopee.ladafura.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminModeratePharmacopeeRequest;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminPharmacopeeDetailResponse;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AdminPharmacopeeMapper;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminPharmacopeeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminPharmacopeeServiceImpl implements IAdminPharmacopeeService {

    private final PharmacopeeRepository pharmacopeeRepository;
    private final AdminPharmacopeeMapper pharmacopeeMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPharmacopeeSummaryResponse> getAllPharmacopees(StatutPharmacopee statut, Pageable pageable) {
        log.info("Récupération paginée des pharmacopées (statut={})", statut);
        Page<Pharmacopee> page = (statut != null)
                ? pharmacopeeRepository.findByStatut(statut, pageable)
                : pharmacopeeRepository.findAll(pageable);

        return page.map(pharmacopeeMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPharmacopeeDetailResponse getPharmacopeeById(Long id) {
        log.info("Consultation détaillée de la pharmacopée {}", id);
        Pharmacopee pharmacopee = pharmacopeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", id));

        return pharmacopeeMapper.toDetailDto(pharmacopee);
    }

    @Override
    public AdminPharmacopeeDetailResponse moderatePharmacopee(Long id, AdminModeratePharmacopeeRequest request) {
        log.info("Modération de la pharmacopée {} : nouveau statut {}", id, request.getAction());
        Pharmacopee pharmacopee = pharmacopeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", id));

        pharmacopee.setStatut(request.getAction());
        Pharmacopee saved = pharmacopeeRepository.save(pharmacopee);

        return pharmacopeeMapper.toDetailDto(saved);
    }
}
