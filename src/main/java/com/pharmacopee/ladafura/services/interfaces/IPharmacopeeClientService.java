package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.client.ClientResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.client.EnregistrerClientRequest;

/** Clients de la pharmacopée courante. Un client n'est obligatoire que pour une vente à crédit. */
public interface IPharmacopeeClientService {

    Page<ClientResponse> getClients(String recherche, Pageable pageable);

    ClientResponse getClient(Long id);

    ClientResponse creer(EnregistrerClientRequest request);

    ClientResponse modifier(Long id, EnregistrerClientRequest request);

    ClientResponse changerActivation(Long id, boolean actif);
}
