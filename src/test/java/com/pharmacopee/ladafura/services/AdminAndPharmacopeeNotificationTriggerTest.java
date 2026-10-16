package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.admin.notification.AdminNotificationStatsResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.notification.NotificationSummaryResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AdminNotificationMapper;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.impl.AdminNotificationServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeNotificationServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;

@ExtendWith(MockitoExtension.class)
class AdminAndPharmacopeeNotificationTriggerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private IPharmacopeeAuthService pharmacopeeAuthService;

    @Mock
    private AdminNotificationMapper adminMapper;

    @InjectMocks
    private AdminNotificationServiceImpl adminNotificationService;

    @InjectMocks
    private PharmacopeeNotificationServiceImpl pharmacopeeNotificationService;

    private Utilisateur adminUser;
    private Utilisateur otherAdminUser;
    private AgentCollecte agentUser;
    private Collecte collecte;
    private Pharmacopee pharmacopee;

    @BeforeEach
    void setUp() {
        adminUser = new Utilisateur();
        adminUser.setId(1L);
        adminUser.setNom("Traoré");
        adminUser.setPrenom("Amadou");
        adminUser.setEmail("admin@ladafura.ml");
        adminUser.setRole(Role.ADMINISTRATEUR);

        otherAdminUser = new Utilisateur();
        otherAdminUser.setId(2L);
        otherAdminUser.setNom("Coulibaly");
        otherAdminUser.setPrenom("Fatoumata");
        otherAdminUser.setEmail("admin2@ladafura.ml");
        otherAdminUser.setRole(Role.ADMINISTRATEUR);

        agentUser = new AgentCollecte();
        agentUser.setId(10L);
        agentUser.setNom("Diallo");
        agentUser.setPrenom("Moussa");
        agentUser.setEmail("agent@ladafura.ml");
        agentUser.setRole(Role.AGENT_COLLECTE);

        collecte = Collecte.builder()
                .id(42L)
                .agentCollecte(agentUser)
                .build();

        pharmacopee = new Pharmacopee();
        pharmacopee.setId(77L);
        pharmacopee.setNom("Herboristerie Sahel");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(email);
        when(auth.getName()).thenReturn(email);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Admin : création automatique de notification lors d'une nouvelle collecte soumise")
    void notifierCollecteSoumise_NouvelleCollecte_Succes() {
        when(utilisateurRepository.findByRole(Role.ADMINISTRATEUR)).thenReturn(List.of(adminUser));
        when(notificationRepository.existsByUtilisateurIdAndTypeAndReferenceIdAndDateNotificationAfter(
                eq(1L), eq(TypeNotification.COLLECTE_SOUMISE), eq("42"), any(LocalDateTime.class)))
                .thenReturn(false);

        adminNotificationService.notifierCollecteSoumise(collecte, false);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getUtilisateur().getId()).isEqualTo(1L);
        assertThat(saved.getType()).isEqualTo(TypeNotification.COLLECTE_SOUMISE);
        assertThat(saved.getReferenceId()).isEqualTo("42");
        assertThat(saved.getTypeEntiteLiee()).isEqualTo("COLLECTE");
        assertThat(saved.getLien()).isEqualTo("/admin/collectes/42");
        assertThat(saved.getLue()).isFalse();
        assertThat(saved.getTitre()).contains("Nouvelle collecte #42 soumise");
    }

    @Test
    @DisplayName("Admin : création automatique lors d'une collecte corrigée resoumise et prévention des doublons")
    void notifierCollecteSoumise_Resoumise_SansDoublon() {
        when(utilisateurRepository.findByRole(Role.ADMINISTRATEUR)).thenReturn(List.of(adminUser));
        when(notificationRepository.existsByUtilisateurIdAndTypeAndReferenceIdAndDateNotificationAfter(
                eq(1L), eq(TypeNotification.COLLECTE_SOUMISE), eq("42"), any(LocalDateTime.class)))
                .thenReturn(true);

        adminNotificationService.notifierCollecteSoumise(collecte, true);

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Admin : sécurité empêchant l'accès ou la modification de la notification d'un autre administrateur")
    void markAsRead_AutreUtilisateur_ResourceNotFound() {
        mockSecurityContext("admin@ladafura.ml");
        when(utilisateurRepository.findByEmail("admin@ladafura.ml")).thenReturn(Optional.of(adminUser));
        when(notificationRepository.findByIdAndUtilisateurId(500L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminNotificationService.markAsRead(500L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Admin : compteur de notifications non lues exact")
    void getStats_Admin_Succes() {
        mockSecurityContext("admin@ladafura.ml");
        when(utilisateurRepository.findByEmail("admin@ladafura.ml")).thenReturn(Optional.of(adminUser));
        when(notificationRepository.countByUtilisateurId(1L)).thenReturn(8L);
        when(notificationRepository.countByUtilisateurIdAndLueFalse(1L)).thenReturn(3L);
        when(notificationRepository.countByUtilisateurIdAndLueTrue(1L)).thenReturn(5L);

        AdminNotificationStatsResponse stats = adminNotificationService.getStats();

        assertThat(stats.getTotal()).isEqualTo(8L);
        assertThat(stats.getNonLues()).isEqualTo(3L);
        assertThat(stats.getLues()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Pharmacopée : création de notification nouvelle commande avec typeEntiteLiee COMMANDE et sans doublon")
    void notifierPharmacopee_CommandeNouvelle_Succes() {
        when(notificationRepository.existsByPharmacopeeIdAndTypeAndReferenceIdAndDateNotificationAfter(
                eq(77L), eq(TypeNotification.COMMANDE_NOUVELLE), eq("99"), any(LocalDateTime.class)))
                .thenReturn(false);

        pharmacopeeNotificationService.notifier(
                pharmacopee,
                TypeNotification.COMMANDE_NOUVELLE,
                NiveauNotification.INFO,
                "Nouvelle commande reçue",
                "Nouvelle commande #CMD-99",
                "99",
                "/pharmacopee/commandes/99"
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getPharmacopee().getId()).isEqualTo(77L);
        assertThat(saved.getType()).isEqualTo(TypeNotification.COMMANDE_NOUVELLE);
        assertThat(saved.getReferenceId()).isEqualTo("99");
        assertThat(saved.getTypeEntiteLiee()).isEqualTo("COMMANDE");
        assertThat(saved.getLien()).isEqualTo("/pharmacopee/commandes/99");
        assertThat(saved.getLue()).isFalse();
    }

    @Test
    @DisplayName("Pharmacopée : erreur propre lorsque la notification demandée n'existe pas ou appartient à une autre pharmacopée")
    void marquerCommeLuePharmacopee_ResourceNotFound() {
        when(pharmacopeeAuthService.getCurrentPharmacopee()).thenReturn(pharmacopee);
        when(notificationRepository.findByIdAndPharmacopeeId(999L, 77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pharmacopeeNotificationService.marquerCommeLue(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Pharmacopée : compteur summary des notifications non lues")
    void getSummaryPharmacopee_Succes() {
        when(pharmacopeeAuthService.getCurrentPharmacopee()).thenReturn(pharmacopee);
        when(notificationRepository.countByPharmacopeeId(77L)).thenReturn(12L);
        when(notificationRepository.countByPharmacopeeIdAndLueFalse(77L)).thenReturn(4L);

        NotificationSummaryResponse summary = pharmacopeeNotificationService.getSummary();

        assertThat(summary.getTotalNotifications()).isEqualTo(12L);
        assertThat(summary.getTotalNonLues()).isEqualTo(4L);
    }
}
