package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.JournalAudit;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.common.audit.JournalAuditAdminResponse;
import com.pharmacopee.ladafura.dto.common.audit.JournalAuditResponse;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.repository.JournalAuditRepository;
import com.pharmacopee.ladafura.services.impl.JournalAuditConsultationServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;

@ExtendWith(MockitoExtension.class)
class JournalAuditConsultationServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;
    private static final Pageable PAGE = PageRequest.of(0, 10);

    @Mock private IPharmacopeeContexteService contexteService;
    @Mock private JournalAuditRepository journalRepository;

    @InjectMocks private JournalAuditConsultationServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine").build();

    private void contexte() {
        when(contexteService.verifierPermission(IPharmacopeeContexteService.AUDIT_CONSULTER))
                .thenReturn(new ContexteMembre(MembrePharmacopee.builder().id(1L).build(), pharmacopee, Set.of()));
    }

    private JournalAudit entree() {
        Utilisateur auteur = new Utilisateur();
        auteur.setPrenom("Awa");
        auteur.setNom("Traoré");
        return JournalAudit.builder().id(9L).pharmacopee(pharmacopee).utilisateur(auteur).action(TypeActionAudit.VENTE_ANNULEE)
                .typeEntite("Vente").entiteId(3L).adresseIp("41.73.100.12").build();
    }

    @Test
    @DisplayName("pharmacopée : la recherche est forcée sur la pharmacopée du contexte")
    void pharmacopeeForcee() {
        contexte();
        when(journalRepository.rechercher(eq(PHARMACOPEE_ID), any(), any(), any(), any(), any(), eq(PAGE))).thenReturn(new PageImpl<>(List.of(entree())));

        JournalAuditResponse reponse = service.getJournalPharmacopee(null, null, null, null, PAGE).getContent().get(0);

        assertThat(reponse.getAuteur()).isEqualTo("Awa Traoré");
        assertThat(JournalAuditResponse.class.getDeclaredFields()).extracting("name").doesNotContain("adresseIp");
    }

    @Test
    @DisplayName("sans la permission AUDIT_CONSULTER, la lecture est refusée")
    void permissionManquante() {
        when(contexteService.verifierPermission(IPharmacopeeContexteService.AUDIT_CONSULTER)).thenThrow(new ForbiddenException("non"));

        assertThatThrownBy(() -> service.getJournalPharmacopee(null, null, null, null, PAGE)).isInstanceOf(ForbiddenException.class);
        verify(journalRepository, never()).rechercher(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("administrateur : l'adresse IP et la pharmacopée sont présentes")
    void vueAdmin() {
        when(journalRepository.rechercher(isNull(), any(), any(), any(), any(), any(), eq(PAGE))).thenReturn(new PageImpl<>(List.of(entree())));

        JournalAuditAdminResponse reponse = service.getJournalAdmin(null, null, null, null, null, null, PAGE).getContent().get(0);

        assertThat(reponse.getAdresseIp()).isEqualTo("41.73.100.12");
        assertThat(reponse.getPharmacopeeNom()).isEqualTo("Officine");
    }

    @Test
    @DisplayName("une action du système n'a pas d'auteur")
    void actionSysteme() {
        when(journalRepository.rechercher(any(), any(), any(), any(), any(), any(), eq(PAGE)))
                .thenReturn(new PageImpl<>(List.of(JournalAudit.builder().id(1L).typeEntite("Abonnement").build())));

        JournalAuditAdminResponse reponse = service.getJournalAdmin(null, null, null, null, null, null, PAGE).getContent().get(0);

        assertThat(reponse.getAuteur()).isNull();
        assertThat(reponse.getPharmacopeeId()).isNull();
    }

    @Test
    @DisplayName("une période inversée est refusée")
    void periodeInvalide() {
        LocalDate fin = LocalDate.of(2026, 11, 1);

        assertThatThrownBy(() -> service.getJournalAdmin(null, null, null, null, fin.plusDays(1), fin, PAGE)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("la date de fin inclut toute la journée")
    void bornesDeDates() {
        LocalDate jour = LocalDate.of(2026, 11, 9);
        when(journalRepository.rechercher(any(), any(), any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

        service.getJournalAdmin(null, null, null, " Vente ", jour, jour, PAGE);

        verify(journalRepository).rechercher(isNull(), isNull(), isNull(), eq("Vente"), eq(jour.atStartOfDay()), eq(jour.atTime(LocalTime.MAX)), eq(PAGE));
    }
}
