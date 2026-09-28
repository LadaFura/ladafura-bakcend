package com.pharmacopee.ladafura.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pharmacopee.ladafura.controllers.admin.AdminAvisController;
import com.pharmacopee.ladafura.dto.admin.avis.AdminAvisResponse;
import com.pharmacopee.ladafura.dto.admin.avis.AdminModerateAvisRequest;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.services.interfaces.IAdminAvisService;

@ExtendWith(MockitoExtension.class)
class AdminAvisControllerTest {

    @Mock
    private IAdminAvisService avisService;

    @InjectMocks
    private AdminAvisController avisController;

    @Test
    @DisplayName("GET /api/v1/admin/avis - 200 OK")
    void getAllAvis_200OK() {
        AdminAvisResponse response = AdminAvisResponse.builder()
                .id(1L)
                .note(5)
                .commentaire("Excellent")
                .statut(StatutAvis.EN_ATTENTE)
                .dateAvis(LocalDateTime.now())
                .utilisateurId(10L)
                .nomCompletUtilisateur("Fatoumata Traoré")
                .produitId(5L)
                .nomProduit("Sirop Kinkéliba")
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(avisService.getAllAvis(eq(StatutAvis.EN_ATTENTE), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        ResponseEntity<Page<AdminAvisResponse>> result = avisController.getAllAvis(StatutAvis.EN_ATTENTE, null, pageable);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).hasSize(1);
        assertThat(result.getBody().getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getBody().getContent().get(0).getNomCompletUtilisateur()).isEqualTo("Fatoumata Traoré");
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/avis/{id}/moderate - 200 OK")
    void moderateAvis_200OK() {
        AdminModerateAvisRequest request = AdminModerateAvisRequest.builder()
                .action(StatutAvis.PUBLIE)
                .motif("Validé par modérateur")
                .build();

        AdminAvisResponse response = AdminAvisResponse.builder()
                .id(1L)
                .statut(StatutAvis.PUBLIE)
                .build();

        when(avisService.moderateAvis(eq(1L), any(AdminModerateAvisRequest.class)))
                .thenReturn(response);

        ResponseEntity<AdminAvisResponse> result = avisController.moderateAvis(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getStatut()).isEqualTo(StatutAvis.PUBLIE);
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/avis/{id} - 204 No Content")
    void deleteAvis_204NoContent() {
        ResponseEntity<Void> result = avisController.deleteAvis(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(avisService).deleteAvis(1L);
    }
}
