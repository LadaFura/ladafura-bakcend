package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.admin.maladie.AdminMaladieRequest;
import com.pharmacopee.ladafura.dto.admin.maladie.AdminMaladieResponse;

public interface IAdminMaladieService {

    Page<AdminMaladieResponse> getAllMaladies(String search, Pageable pageable);

    AdminMaladieResponse getMaladieById(Long id);

    AdminMaladieResponse createMaladie(AdminMaladieRequest request);

    AdminMaladieResponse updateMaladie(Long id, AdminMaladieRequest request);

    void deleteMaladie(Long id);
}
