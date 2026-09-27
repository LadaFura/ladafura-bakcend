package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.admin.etude.AdminEtudeRequest;
import com.pharmacopee.ladafura.dto.admin.etude.AdminEtudeResponse;

public interface IAdminEtudeScientifiqueService {

    Page<AdminEtudeResponse> getAllEtudes(Pageable pageable);

    List<AdminEtudeResponse> getEtudesByPlanteId(Long planteId);

    AdminEtudeResponse getEtudeById(Long id);

    AdminEtudeResponse createEtude(AdminEtudeRequest request);

    AdminEtudeResponse updateEtude(Long id, AdminEtudeRequest request);

    void deleteEtude(Long id);
}
