package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminCreatePharmacopeeRequest;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminModeratePharmacopeeRequest;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminPharmacopeeDetailResponse;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminUpdatePharmacopeeRequest;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;

public interface IAdminPharmacopeeService {

    Page<AdminPharmacopeeSummaryResponse> getAllPharmacopees(StatutPharmacopee statut, Pageable pageable);

    AdminPharmacopeeDetailResponse getPharmacopeeById(Long id);

    AdminPharmacopeeDetailResponse createPharmacopee(AdminCreatePharmacopeeRequest request);

    AdminPharmacopeeDetailResponse updatePharmacopee(Long id, AdminUpdatePharmacopeeRequest request);

    AdminPharmacopeeDetailResponse moderatePharmacopee(Long id, AdminModeratePharmacopeeRequest request);

    void deletePharmacopee(Long id);
}
