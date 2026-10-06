package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.collaborateur.CreateCollaborateurRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.collaborateur.PharmacopeeCollaborateurResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.collaborateur.UpdateCollaborateurRequest;

public interface IPharmacopeeCollaborateurService {
    Page<PharmacopeeCollaborateurResponse> getCollaborateurs(Pageable pageable);
    PharmacopeeCollaborateurResponse getCollaborateurById(Long id);
    PharmacopeeCollaborateurResponse addCollaborateur(CreateCollaborateurRequest request);
    PharmacopeeCollaborateurResponse updateCollaborateur(Long id, UpdateCollaborateurRequest request);
    void deleteCollaborateur(Long id);
}
