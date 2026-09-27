package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.admin.avis.AdminAvisResponse;
import com.pharmacopee.ladafura.dto.admin.avis.AdminModerateAvisRequest;
import com.pharmacopee.ladafura.enums.StatutAvis;

public interface IAdminAvisService {

    Page<AdminAvisResponse> getAllAvis(StatutAvis statut, Long produitId, Pageable pageable);

    AdminAvisResponse moderateAvis(Long id, AdminModerateAvisRequest request);

    void deleteAvis(Long id);
}
