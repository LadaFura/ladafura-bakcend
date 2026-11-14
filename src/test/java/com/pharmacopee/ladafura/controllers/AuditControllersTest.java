package com.pharmacopee.ladafura.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.pharmacopee.ladafura.controllers.admin.AdminAuditController;
import com.pharmacopee.ladafura.controllers.pharmacopee.PharmacopeeAuditController;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditConsultationService;

@ExtendWith(MockitoExtension.class)
class AuditControllersTest {

    private static final Pageable PAGE = PageRequest.of(0, 10);

    @Mock private IJournalAuditConsultationService service;

    @InjectMocks private PharmacopeeAuditController pharmacopeeController;
    @InjectMocks private AdminAuditController adminController;

    @Test
    @DisplayName("GET /api/v1/pharmacopee/audit - 200 OK")
    void pharmacopee() {
        when(service.getJournalPharmacopee(null, null, null, null, PAGE)).thenReturn(new PageImpl<>(List.of()));

        assertThat(pharmacopeeController.getJournal(null, null, null, null, PAGE).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/v1/admin/audit - 200 OK")
    void admin() {
        when(service.getJournalAdmin(null, null, null, null, null, null, PAGE)).thenReturn(new PageImpl<>(List.of()));

        assertThat(adminController.getJournal(null, null, null, null, null, null, PAGE).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
