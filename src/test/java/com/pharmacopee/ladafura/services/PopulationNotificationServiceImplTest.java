package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationCountResponse;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationResponse;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationNotificationMapper;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.services.impl.PopulationNotificationServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

@ExtendWith(MockitoExtension.class)
class PopulationNotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private IPopulationAuthService populationAuthService;

    @Spy
    private PopulationNotificationMapper mapper = new PopulationNotificationMapper();

    @InjectMocks
    private PopulationNotificationServiceImpl notificationService;

    private Utilisateur currentUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        currentUser = new Utilisateur();
        currentUser.setId(10L);
        currentUser.setEmail("client@ladafura.ml");
        currentUser.setNom("Coulibaly");
        currentUser.setPrenom("Awa");
        currentUser.setRole(Role.POPULATION);
        currentUser.setStatut(StatutUtilisateur.ACTIF);

        testNotification = Notification.builder()
                .id(1L)
                .utilisateur(currentUser)
                .titre("Commande expédiée")
                .message("Votre commande CMD-001 est en cours de livraison")
                .type(TypeNotification.COMMANDE_STATUT)
                .niveau(NiveauNotification.INFO)
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .referenceId("CMD-001")
                .lien("/api/v1/population/commandes/1")
                .build();
    }

    @Test
    @DisplayName("getMesNotifications - Sans filtre")
    void getMesNotifications_NoFilter() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        Pageable pageable = PageRequest.of(0, 10);
        when(notificationRepository.findByUtilisateurId(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(testNotification)));

        Page<PopulationNotificationResponse> result = notificationService.getMesNotifications(null, null, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getTitre()).isEqualTo("Commande expédiée");
    }

    @Test
    @DisplayName("getMesNotifications - Filtre par lue et type")
    void getMesNotifications_FilterLueAndType() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        Pageable pageable = PageRequest.of(0, 10);
        when(notificationRepository.findByUtilisateurIdAndLueAndType(10L, false, TypeNotification.COMMANDE_STATUT, pageable))
                .thenReturn(new PageImpl<>(List.of(testNotification)));

        Page<PopulationNotificationResponse> result = notificationService.getMesNotifications(false, TypeNotification.COMMANDE_STATUT, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo(TypeNotification.COMMANDE_STATUT);
    }

    @Test
    @DisplayName("getNotificationById - Succès et marquage automatique comme lue")
    void getNotificationById_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(notificationRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PopulationNotificationResponse response = notificationService.getNotificationById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLue()).isTrue();
        assertThat(response.getDateLecture()).isNotNull();
        verify(notificationRepository).save(testNotification);
    }

    @Test
    @DisplayName("getNotificationById - Introuvable lance ResourceNotFoundException")
    void getNotificationById_NotFound() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(notificationRepository.findByIdAndUtilisateurId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("markAsRead - Succès")
    void markAsRead_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(notificationRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PopulationNotificationResponse response = notificationService.markAsRead(1L);

        assertThat(response).isNotNull();
        assertThat(response.getLue()).isTrue();
        verify(notificationRepository).save(testNotification);
    }

    @Test
    @DisplayName("markAllAsRead - Succès")
    void markAllAsRead_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);

        notificationService.markAllAsRead();

        verify(notificationRepository).markAllAsReadByUtilisateurId(10L);
    }

    @Test
    @DisplayName("deleteNotification - Succès")
    void deleteNotification_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(notificationRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(testNotification));

        notificationService.deleteNotification(1L);

        verify(notificationRepository).delete(testNotification);
    }

    @Test
    @DisplayName("clearAllReadNotifications - Succès")
    void clearAllReadNotifications_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);

        notificationService.clearAllReadNotifications();

        verify(notificationRepository).deleteAllReadByUtilisateurId(10L);
    }

    @Test
    @DisplayName("getStats - Succès")
    void getStats_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(notificationRepository.countByUtilisateurId(10L)).thenReturn(10L);
        when(notificationRepository.countByUtilisateurIdAndLueFalse(10L)).thenReturn(3L);
        when(notificationRepository.countByUtilisateurIdAndLueTrue(10L)).thenReturn(7L);
        when(notificationRepository.countByUtilisateurIdAndType(10L, TypeNotification.COMMANDE_NOUVELLE)).thenReturn(2L);
        when(notificationRepository.countByUtilisateurIdAndType(10L, TypeNotification.COMMANDE_STATUT)).thenReturn(4L);
        when(notificationRepository.countByUtilisateurIdAndType(10L, TypeNotification.AVIS_NOUVEAU)).thenReturn(1L);
        when(notificationRepository.countByUtilisateurIdAndType(10L, TypeNotification.INFO_IMPORTANTE)).thenReturn(2L);
        when(notificationRepository.countByUtilisateurIdAndType(10L, TypeNotification.STOCK_ALERTE)).thenReturn(1L);

        PopulationNotificationStatsResponse stats = notificationService.getStats();

        assertThat(stats.getTotal()).isEqualTo(10L);
        assertThat(stats.getNonLues()).isEqualTo(3L);
        assertThat(stats.getLues()).isEqualTo(7L);
        assertThat(stats.getCommandes()).isEqualTo(6L);
        assertThat(stats.getAvis()).isEqualTo(1L);
        assertThat(stats.getAlertes()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getUnreadCount - Succès")
    void getUnreadCount_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(notificationRepository.countByUtilisateurIdAndLueFalse(10L)).thenReturn(4L);

        PopulationNotificationCountResponse count = notificationService.getUnreadCount();

        assertThat(count.getCount()).isEqualTo(4L);
    }

    @Test
    @DisplayName("envoyerNotification - Succès")
    void envoyerNotification_Success() {
        notificationService.envoyerNotification(
                currentUser,
                "Alerte Santé",
                "Campagne de sensibilisation",
                TypeNotification.INFO_IMPORTANTE,
                NiveauNotification.ATTENTION,
                "REF-99",
                "/info"
        );

        verify(notificationRepository).save(any(Notification.class));
    }
}
