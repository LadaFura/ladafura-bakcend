package com.pharmacopee.ladafura.services.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Client;
import com.pharmacopee.ladafura.dto.pharmacopee.client.ClientResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.client.EnregistrerClientRequest;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.VenteMapper;
import com.pharmacopee.ladafura.repository.ClientRepository;
import com.pharmacopee.ladafura.repository.PaiementVenteRepository;
import com.pharmacopee.ladafura.repository.VenteRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeClientService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacopeeClientServiceImpl implements IPharmacopeeClientService {

    private final IPharmacopeeContexteService contexteService;
    private final ClientRepository clientRepository;
    private final VenteRepository venteRepository;
    private final PaiementVenteRepository paiementRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> getClients(String recherche, Pageable pageable) {
        Long pharmacopeeId = lecture();
        String motif = recherche == null || recherche.isBlank() ? null : "%" + recherche.trim().toLowerCase(Locale.ROOT) + "%";
        Page<Client> clients = clientRepository.rechercher(pharmacopeeId, motif, pageable);
        Map<Long, BigDecimal> encours = encours(clients.map(Client::getId).getContent());
        return clients.map(c -> VenteMapper.client(c, encours.getOrDefault(c.getId(), BigDecimal.ZERO)));
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClient(Long id) {
        return versReponse(clientDe(lecture(), id));
    }

    @Override
    public ClientResponse creer(EnregistrerClientRequest request) {
        var pharmacopee = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.CLIENTS_GERER).pharmacopee();
        Client client = Client.builder().pharmacopee(pharmacopee).build();
        copier(request, client, pharmacopee.getId());
        return versReponse(clientRepository.save(client));
    }

    @Override
    public ClientResponse modifier(Long id, EnregistrerClientRequest request) {
        Long pharmacopeeId = ecriture();
        Client client = clientDe(pharmacopeeId, id);
        copier(request, client, pharmacopeeId);
        return versReponse(client);
    }

    @Override
    public ClientResponse changerActivation(Long id, boolean actif) {
        Client client = clientDe(ecriture(), id);
        client.setActif(actif);
        return versReponse(client);
    }

    private void copier(EnregistrerClientRequest request, Client client, Long pharmacopeeId) {
        String telephone = request.getTelephone() == null || request.getTelephone().isBlank() ? null : request.getTelephone().trim();
        if (telephone != null) {
            clientRepository.findByPharmacopeeIdAndTelephone(pharmacopeeId, telephone)
                    .filter(existant -> !existant.getId().equals(client.getId()))
                    .ifPresent(existant -> {
                        throw new ConflictException("Un client utilise déjà ce numéro de téléphone.");
                    });
        }
        client.setNom(request.getNom().trim());
        client.setPrenom(request.getPrenom());
        client.setTelephone(telephone);
        client.setAdresse(request.getAdresse());
        client.setObservation(request.getObservation());
    }

    private ClientResponse versReponse(Client client) {
        BigDecimal encours = client.getId() == null ? BigDecimal.ZERO
                : encours(List.of(client.getId())).getOrDefault(client.getId(), BigDecimal.ZERO);
        return VenteMapper.client(client, encours);
    }

    /** Encours = total des ventes validées moins les paiements valides de ces ventes. */
    private Map<Long, BigDecimal> encours(Collection<Long> clientIds) {
        if (clientIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, BigDecimal> payes = enTable(paiementRepository.paiementsValidesParClient(clientIds));
        return enTable(venteRepository.totauxValidesParClient(clientIds)).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().subtract(payes.getOrDefault(e.getKey(), BigDecimal.ZERO))));
    }

    private static Map<Long, BigDecimal> enTable(List<Object[]> lignes) {
        return lignes.stream().collect(Collectors.toMap(l -> (Long) l[0], l -> (BigDecimal) l[1]));
    }

    private Long lecture() {
        return contexteService.verifierPermission(IPharmacopeeContexteService.CLIENTS_CONSULTER).pharmacopee().getId();
    }

    private Long ecriture() {
        return contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.CLIENTS_GERER).pharmacopee().getId();
    }

    private Client clientDe(Long pharmacopeeId, Long id) {
        return clientRepository.findByIdAndPharmacopeeId(id, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
    }
}
