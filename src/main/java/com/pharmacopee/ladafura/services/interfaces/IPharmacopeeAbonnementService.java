package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DeclarerPaiementRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DemandeContratRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DemandeContratResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.FactureAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.MotifRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.OffreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.PaiementAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.SouscrireOffreRequest;

/** Abonnement de la pharmacopée courante. Ces opérations restent possibles en période de grâce. */
public interface IPharmacopeeAbonnementService {

    AbonnementResponse getAbonnementEnVigueur();

    Page<AbonnementResponse> getHistorique(Pageable pageable);

    Page<FactureAbonnementResponse> getFactures(Pageable pageable);

    List<OffreResponse> getOffres();

    AbonnementResponse souscrire(SouscrireOffreRequest request);

    PaiementAbonnementResponse declarerPaiement(Long abonnementId, DeclarerPaiementRequest request);

    AbonnementResponse annuler(Long abonnementId, MotifRequest request);

    DemandeContratResponse demanderContrat(DemandeContratRequest request);
}
