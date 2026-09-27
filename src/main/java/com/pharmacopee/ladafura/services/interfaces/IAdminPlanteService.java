package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.admin.plante.AdminModerateVertuRequest;
import com.pharmacopee.ladafura.dto.admin.plante.AdminPlanteRequest;
import com.pharmacopee.ladafura.dto.admin.plante.AdminPlanteResponse;
import com.pharmacopee.ladafura.dto.admin.plante.AdminVertuResponse;
import com.pharmacopee.ladafura.enums.StatutPlante;

public interface IAdminPlanteService {

    Page<AdminPlanteResponse> getAllPlantes(StatutPlante statut, Pageable pageable);

    AdminPlanteResponse getPlanteById(Long id);

    AdminPlanteResponse createPlante(AdminPlanteRequest request);

    AdminPlanteResponse updatePlante(Long id, AdminPlanteRequest request);

    AdminPlanteResponse moderatePlante(Long id, StatutPlante statut);

    AdminVertuResponse moderateVertu(Long vertuId, AdminModerateVertuRequest request);

    void deletePlante(Long id);
}
