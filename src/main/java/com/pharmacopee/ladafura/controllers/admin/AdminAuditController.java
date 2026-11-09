package com.pharmacopee.ladafura.controllers.admin;

import java.time.LocalDate;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.common.audit.JournalAuditAdminResponse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditConsultationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
@Tag(name = "Admin - Audit", description = "Journal des opérations sensibles de toute la plateforme")
@SecurityRequirement(name = "bearerAuth")
public class AdminAuditController {

    private final IJournalAuditConsultationService consultationService;

    @GetMapping
    @Operation(summary = "Consulter le journal d'audit", description = "Filtres facultatifs ; du plus récent au plus ancien.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    @ApiResponse(responseCode = "400", description = "Période invalide")
    public ResponseEntity<Page<JournalAuditAdminResponse>> getJournal(@RequestParam(required = false) Long pharmacopeeId,
                                                                      @RequestParam(required = false) TypeActionAudit action,
                                                                      @RequestParam(required = false) OrigineAction origine,
                                                                      @RequestParam(required = false) String typeEntite,
                                                                      @RequestParam(required = false) LocalDate debut,
                                                                      @RequestParam(required = false) LocalDate fin,
                                                                      @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(consultationService.getJournalAdmin(pharmacopeeId, action, origine, typeEntite, debut, fin, pageable));
    }
}
