package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteDetailResponse;
import com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.admin.collecte.AdminModerateCollecteRequest;
import com.pharmacopee.ladafura.enums.StatutCollecte;

public interface IAdminCollecteService {

    Page<AdminCollecteSummaryResponse> getAllCollectes(StatutCollecte statut, Pageable pageable);

    AdminCollecteDetailResponse getCollecteById(Long id);

    AdminCollecteDetailResponse moderateCollecte(Long id, AdminModerateCollecteRequest request);

    com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteStatsResponse getStats();

    AdminCollecteDetailResponse updatePhoto(Long id, String photoUrl);
}
