package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.DemandeContrat;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.Models.PaiementAbonnement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.enums.ModePaiementAbonnement;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.impl.AbonnementNotificationServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IEmailService.Email;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeNotificationService;

class AbonnementNotificationServiceImplTest {

    private final UtilisateurRepository utilisateurRepository = mock(UtilisateurRepository.class);
    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final MembrePharmacopeeRepository membreRepository = mock(MembrePharmacopeeRepository.class);
    private final IPharmacopeeNotificationService notificationPharmacopee = mock(IPharmacopeeNotificationService.class);
    private final ApplicationEventPublisher evenements = mock(ApplicationEventPublisher.class);

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(1L).nom("Officine de Ségou").build();
    private final Utilisateur administrateur = new Utilisateur();
    private PaiementAbonnement paiement;

    @BeforeEach
    void preparer() {
        administrateur.setEmail("admin@ladafura.ml");
        when(utilisateurRepository.findByRole(Role.ADMINISTRATEUR)).thenReturn(List.of(administrateur));
        paiement = PaiementAbonnement.builder().id(7L).reference("PAB-1").montant(new BigDecimal("96000.00")).devise("XOF")
                .abonnement(Abonnement.builder().pharmacopee(pharmacopee).build())
                .modePaiement(ModePaiementAbonnement.ESPECES)
                .contactNom("Awa Traoré").contactTelephone("+223 70 11 22 33").observation("Le matin").build();
    }

    private AbonnementNotificationServiceImpl service(String emailContact) {
        return new AbonnementNotificationServiceImpl(utilisateurRepository, notificationRepository, membreRepository,
                notificationPharmacopee, evenements, emailContact);
    }

    private Email emailPublie() {
        ArgumentCaptor<Object> evenement = ArgumentCaptor.forClass(Object.class);
        verify(evenements).publishEvent(evenement.capture());
        return (Email) evenement.getValue();
    }

    @Test
    @DisplayName("une demande en espèces transmet le contact à LADAFURA, dans le portail et par email")
    void especes() {
        service("contact@ladafura.ml").paiementDeclare(paiement);

        ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notification.capture());
        assertThat(notification.getValue().getMessage()).contains("Awa Traoré", "+223 70 11 22 33");
        assertThat(notification.getValue().getLien()).isEqualTo("/admin/paiements-abonnement");

        Email email = emailPublie();
        assertThat(email.destinataires()).containsExactly("contact@ladafura.ml");
        assertThat(email.corps()).contains("+223 70 11 22 33", "Le matin", "96000 XOF");
    }

    @Test
    @DisplayName("sans adresse de contact, les comptes administrateurs reçoivent l'email")
    void repliSurAdministrateurs() {
        service("").paiementDeclare(paiement);

        assertThat(emailPublie().destinataires()).containsExactly("admin@ladafura.ml");
    }

    @Test
    @DisplayName("une demande de contrat prévient LADAFURA")
    void demandeContrat() {
        service("contact@ladafura.ml").demandeContrat(DemandeContrat.builder().id(3L).pharmacopee(pharmacopee)
                .nomContact("Oumar Keita").telephone("+223 66 00 00 01").descriptionBesoin("Dix officines").build());

        assertThat(emailPublie().corps()).contains("Oumar Keita", "Dix officines");
    }

    @Test
    @DisplayName("un rejet informe la pharmacopée et son gérant, avec le motif")
    void rejet() {
        Utilisateur gerant = new Utilisateur();
        gerant.setEmail("gerant@officine.ml");
        when(membreRepository.findFirstByPharmacopeeIdAndEstProprietaireTrue(1L))
                .thenReturn(Optional.of(MembrePharmacopee.builder().utilisateur(gerant).build()));

        service("").paiementRejete(paiement, "Montant non reçu");

        verify(notificationPharmacopee).notifier(eq(pharmacopee), eq(TypeNotification.INFO_IMPORTANTE), eq(NiveauNotification.ATTENTION),
                any(), any(), any(), eq("/pharmacopee/abonnement"));
        Email email = emailPublie();
        assertThat(email.destinataires()).containsExactly("gerant@officine.ml");
        assertThat(email.corps()).contains("Montant non reçu");
    }
}
