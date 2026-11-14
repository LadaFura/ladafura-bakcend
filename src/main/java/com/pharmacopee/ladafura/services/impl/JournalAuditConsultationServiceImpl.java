package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.JournalAudit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.common.audit.JournalAuditAdminResponse;
import com.pharmacopee.ladafura.dto.common.audit.JournalAuditResponse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.repository.JournalAuditRepository;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditConsultationService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JournalAuditConsultationServiceImpl implements IJournalAuditConsultationService {

    private final IPharmacopeeContexteService contexteService;
    private final JournalAuditRepository journalRepository;

    @Override
    public Page<JournalAuditResponse> getJournalPharmacopee(TypeActionAudit action, String typeEntite, LocalDate debut, LocalDate fin, Pageable pageable) {
        Long pharmacopeeId = contexteService.verifierPermission(IPharmacopeeContexteService.AUDIT_CONSULTER).pharmacopee().getId();
        exigerPeriodeValide(debut, fin);
        return journalRepository.rechercher(pharmacopeeId, action, null, normaliser(typeEntite), debutDe(debut), finDe(fin), pageable)
                .map(JournalAuditConsultationServiceImpl::versReponse);
    }

    @Override
    public Page<JournalAuditAdminResponse> getJournalAdmin(Long pharmacopeeId, TypeActionAudit action, OrigineAction origine, String typeEntite,
                                                           LocalDate debut, LocalDate fin, Pageable pageable) {
        exigerPeriodeValide(debut, fin);
        return journalRepository.rechercher(pharmacopeeId, action, origine, normaliser(typeEntite), debutDe(debut), finDe(fin), pageable)
                .map(JournalAuditConsultationServiceImpl::versReponseAdmin);
    }

    private static void exigerPeriodeValide(LocalDate debut, LocalDate fin) {
        if (debut != null && fin != null && debut.isAfter(fin)) {
            throw new BadRequestException("La date de début doit précéder la date de fin.");
        }
    }

    private static String normaliser(String typeEntite) {
        return typeEntite == null || typeEntite.isBlank() ? null : typeEntite.trim();
    }

    private static LocalDateTime debutDe(LocalDate debut) {
        return debut != null ? debut.atStartOfDay() : null;
    }

    private static LocalDateTime finDe(LocalDate fin) {
        return fin != null ? fin.atTime(LocalTime.MAX) : null;
    }

    private static String auteur(Utilisateur utilisateur) {
        return utilisateur != null ? utilisateur.getPrenom() + " " + utilisateur.getNom() : null;
    }

    /** L'adresse IP est volontairement absente de la vue pharmacopée. */
    private static JournalAuditResponse versReponse(JournalAudit j) {
        return JournalAuditResponse.builder().id(j.getId()).action(j.getAction()).origine(j.getOrigine()).typeEntite(j.getTypeEntite())
                .entiteId(j.getEntiteId()).ancienneValeur(j.getAncienneValeur()).nouvelleValeur(j.getNouvelleValeur())
                .description(j.getDescription()).auteur(auteur(j.getUtilisateur())).dateAction(j.getDateAction()).build();
    }

    private static JournalAuditAdminResponse versReponseAdmin(JournalAudit j) {
        return JournalAuditAdminResponse.builder().id(j.getId())
                .pharmacopeeId(j.getPharmacopee() != null ? j.getPharmacopee().getId() : null)
                .pharmacopeeNom(j.getPharmacopee() != null ? j.getPharmacopee().getNom() : null)
                .action(j.getAction()).origine(j.getOrigine()).typeEntite(j.getTypeEntite()).entiteId(j.getEntiteId())
                .ancienneValeur(j.getAncienneValeur()).nouvelleValeur(j.getNouvelleValeur()).description(j.getDescription())
                .auteur(auteur(j.getUtilisateur())).adresseIp(j.getAdresseIp()).dateAction(j.getDateAction()).build();
    }
}
