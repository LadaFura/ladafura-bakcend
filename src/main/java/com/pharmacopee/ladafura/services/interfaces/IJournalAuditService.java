package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.Models.JournalAudit;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.TypeActionAudit;

import lombok.Builder;

/**
 * Trace les opérations sensibles, dans la même transaction que l'opération elle-même.
 * Ne jamais y placer de jeton, de mot de passe ni de donnée personnelle superflue.
 */
public interface IJournalAuditService {

    /** Pharmacopée nulle : action de plateforme ; utilisateur nul : action du système. */
    @Builder
    record EvenementAudit(TypeActionAudit action, OrigineAction origine, Pharmacopee pharmacopee, Utilisateur utilisateur,
                          String typeEntite, Long entiteId, String ancienneValeur, String nouvelleValeur, String description) {
    }

    JournalAudit tracer(EvenementAudit evenement);
}
