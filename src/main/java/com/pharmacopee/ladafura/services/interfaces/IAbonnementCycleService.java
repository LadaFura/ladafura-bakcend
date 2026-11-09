package com.pharmacopee.ladafura.services.interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.DemandeContrat;
import com.pharmacopee.ladafura.Models.LimiteOffre;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.enums.TypeEvenementAbonnement;

/** Cycle de vie des abonnements, commun aux espaces pharmacopée, administrateur et aux tâches planifiées. */
public interface IAbonnementCycleService {

    String OFFRE_GRATUITE = "GRATUITE";

    /** Quota négocié d'un contrat personnalisé. */
    record QuotaNegocie(LimiteOffre limite, Integer valeurMaximale, boolean illimite) {
    }

    /** Crée un abonnement EN_ATTENTE_PAIEMENT avec l'instantané des conditions de l'offre. */
    Abonnement creerDepuisOffre(Pharmacopee pharmacopee, OffreAbonnement offre, MembrePharmacopee souscritPar);

    /** Crée un abonnement EN_ATTENTE_PAIEMENT aux conditions négociées (durée éventuellement supérieure à 12 mois). */
    Abonnement creerNegocie(Pharmacopee pharmacopee, OffreAbonnement offre, BigDecimal prix, int dureeMois, int joursGrace,
                            List<QuotaNegocie> quotas, DemandeContrat demande, Utilisateur administrateur);

    /**
     * Applique un paiement confirmé : activation immédiate, ou programmation d'un renouvellement anticipé.
     *
     * @throws com.pharmacopee.ladafura.exceptions.ConflictException si l'abonnement n'est pas en attente de paiement,
     *         si la pharmacopée a un abonnement suspendu ou un renouvellement déjà programmé
     */
    void activerApresPaiement(Abonnement abonnement, Utilisateur acteur);

    /** Attribue l'offre gratuite à une pharmacopée qui vient d'être créée. */
    Abonnement attribuerGratuit(Pharmacopee pharmacopee, Utilisateur acteur);

    /** Remplace un abonnement terminé par l'offre gratuite et suspend les membres au-delà du quota. */
    Abonnement retournerAuGratuit(Pharmacopee pharmacopee, Abonnement precedent, Utilisateur acteur);

    /** Traite les échéances et les fins de grâce ; renvoie le nombre d'abonnements traités. */
    int traiterEcheances(LocalDateTime maintenant);

    void historiser(Abonnement abonnement, TypeEvenementAbonnement type, String ancienne, String nouvelle,
                    String description, Utilisateur acteur);
}
