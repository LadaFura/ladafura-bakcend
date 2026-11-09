package com.pharmacopee.ladafura.services.interfaces;

import java.util.Set;

import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;

/**
 * Contexte d'exécution dans l'espace pharmacopée : qui agit, dans quelle pharmacopée, avec quels droits.
 * La pharmacopée est toujours résolue par le serveur à partir des appartenances ACTIF de l'utilisateur.
 */
public interface IPharmacopeeContexteService {

    String MEMBRES_CONSULTER = "MEMBRES_CONSULTER";
    String MEMBRES_INVITER = "MEMBRES_INVITER";
    String MEMBRES_GERER = "MEMBRES_GERER";
    String ROLES_GERER = "ROLES_GERER";
    String ABONNEMENT_CONSULTER = "ABONNEMENT_CONSULTER";
    String ABONNEMENT_GERER = "ABONNEMENT_GERER";
    String FOURNISSEURS_CONSULTER = "FOURNISSEURS_CONSULTER";
    String FOURNISSEURS_GERER = "FOURNISSEURS_GERER";
    String ACHATS_CONSULTER = "ACHATS_CONSULTER";
    String ACHATS_GERER = "ACHATS_GERER";
    String CLIENTS_CONSULTER = "CLIENTS_CONSULTER";
    String CLIENTS_GERER = "CLIENTS_GERER";
    String VENTES_CONSULTER = "VENTES_CONSULTER";
    String VENTES_CREER = "VENTES_CREER";
    String VENTES_ANNULER = "VENTES_ANNULER";
    String VENTES_REMISE = "VENTES_REMISE";
    String PAIEMENTS_ENCAISSER = "PAIEMENTS_ENCAISSER";
    String CAISSE_CONSULTER = "CAISSE_CONSULTER";
    String CAISSE_GERER = "CAISSE_GERER";
    String CAISSE_OUVRIR_FERMER = "CAISSE_OUVRIR_FERMER";
    String DEPENSES_CONSULTER = "DEPENSES_CONSULTER";
    String DEPENSES_GERER = "DEPENSES_GERER";
    String COMPTABILITE_CONSULTER = "COMPTABILITE_CONSULTER";
    String AUDIT_CONSULTER = "AUDIT_CONSULTER";

    /** Membre courant, sa pharmacopée et ses permissions effectives. */
    record ContexteMembre(MembrePharmacopee membre, Pharmacopee pharmacopee, Set<String> permissions) {
    }

    /**
     * Résout le contexte courant. {@code X-Pharmacopee-Id} n'est qu'un choix parmi les appartenances ;
     * sans en-tête, la première pharmacopée VALIDEE est retenue, sinon la première.
     *
     * @throws com.pharmacopee.ladafura.exceptions.ForbiddenException si l'utilisateur n'est pas membre actif
     *         de la pharmacopée demandée ou d'aucune pharmacopée
     * @throws com.pharmacopee.ladafura.exceptions.BadRequestException si l'en-tête n'est pas numérique
     */
    ContexteMembre getContexte();

    /**
     * @throws com.pharmacopee.ladafura.exceptions.ForbiddenException si la permission manque
     *         ou si la pharmacopée est SUSPENDUE ou REJETEE
     */
    ContexteMembre verifierPermission(String codePermission);

    /**
     * Seul un abonnement ACTIF autorise les écritures (EN_GRACE et SUSPENDU : lecture seule).
     *
     * @throws com.pharmacopee.ladafura.exceptions.ForbiddenException sinon
     */
    void verifierEcritureAutorisee(Long pharmacopeeId);

    /**
     * Contrôles d'une opération commerciale : permission, pharmacopée VALIDEE, abonnement ACTIF.
     *
     * @throws com.pharmacopee.ladafura.exceptions.ForbiddenException si l'un des contrôles échoue
     */
    ContexteMembre verifierOperationCommerciale(String codePermission);
}
