package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pharmacopee.ladafura.Models.DemandeContrat;
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
import com.pharmacopee.ladafura.services.interfaces.IAbonnementNotificationService;
import com.pharmacopee.ladafura.services.interfaces.IEmailService.Email;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeNotificationService;

@Service
public class AbonnementNotificationServiceImpl implements IAbonnementNotificationService {

    private static final String LIEN_PAIEMENTS_ADMIN = "/admin/paiements-abonnement";
    private static final String LIEN_CONTRATS_ADMIN = "/admin/demandes-contrat";
    private static final String LIEN_ABONNEMENT = "/pharmacopee/abonnement";
    private static final String ENTITE_LIEE = "ABONNEMENT";

    private final UtilisateurRepository utilisateurRepository;
    private final NotificationRepository notificationRepository;
    private final MembrePharmacopeeRepository membreRepository;
    private final IPharmacopeeNotificationService pharmacopeeNotificationService;
    private final ApplicationEventPublisher evenements;
    private final String emailContact;

    public AbonnementNotificationServiceImpl(UtilisateurRepository utilisateurRepository,
                                             NotificationRepository notificationRepository,
                                             MembrePharmacopeeRepository membreRepository,
                                             IPharmacopeeNotificationService pharmacopeeNotificationService,
                                             ApplicationEventPublisher evenements,
                                             @Value("${ladafura.email.contact:}") String emailContact) {
        this.utilisateurRepository = utilisateurRepository;
        this.notificationRepository = notificationRepository;
        this.membreRepository = membreRepository;
        this.pharmacopeeNotificationService = pharmacopeeNotificationService;
        this.evenements = evenements;
        this.emailContact = emailContact;
    }

    @Override
    public void paiementDeclare(PaiementAbonnement paiement) {
        Pharmacopee pharmacopee = paiement.getAbonnement().getPharmacopee();
        boolean especes = paiement.getModePaiement() == ModePaiementAbonnement.ESPECES;
        String titre = (especes ? "Paiement en espèces à organiser : " : "Paiement d'abonnement à vérifier : ") + pharmacopee.getNom();
        String message = especes
                ? "Contactez " + paiement.getContactNom() + " au " + paiement.getContactTelephone()
                        + " pour la remise de " + montant(paiement) + "."
                : "Un paiement de " + montant(paiement) + " a été déclaré (" + paiement.getModePaiement() + ").";
        List<Utilisateur> administrateurs = administrateurs();
        notifierAdministrateurs(administrateurs, titre, message, String.valueOf(paiement.getId()), LIEN_PAIEMENTS_ADMIN);
        evenements.publishEvent(new Email(destinatairesLadafura(administrateurs), titre, corpsPaiementDeclare(paiement, message)));
    }

    private static String corpsPaiementDeclare(PaiementAbonnement paiement, String message) {
        StringBuilder corps = new StringBuilder(message).append("\n\nPharmacopée : ")
                .append(paiement.getAbonnement().getPharmacopee().getNom())
                .append("\nRéférence interne : ").append(paiement.getReference());
        if (StringUtils.hasText(paiement.getReferenceTransaction())) {
            corps.append("\nRéférence de la transaction : ").append(paiement.getReferenceTransaction());
        }
        if (StringUtils.hasText(paiement.getObservation())) {
            corps.append("\nPrécisions : ").append(paiement.getObservation());
        }
        return corps.append("\n\nÀ traiter depuis l'espace administrateur : Paiements d'abonnement.").toString();
    }

    @Override
    public void demandeContrat(DemandeContrat demande) {
        String titre = "Demande de contrat personnalisé : " + demande.getPharmacopee().getNom();
        String message = "Contactez " + demande.getNomContact() + " au " + demande.getTelephone() + ".";
        List<Utilisateur> administrateurs = administrateurs();
        notifierAdministrateurs(administrateurs, titre, message, String.valueOf(demande.getId()), LIEN_CONTRATS_ADMIN);
        String corps = message
                + (StringUtils.hasText(demande.getEmail()) ? "\nEmail : " + demande.getEmail() : "")
                + "\n\nBesoin exprimé :\n" + demande.getDescriptionBesoin();
        evenements.publishEvent(new Email(destinatairesLadafura(administrateurs), titre, corps));
    }

    @Override
    public void paiementConfirme(PaiementAbonnement paiement) {
        informerPharmacopee(paiement, NiveauNotification.SUCCES, "Abonnement activé",
                "Votre paiement de " + montant(paiement) + " a été confirmé. Votre formule est active.");
    }

    @Override
    public void paiementRejete(PaiementAbonnement paiement, String motif) {
        informerPharmacopee(paiement, NiveauNotification.ATTENTION, "Paiement d'abonnement rejeté",
                "Votre paiement de " + montant(paiement) + " n'a pas pu être validé. Motif : " + motif);
    }

    private void informerPharmacopee(PaiementAbonnement paiement, NiveauNotification niveau, String titre, String message) {
        Pharmacopee pharmacopee = paiement.getAbonnement().getPharmacopee();
        pharmacopeeNotificationService.notifier(pharmacopee, TypeNotification.INFO_IMPORTANTE, niveau, titre, message,
                String.valueOf(paiement.getId()), LIEN_ABONNEMENT);
        List<String> gerant = membreRepository.findFirstByPharmacopeeIdAndEstProprietaireTrue(pharmacopee.getId())
                .map(membre -> membre.getUtilisateur().getEmail())
                .filter(StringUtils::hasText)
                .map(List::of)
                .orElse(List.of());
        evenements.publishEvent(new Email(gerant, titre + " : " + pharmacopee.getNom(), message));
    }

    private List<Utilisateur> administrateurs() {
        return utilisateurRepository.findByRole(Role.ADMINISTRATEUR);
    }

    /** L'adresse de contact dédiée est prioritaire ; à défaut, les comptes administrateurs sont prévenus. */
    private List<String> destinatairesLadafura(List<Utilisateur> administrateurs) {
        if (StringUtils.hasText(emailContact)) {
            return List.of(emailContact.trim());
        }
        return administrateurs.stream().map(Utilisateur::getEmail).filter(StringUtils::hasText).toList();
    }

    private void notifierAdministrateurs(List<Utilisateur> administrateurs, String titre, String message, String referenceId, String lien) {
        for (Utilisateur administrateur : administrateurs) {
            notificationRepository.save(Notification.builder()
                    .utilisateur(administrateur)
                    .titre(titre)
                    .message(message)
                    .type(TypeNotification.INFO_IMPORTANTE)
                    .niveau(NiveauNotification.INFO)
                    .lue(false)
                    .dateNotification(LocalDateTime.now())
                    .typeEntiteLiee(ENTITE_LIEE)
                    .referenceId(referenceId)
                    .lien(lien)
                    .build());
        }
    }

    private static String montant(PaiementAbonnement paiement) {
        return paiement.getMontant().stripTrailingZeros().toPlainString() + " " + paiement.getDevise();
    }
}
