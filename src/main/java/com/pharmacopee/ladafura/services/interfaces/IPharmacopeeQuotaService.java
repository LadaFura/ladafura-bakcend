package com.pharmacopee.ladafura.services.interfaces;

/**
 * Contrôle des quotas de l'abonnement en vigueur, à appeler avant chaque opération concernée.
 * Un dépassement bloque uniquement les nouvelles créations : les données existantes restent intactes.
 */
public interface IPharmacopeeQuotaService {

    String MAX_UTILISATEURS_ACTIFS = "MAX_UTILISATEURS_ACTIFS";
    String MAX_PRODUITS = "MAX_PRODUITS";
    String MAX_VENTES_MENSUELLES = "MAX_VENTES_MENSUELLES";
    String MAX_PHARMACOPEES = "MAX_PHARMACOPEES";

    /** Valeur renvoyée par {@link #maxPharmacopeesParGerant(Long)} quand une formule ne fixe pas de plafond. */
    int PHARMACOPEES_ILLIMITEES = Integer.MAX_VALUE;

    /**
     * @throws com.pharmacopee.ladafura.exceptions.QuotaExceededException si le quota est atteint, ou si
     *         la pharmacopée n'a pas d'abonnement en vigueur ou pas de valeur pour cette limite
     */
    void verifierQuota(Long pharmacopeeId, String codeLimite);

    /**
     * Plafond d'une limite pour une pharmacopée. La formule du gérant couvre toutes ses pharmacopées :
     * le plafond le plus favorable parmi leurs abonnements en vigueur s'applique.
     */
    java.util.Optional<Plafond> plafondApplicable(Long pharmacopeeId, String codeLimite);

    /** Plafond retenu et abonnement qui le fournit. */
    record Plafond(Integer valeurMaximale, boolean illimite, Long abonnementId) {
        public boolean autorise(long consommation) {
            return illimite || (valeurMaximale != null && consommation < valeurMaximale);
        }
    }

    /** Consommation actuelle d'une limite. */
    long consommationActuelle(Long pharmacopeeId, String codeLimite);

    /**
     * Nombre de pharmacopées permis à un gérant. Chaque pharmacopée ayant son propre abonnement,
     * la formule la plus favorable parmi celles qu'il détient s'applique. Sans formule définissant
     * cette limite (compte sans pharmacopée, ancien abonnement), le paramètre plateforme
     * MAX_PHARMACOPEES_PAR_GERANT s'applique.
     */
    int maxPharmacopeesParGerant(Long utilisateurId);

    /** Nombre de pharmacopées dont l'utilisateur est propriétaire actif. */
    long pharmacopeesPossedees(Long utilisateurId);
}
