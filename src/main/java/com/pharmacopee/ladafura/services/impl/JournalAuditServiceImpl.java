package com.pharmacopee.ladafura.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pharmacopee.ladafura.Models.JournalAudit;
import com.pharmacopee.ladafura.repository.JournalAuditRepository;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class JournalAuditServiceImpl implements IJournalAuditService {

    private final JournalAuditRepository journalAuditRepository;

    @Override
    public JournalAudit tracer(EvenementAudit evenement) {
        return journalAuditRepository.save(JournalAudit.builder()
                .action(evenement.action())
                .origine(evenement.origine())
                .pharmacopee(evenement.pharmacopee())
                .utilisateur(evenement.utilisateur())
                .typeEntite(evenement.typeEntite())
                .entiteId(evenement.entiteId())
                .ancienneValeur(evenement.ancienneValeur())
                .nouvelleValeur(evenement.nouvelleValeur())
                .description(evenement.description())
                .adresseIp(adresseIpCourante())
                .build());
    }

    private static String adresseIpCourante() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributs) {
            return attributs.getRequest().getRemoteAddr();
        }
        return null;
    }
}
