package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.Models.DemandeContrat;
import com.pharmacopee.ladafura.Models.PaiementAbonnement;

/** Prévient, dans le portail et par email, les personnes qui doivent se contacter autour d'un abonnement. */
public interface IAbonnementNotificationService {

    /** Paiement à vérifier, ou demande de prise de contact s'il est en espèces. */
    void paiementDeclare(PaiementAbonnement paiement);

    void demandeContrat(DemandeContrat demande);

    void paiementConfirme(PaiementAbonnement paiement);

    void paiementRejete(PaiementAbonnement paiement, String motif);
}
