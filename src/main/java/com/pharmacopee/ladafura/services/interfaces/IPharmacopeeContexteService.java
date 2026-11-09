package com.pharmacopee.ladafura.services.interfaces;

import java.util.Set;

import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;

/**
 * Contexte d'exécution dans l'espace pharmacopée : qui agit, dans quelle pharmacopée, avec quels droits.
 * La pharmacopée est toujours résolue par le serveur à partir des appartenances ACTIF de l'utilisateur.
 */
public interface IPharmacopeeContexteService {

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
}
