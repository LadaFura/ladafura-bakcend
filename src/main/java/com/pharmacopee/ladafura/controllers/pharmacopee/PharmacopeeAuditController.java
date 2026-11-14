package com.pharmacopee.ladafura.controllers.pharmacopee;

import java.time.LocalDate;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.common.audit.JournalAuditResponse;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditConsultationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/audit")
@RequiredArgsConstructor
@Tag(name = "Pharmacopée - Audit", description = "Journal des opérations sensibles de la pharmacopée")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeAuditController {

    private final IJournalAuditConsultationService consultationService;

    @GetMapping
    @Operation(summary = "Consulter le journal d'audit", description = "Permission AUDIT_CONSULTER. Filtres facultatifs ; du plus récent au plus ancien.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    @ApiResponse(responseCode = "400", description = "Période invalide")
    @ApiResponse(responseCode = "403", description = "Permission manquante")
    public ResponseEntity<Page<JournalAuditResponse>> getJournal(@RequestParam(required = false) TypeActionAudit action,
                                                                 @RequestParam(required = false) String typeEntite,
                                                                 @RequestParam(required = false) LocalDate debut,
                                                                 @RequestParam(required = false) LocalDate fin,
                                                                 @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(consultationService.getJournalPharmacopee(action, typeEntite, debut, fin, pageable));
    }
}
