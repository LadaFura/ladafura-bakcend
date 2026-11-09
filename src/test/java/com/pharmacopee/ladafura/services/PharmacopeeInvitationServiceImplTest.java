package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.InvitationResponse;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.QuotaExceededException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeInvitationServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

@ExtendWith(MockitoExtension.class)
class PharmacopeeInvitationServiceImplTest {

    private static final Long UTILISATEUR_ID = 2L;
    private static final Long PHARMACOPEE_ID = 5L;

    @Mock private IPharmacopeeAuthService pharmacopeeAuthService;
    @Mock private IPharmacopeeContexteService contexteService;
    @Mock private IPharmacopeeQuotaService quotaService;
    @Mock private IJournalAuditService journalAuditService;
    @Mock private MembrePharmacopeeRepository membreRepository;

    @InjectMocks private PharmacopeeInvitationServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine de Bamako").build();

    @BeforeEach
    void preparer() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UTILISATEUR_ID);
        when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(utilisateur);
    }

    private MembrePharmacopee invitation(LocalDateTime expiration) {
        return MembrePharmacopee.builder().id(9L).pharmacopee(pharmacopee).statut(StatutMembre.INVITE).dateExpirationInvitation(expiration).build();
    }

    private void invitationTrouvee(MembrePharmacopee invitation) {
        when(membreRepository.findByIdAndUtilisateurIdAndStatut(9L, UTILISATEUR_ID, StatutMembre.INVITE)).thenReturn(Optional.of(invitation));
    }

    @Test
    @DisplayName("les invitations expirées ne sont pas listées")
    void listeSansExpirees() {
        when(membreRepository.findByUtilisateurIdAndStatut(UTILISATEUR_ID, StatutMembre.INVITE))
                .thenReturn(List.of(invitation(LocalDateTime.now().plusDays(1)), invitation(LocalDateTime.now().minusDays(1))));

        List<InvitationResponse> invitations = service.getMesInvitations();

        assertThat(invitations).hasSize(1);
        assertThat(invitations.get(0).getPharmacopeeNom()).isEqualTo("Officine de Bamako");
    }

    @Test
    @DisplayName("accepter : le membre devient ACTIF")
    void acceptation() {
        MembrePharmacopee invitation = invitation(LocalDateTime.now().plusDays(1));
        invitationTrouvee(invitation);

        service.accepter(9L);

        assertThat(invitation.getStatut()).isEqualTo(StatutMembre.ACTIF);
        assertThat(invitation.getDateAdhesion()).isNotNull();
    }

    @Test
    @DisplayName("une invitation expirée passe RETIRE et donne 409")
    void acceptationExpiree() {
        MembrePharmacopee invitation = invitation(LocalDateTime.now().minusMinutes(1));
        invitationTrouvee(invitation);

        assertThatThrownBy(() -> service.accepter(9L)).isInstanceOf(ConflictException.class);
        assertThat(invitation.getStatut()).isEqualTo(StatutMembre.RETIRE);
    }

    @Test
    @DisplayName("le quota de la pharmacopée bloque l'acceptation")
    void acceptationQuota() {
        MembrePharmacopee invitation = invitation(LocalDateTime.now().plusDays(1));
        invitationTrouvee(invitation);
        doThrow(new QuotaExceededException("limite")).when(quotaService)
                .verifierQuota(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS);

        assertThatThrownBy(() -> service.accepter(9L)).isInstanceOf(QuotaExceededException.class);
        assertThat(invitation.getStatut()).isEqualTo(StatutMembre.INVITE);
    }

    @Test
    @DisplayName("l'invitation d'un autre utilisateur n'est pas trouvée")
    void invitationDUnAutre() {
        when(membreRepository.findByIdAndUtilisateurIdAndStatut(9L, UTILISATEUR_ID, StatutMembre.INVITE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.accepter(9L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("refuser : l'invitation passe RETIRE")
    void refus() {
        MembrePharmacopee invitation = invitation(LocalDateTime.now().plusDays(1));
        invitationTrouvee(invitation);

        service.refuser(9L);

        assertThat(invitation.getStatut()).isEqualTo(StatutMembre.RETIRE);
    }
}
