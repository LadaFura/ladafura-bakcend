package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationResponse;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AgentNotificationMapper;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.services.impl.AgentNotificationServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

@ExtendWith(MockitoExtension.class)
class AgentNotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Spy
    private AgentNotificationMapper agentNotificationMapper = new AgentNotificationMapper();

    @Mock
    private IAgentAuthService agentAuthService;

    @InjectMocks
    private AgentNotificationServiceImpl agentNotificationService;

    private AgentCollecte agentConnecte;
    private Notification notification;
    private Collecte collecte;

    @BeforeEach
    void setUp() {
        agentConnecte = new AgentCollecte();
        agentConnecte.setId(10L);
        agentConnecte.setNom("Coulibaly");
        agentConnecte.setPrenom("Oumar");

        notification = Notification.builder()
                .id(1L)
                .utilisateur(agentConnecte)
                .titre("Collecte validée")
                .message("Votre fiche #100 a été validée.")
                .type(TypeNotification.COLLECTE_VALIDEE)
                .niveau(NiveauNotification.SUCCES)
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .referenceId("100")
                .lien("/api/v1/agent/collectes/100")
                .build();

        collecte = Collecte.builder()
                .id(100L)
                .statut(StatutCollecte.SOUMISE)
                .agentCollecte(agentConnecte)
                .build();
    }

    @Test
    @DisplayName("Lister les notifications avec double filtre (lue et type)")
    void getMyNotifications_AvecFiltres() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification), pageable, 1);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(notificationRepository.findByUtilisateurIdAndLueAndType(10L, false, TypeNotification.COLLECTE_VALIDEE, pageable))
                .thenReturn(page);

        Page<AgentNotificationResponse> result = agentNotificationService.getMyNotifications(false, TypeNotification.COLLECTE_VALIDEE, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitre()).isEqualTo("Collecte validée");
    }

    @Test
    @DisplayName("Lister les notifications avec filtre lue seulement")
    void getMyNotifications_FiltreLue() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification), pageable, 1);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(notificationRepository.findByUtilisateurIdAndLue(10L, false, pageable)).thenReturn(page);

        Page<AgentNotificationResponse> result = agentNotificationService.getMyNotifications(false, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lister les notifications sans filtre")
    void getMyNotifications_SansFiltres() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification), pageable, 1);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(notificationRepository.findByUtilisateurId(10L, pageable)).thenReturn(page);

        Page<AgentNotificationResponse> result = agentNotificationService.getMyNotifications(null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Consulter une notification et la marquer automatiquement comme lue")
    void getNotificationById_Succes() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(notificationRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        AgentNotificationResponse response = agentNotificationService.getNotificationById(1L);

        assertThat(response).isNotNull();
        assertThat(notification.getLue()).isTrue();
        assertThat(notification.getDateLecture()).isNotNull();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("Consulter notification : 404 si introuvable")
    void getNotificationById_NotFound() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(notificationRepository.findByIdAndUtilisateurId(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agentNotificationService.getNotificationById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Marquer explicitement une notification comme lue")
    void markAsRead_Succes() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(notificationRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        AgentNotificationResponse response = agentNotificationService.markAsRead(1L);

        assertThat(response).isNotNull();
        assertThat(notification.getLue()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("Marquer toutes les notifications comme lues")
    void markAllAsRead_Succes() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);

        agentNotificationService.markAllAsRead();

        verify(notificationRepository).markAllAsReadByUtilisateurId(10L);
    }

    @Test
    @DisplayName("Supprimer une notification")
    void deleteNotification_Succes() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(notificationRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(notification));

        agentNotificationService.deleteNotification(1L);

        verify(notificationRepository).delete(notification);
    }

    @Test
    @DisplayName("Statistiques de notifications de l'agent")
    void getStats_Succes() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(notificationRepository.countByUtilisateurId(10L)).thenReturn(10L);
        when(notificationRepository.countByUtilisateurIdAndLueFalse(10L)).thenReturn(3L);
        when(notificationRepository.countByUtilisateurIdAndLueTrue(10L)).thenReturn(7L);
        when(notificationRepository.countByUtilisateurIdAndType(10L, TypeNotification.COLLECTE_VALIDEE)).thenReturn(5L);
        when(notificationRepository.countByUtilisateurIdAndType(10L, TypeNotification.COLLECTE_REJETEE)).thenReturn(2L);
        when(notificationRepository.countByUtilisateurIdAndType(10L, TypeNotification.COLLECTE_CORRECTION)).thenReturn(1L);
        when(notificationRepository.countByUtilisateurIdAndType(10L, TypeNotification.COLLECTE_STATUT)).thenReturn(1L);
        when(notificationRepository.countByUtilisateurIdAndType(10L, TypeNotification.INFO_IMPORTANTE)).thenReturn(1L);

        AgentNotificationStatsResponse stats = agentNotificationService.getStats();

        assertThat(stats).isNotNull();
        assertThat(stats.getTotal()).isEqualTo(10L);
        assertThat(stats.getNonLues()).isEqualTo(3L);
        assertThat(stats.getValidations()).isEqualTo(5L);
        assertThat(stats.getRejets()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Notification : validation de collecte")
    void notifierValidationCollecte_Succes() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification notif = agentNotificationService.notifierValidationCollecte(collecte);

        assertThat(notif).isNotNull();
        assertThat(notif.getType()).isEqualTo(TypeNotification.COLLECTE_VALIDEE);
        assertThat(notif.getNiveau()).isEqualTo(NiveauNotification.SUCCES);
        assertThat(notif.getReferenceId()).isEqualTo("100");
    }

    @Test
    @DisplayName("Notification : rejet de collecte avec motif")
    void notifierRejetCollecte_Succes() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification notif = agentNotificationService.notifierRejetCollecte(collecte, "Photos inexploitables");

        assertThat(notif).isNotNull();
        assertThat(notif.getType()).isEqualTo(TypeNotification.COLLECTE_REJETEE);
        assertThat(notif.getNiveau()).isEqualTo(NiveauNotification.ATTENTION);
        assertThat(notif.getMessage()).contains("Photos inexploitables");
        assertThat(notif.getLien()).isEqualTo("/api/v1/agent/suivi/100/motif-rejet");
    }

    @Test
    @DisplayName("Notification : demande de correction")
    void notifierDemandeCorrection_Succes() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification notif = agentNotificationService.notifierDemandeCorrection(collecte, "Préciser la posologie");

        assertThat(notif).isNotNull();
        assertThat(notif.getType()).isEqualTo(TypeNotification.COLLECTE_CORRECTION);
    }

    @Test
    @DisplayName("Notification : changement de statut")
    void notifierChangementStatut_Succes() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification notif = agentNotificationService.notifierChangementStatut(collecte, StatutCollecte.SOUMISE, StatutCollecte.EN_EXAMEN);

        assertThat(notif).isNotNull();
        assertThat(notif.getType()).isEqualTo(TypeNotification.COLLECTE_STATUT);
    }

    @Test
    @DisplayName("Notification : communication administrative")
    void notifierMessageAdmin_Succes() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification notif = agentNotificationService.notifierMessageAdmin(agentConnecte, "Rappel protocole", "Merci d'utiliser le nouveau formulaire", NiveauNotification.INFO);

        assertThat(notif).isNotNull();
        assertThat(notif.getType()).isEqualTo(TypeNotification.INFO_IMPORTANTE);
    }
}
