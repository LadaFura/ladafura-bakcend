package com.pharmacopee.ladafura.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.pharmacopee.localisation.PharmacopeeLocalisationRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.localisation.PharmacopeeLocalisationResponse;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.LocalisationRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeLocalisationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacopeeLocalisationServiceImpl implements IPharmacopeeLocalisationService {

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final PharmacopeeRepository pharmacopeeRepository;
    private final LocalisationRepository localisationRepository;

    @Override
    @Transactional(readOnly = true)
    public PharmacopeeLocalisationResponse getLocalisation() {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        if (pharmacopee.getLocalisation() == null) {
            log.info("Aucune localisation configurée pour la pharmacopée ID: {}", pharmacopee.getId());
            throw new ResourceNotFoundException("Localisation", "pharmacopeeId", pharmacopee.getId());
        }

        return buildResponse(pharmacopee, pharmacopee.getLocalisation());
    }

    @Override
    public PharmacopeeLocalisationResponse saveOrUpdateLocalisation(PharmacopeeLocalisationRequest request) {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Localisation localisation = pharmacopee.getLocalisation();

        if (localisation != null) {
            log.info("Mise à jour de la localisation existante ID {} pour la pharmacopée ID {}",
                    localisation.getId(), pharmacopee.getId());
            localisation.setRegion(request.getRegion().trim());
            localisation.setCercle(request.getCercle().trim());
            localisation.setCommune(request.getCommune().trim());
            localisation.setLocalite(request.getLocalite().trim());
            localisation.setLatitude(request.getLatitude());
            localisation.setLongitude(request.getLongitude());
            localisation = localisationRepository.save(localisation);
        } else {
            log.info("Création et association d'une nouvelle localisation pour la pharmacopée ID {}",
                    pharmacopee.getId());
            localisation = Localisation.builder()
                    .region(request.getRegion().trim())
                    .cercle(request.getCercle().trim())
                    .commune(request.getCommune().trim())
                    .localite(request.getLocalite().trim())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .build();
            localisation = localisationRepository.save(localisation);

            pharmacopee.setLocalisation(localisation);
            pharmacopeeRepository.save(pharmacopee);
        }

        return buildResponse(pharmacopee, localisation);
    }

    private PharmacopeeLocalisationResponse buildResponse(Pharmacopee pharmacopee, Localisation loc) {
        boolean geolocalisee = (loc.getLatitude() != null && loc.getLongitude() != null);
        String adresseComplete = String.format("%s, %s, %s, %s",
                loc.getLocalite(), loc.getCommune(), loc.getCercle(), loc.getRegion());

        return PharmacopeeLocalisationResponse.builder()
                .id(loc.getId())
                .pharmacopeeId(pharmacopee.getId())
                .nomPharmacopee(pharmacopee.getNom())
                .region(loc.getRegion())
                .cercle(loc.getCercle())
                .commune(loc.getCommune())
                .localite(loc.getLocalite())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .geolocalisee(geolocalisee)
                .adresseComplete(adresseComplete)
                .build();
    }
}
