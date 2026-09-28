package com.pharmacopee.ladafura.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.profil.PopulationProfileResponse;
import com.pharmacopee.ladafura.dto.population.profil.PopulationUpdateProfileRequest;
import com.pharmacopee.ladafura.mappers.PopulationProfileMapper;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.FavoriRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopulationProfileServiceImpl implements IPopulationProfileService {

    private final IPopulationAuthService populationAuthService;
    private final UtilisateurRepository utilisateurRepository;
    private final CommandeRepository commandeRepository;
    private final FavoriRepository favoriRepository;
    private final PopulationProfileMapper populationProfileMapper;

    @Override
    @Transactional(readOnly = true)
    public PopulationProfileResponse getProfile() {
        Utilisateur currentUser = populationAuthService.getCurrentPopulationUser();
        Long userId = currentUser.getId();

        log.info("Consultation du profil Population pour l'utilisateur ID: {}", userId);

        long totalCommandes = commandeRepository.countByUtilisateurId(userId);
        long totalFavoris = favoriRepository.countByUtilisateurId(userId);

        return populationProfileMapper.toDto(currentUser, totalCommandes, totalFavoris);
    }

    @Override
    @Transactional
    public PopulationProfileResponse updateProfile(PopulationUpdateProfileRequest request) {
        Utilisateur currentUser = populationAuthService.getCurrentPopulationUser();
        Long userId = currentUser.getId();

        log.info("Mise à jour du profil Population pour l'utilisateur ID: {}", userId);

        // Modification exclusive des champs autorisés (interdiction formelle d'altérer rôle ou statut)
        currentUser.setNom(request.getNom().trim());
        currentUser.setPrenom(request.getPrenom().trim());
        if (request.getTelephone() != null) {
            currentUser.setTelephone(request.getTelephone().trim());
        }

        Utilisateur savedUser = utilisateurRepository.save(currentUser);

        long totalCommandes = commandeRepository.countByUtilisateurId(userId);
        long totalFavoris = favoriRepository.countByUtilisateurId(userId);

        log.info("Profil Population ID: {} mis à jour avec succès", userId);

        return populationProfileMapper.toDto(savedUser, totalCommandes, totalFavoris);
    }
}
