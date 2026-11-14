package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.EvenementAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.FactureAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.PaiementAbonnementResponse;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutPaiement;

/** Supervision des abonnements et confirmation des paiements par l'administrateur. */
public interface IAdminAbonnementService {

    Page<AbonnementResponse> getAbonnements(StatutAbonnement statut, Long pharmacopeeId, Pageable pageable);

    AbonnementResponse getAbonnement(Long id);

    Page<EvenementAbonnementResponse> getHistorique(Long id, Pageable pageable);

    AbonnementResponse suspendre(Long id, String motif);

    AbonnementResponse reactiver(Long id, String motif);

    /** Annule un abonnement ; s'il était en vigueur, la pharmacopée revient à l'offre gratuite. */
    AbonnementResponse annuler(Long id, String motif);

    Page<PaiementAbonnementResponse> getPaiements(StatutPaiement statut, Pageable pageable);

    /** Confirme un paiement déclaré, active l'abonnement et émet la facture. */
    FactureAbonnementResponse confirmerPaiement(Long paiementId);

    PaiementAbonnementResponse rejeterPaiement(Long paiementId, String motif);
}
