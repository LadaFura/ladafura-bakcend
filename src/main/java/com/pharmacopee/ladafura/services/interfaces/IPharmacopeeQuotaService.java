package com.pharmacopee.ladafura.services.interfaces;

/**
 * Contrôle des quotas de l'abonnement en vigueur, à appeler avant chaque opération concernée.
 * Un dépassement bloque uniquement les nouvelles créations : les données existantes restent intactes.
 */
public interface IPharmacopeeQuotaService {

    String MAX_UTILISATEURS_ACTIFS = "MAX_UTILISATEURS_ACTIFS";
    String MAX_PRODUITS = "MAX_PRODUITS";
    String MAX_VENTES_MENSUELLES = "MAX_VENTES_MENSUELLES";

    /**
     * @throws com.pharmacopee.ladafura.exceptions.QuotaExceededException si le quota est atteint, ou si
     *         la pharmacopée n'a pas d'abonnement en vigueur ou pas de valeur pour cette limite
     */
    void verifierQuota(Long pharmacopeeId, String codeLimite);

    /** Consommation actuelle d'une limite. */
    long consommationActuelle(Long pharmacopeeId, String codeLimite);
}
