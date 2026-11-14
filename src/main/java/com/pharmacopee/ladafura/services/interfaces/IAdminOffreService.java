package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import com.pharmacopee.ladafura.dto.admin.abonnement.DefinirQuotasOffreRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.EnregistrerOffreRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.LimiteOffreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.OffreResponse;

/** Catalogue des offres. Modifier une offre n'affecte jamais les abonnements déjà souscrits. */
public interface IAdminOffreService {

    List<OffreResponse> getOffres();

    OffreResponse creer(EnregistrerOffreRequest request);

    OffreResponse modifier(Long id, EnregistrerOffreRequest request);

    OffreResponse activer(Long id);

    OffreResponse desactiver(Long id);

    List<LimiteOffreResponse> getLimites();

    OffreResponse definirQuotas(Long offreId, DefinirQuotasOffreRequest request);
}
