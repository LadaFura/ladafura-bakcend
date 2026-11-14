package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pharmacopee.ladafura.Models.JournalAudit;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.repository.JournalAuditRepository;
import com.pharmacopee.ladafura.services.impl.JournalAuditServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;

@ExtendWith(MockitoExtension.class)
class JournalAuditServiceImplTest {

    @Mock private JournalAuditRepository journalAuditRepository;

    @InjectMocks private JournalAuditServiceImpl service;

    @BeforeEach
    void preparer() {
        when(journalAuditRepository.save(any(JournalAudit.class))).thenAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void nettoyer() {
        RequestContextHolder.resetRequestAttributes();
    }

    private static EvenementAudit evenement() {
        return EvenementAudit.builder().action(TypeActionAudit.VENTE_ANNULEE).origine(OrigineAction.PHARMACOPEE)
                .typeEntite("Vente").entiteId(12L).description("Erreur de caisse").build();
    }

    @Test
    @DisplayName("l'événement est enregistré avec l'adresse IP de la requête")
    void avecRequete() {
        MockHttpServletRequest requete = new MockHttpServletRequest();
        requete.setRemoteAddr("41.73.100.12");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requete));

        JournalAudit entree = service.tracer(evenement());

        assertThat(entree.getAction()).isEqualTo(TypeActionAudit.VENTE_ANNULEE);
        assertThat(entree.getEntiteId()).isEqualTo(12L);
        assertThat(entree.getAdresseIp()).isEqualTo("41.73.100.12");
    }

    @Test
    @DisplayName("hors requête HTTP (tâche planifiée), l'adresse IP reste vide")
    void sansRequete() {
        assertThat(service.tracer(evenement()).getAdresseIp()).isNull();
    }
}
