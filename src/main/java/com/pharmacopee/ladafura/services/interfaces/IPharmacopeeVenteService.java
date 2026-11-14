package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.vente.EnregistrerVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.PaiementVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.PaiementVenteResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.ValiderVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.VenteResponse;
import com.pharmacopee.ladafura.enums.StatutVente;

/** Ventes au comptoir de la pharmacopée courante. Seule la validation touche au stock et à la caisse. */
public interface IPharmacopeeVenteService {

    Page<VenteResponse> getVentes(StatutVente statut, Long clientId, Pageable pageable);

    VenteResponse getVente(Long id);

    VenteResponse creer(EnregistrerVenteRequest request);

    VenteResponse modifier(Long id, EnregistrerVenteRequest request);

    VenteResponse abandonner(Long id);

    /**
     * Fige les prix, sort le stock et enregistre les paiements reçus.
     *
     * @throws com.pharmacopee.ladafura.exceptions.BadRequestException si un reste à payer subsiste sans client
     * @throws com.pharmacopee.ladafura.exceptions.ConflictException si le stock vendable est insuffisant
     * @throws com.pharmacopee.ladafura.exceptions.QuotaExceededException si le quota mensuel est atteint
     */
    VenteResponse valider(Long id, ValiderVenteRequest request);

    /** Annulation totale : le stock revient dans les mêmes lots et les paiements sont contre-passés. */
    VenteResponse annuler(Long id, String motif);

    List<PaiementVenteResponse> getPaiements(Long venteId);

    PaiementVenteResponse enregistrerPaiement(Long venteId, PaiementVenteRequest request);

    PaiementVenteResponse annulerPaiement(Long venteId, Long paiementId, String motif);
}
