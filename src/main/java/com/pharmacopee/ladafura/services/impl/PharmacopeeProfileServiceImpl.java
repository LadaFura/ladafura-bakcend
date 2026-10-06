package com.pharmacopee.ladafura.services.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.localisation.PharmacopeeLocalisationResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.profil.PharmacopeeProfileResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.profil.PharmacopeeUpdateProfileRequest;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeLocalisationService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacopeeProfileServiceImpl implements IPharmacopeeProfileService {

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final IPharmacopeeLocalisationService localisationService;
    private final PharmacopeeRepository pharmacopeeRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(readOnly = true)
    public PharmacopeeProfileResponse getProfile() {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        return mapToProfileResponse(pharmacopee);
    }

    @Override
    public PharmacopeeProfileResponse updateProfile(PharmacopeeUpdateProfileRequest request) {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        log.info("Mise à jour du profil de la pharmacopée ID {}", pharmacopee.getId());

        pharmacopee.setNom(request.getNom().trim());
        pharmacopee.setDescription(request.getDescription());
        pharmacopee.setTelephone(request.getTelephone().trim());
        if (request.getPhotoUrl() != null) {
            pharmacopee.setPhotoUrl(request.getPhotoUrl().isBlank() ? null : request.getPhotoUrl());
        }

        // Mise à jour du téléphone personnel du responsable
        if (request.getTelephoneResponsable() != null) {
            Utilisateur user = pharmacopee.getUtilisateur();
            if (user != null) {
                user.setTelephone(request.getTelephoneResponsable().trim());
                utilisateurRepository.save(user);
            }
        }

        // Mise à jour de la localisation géographique si fournie
        if (request.getLocalisation() != null) {
            localisationService.saveOrUpdateLocalisation(request.getLocalisation());
        }

        Pharmacopee saved = pharmacopeeRepository.save(pharmacopee);
        log.info("Profil enregistré avec succès pour la pharmacopée ID: {}", saved.getId());

        return mapToProfileResponse(saved);
    }

    private PharmacopeeProfileResponse mapToProfileResponse(Pharmacopee p) {
        PharmacopeeProfileResponse.PharmacopeeProfileResponseBuilder builder = PharmacopeeProfileResponse.builder()
                .id(p.getId())
                .nom(p.getNom())
                .description(p.getDescription())
                .telephone(p.getTelephone())
                .photoUrl(p.getPhotoUrl())
                .statut(p.getStatut())
                .validee(p.getStatut() == StatutPharmacopee.VALIDEE);

        // Données du responsable
        Utilisateur user = p.getUtilisateur();
        if (user != null) {
            builder.utilisateurId(user.getId())
                   .nomResponsable(user.getNom())
                   .prenomResponsable(user.getPrenom())
                   .emailResponsable(user.getEmail())
                   .telephoneResponsable(user.getTelephone());
        }

        // Données de localisation
        if (p.getLocalisation() != null) {
            Localisation loc = p.getLocalisation();
            boolean geolocalisee = (loc.getLatitude() != null && loc.getLongitude() != null);
            String adresseComplete = String.format("%s, %s, %s, %s",
                    loc.getLocalite(), loc.getCommune(), loc.getCercle(), loc.getRegion());

            builder.localisation(PharmacopeeLocalisationResponse.builder()
                    .id(loc.getId())
                    .pharmacopeeId(p.getId())
                    .nomPharmacopee(p.getNom())
                    .region(loc.getRegion())
                    .cercle(loc.getCercle())
                    .commune(loc.getCommune())
                    .localite(loc.getLocalite())
                    .latitude(loc.getLatitude())
                    .longitude(loc.getLongitude())
                    .geolocalisee(geolocalisee)
                    .adresseComplete(adresseComplete)
                    .build());
        }

        // Modes de retrait actifs
        if (p.getModesRetrait() != null) {
            List<String> modes = p.getModesRetrait().stream()
                    .filter(mr -> mr.getActif() == null || mr.getActif())
                    .map(mr -> mr.getType() != null ? mr.getType().name() : null)
                    .filter(Objects::nonNull)
                    .toList();
            builder.modesRetraitActifs(modes);
        }

        // Volume de catalogue
        int nbProduits = (p.getDisponibilites() != null) ? p.getDisponibilites().size() : 0;
        builder.nombreProduitsDisponibles(nbProduits);

        return builder.build();
    }
}
