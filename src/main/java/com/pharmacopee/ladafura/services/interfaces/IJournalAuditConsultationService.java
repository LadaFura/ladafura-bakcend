package com.pharmacopee.ladafura.services.interfaces;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.common.audit.JournalAuditAdminResponse;
import com.pharmacopee.ladafura.dto.common.audit.JournalAuditResponse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.TypeActionAudit;

/** Lecture du journal d'audit. Le journal est en ajout seul : aucune modification n'est exposée. */
public interface IJournalAuditConsultationService {

    /** Entrées de la pharmacopée courante, sans adresse IP. */
    Page<JournalAuditResponse> getJournalPharmacopee(TypeActionAudit action, String typeEntite, LocalDate debut, LocalDate fin, Pageable pageable);

    /** Toutes les entrées, pour l'administrateur LADAFURA. */
    Page<JournalAuditAdminResponse> getJournalAdmin(Long pharmacopeeId, TypeActionAudit action, OrigineAction origine, String typeEntite,
                                                    LocalDate debut, LocalDate fin, Pageable pageable);
}
