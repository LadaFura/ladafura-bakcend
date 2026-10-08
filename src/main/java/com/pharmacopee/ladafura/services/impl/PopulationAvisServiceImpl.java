package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.avis.PopulationAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationCreateAvisRequest;
import com.pharmacopee.ladafura.dto.population.avis.PopulationEligibiliteAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationUpdateAvisRequest;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationAvisMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAvisService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PopulationAvisServiceImpl implements IPopulationAvisService {

    private final IPopulationAuthService populationAuthService;
    private final AvisRepository avisRepository;
    private final PharmacopeeRepository pharmacopeeRepository;
    private final CommandeRepository commandeRepository;
    private final PopulationAvisMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PopulationEligibiliteAvisResponse verifierEligibiliteAvis(Long pharmacopeeId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Vérification d'éligibilité avis pour l'utilisateur ID: {} et la pharmacopée ID: {}", user.getId(), pharmacopeeId);

        Pharmacopee pharmacopee = pharmacopeeRepository.findById(pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", pharmacopeeId));

        boolean eligible = commandeRepository.existsByUtilisateurIdAndPharmacopeeIdAndStatutIn(
                user.getId(), pharmacopee.getId(), List.of(StatutCommande.LIVREE));

        Optional<Avis> existingAvis = avisRepository.findByUtilisateurIdAndPharmacopeeId(user.getId(), pharmacopeeId);

        return mapper.toEligibiliteResponse(pharmacopee, eligible, existingAvis);
    }

    @Override
    public PopulationAvisResponse creerAvis(PopulationCreateAvisRequest request) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Création d'un avis par l'utilisateur ID: {} pour la pharmacopée ID: {}", user.getId(), request.getPharmacopeeId());

        Pharmacopee pharmacopee = pharmacopeeRepository.findById(request.getPharmacopeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", request.getPharmacopeeId()));

        boolean eligible = commandeRepository.existsByUtilisateurIdAndPharmacopeeIdAndStatutIn(
                user.getId(), pharmacopee.getId(), List.of(StatutCommande.LIVREE));
        if (!eligible) {
            throw new BadRequestException("Vous devez avoir passé au moins une commande livrée auprès de cette pharmacopée pour pouvoir donner votre avis.");
        }

        if (avisRepository.existsByUtilisateurIdAndPharmacopeeId(user.getId(), pharmacopee.getId())) {
            throw new BadRequestException("Vous avez déjà déposé un avis pour cette pharmacopée. Vous pouvez modifier votre avis existant.");
        }

        Avis avis = Avis.builder()
                .note(request.getNote())
                .commentaire(request.getCommentaire())
                .dateAvis(LocalDateTime.now())
                .statut(StatutAvis.PUBLIE)
                .utilisateur(user)
                .pharmacopee(pharmacopee)
                .build();

        Avis saved = avisRepository.save(avis);
        log.info("Avis ID: {} créé et publié avec succès pour la pharmacopée ID: {}", saved.getId(), pharmacopee.getId());

        return mapper.toResponse(saved);
    }

    @Override
    public PopulationAvisResponse modifierAvis(Long avisId, PopulationUpdateAvisRequest request) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Modification de l'avis ID: {} par l'utilisateur ID: {}", avisId, user.getId());

        Avis avis = avisRepository.findByIdAndUtilisateurId(avisId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Avis", "id", avisId));

        avis.setNote(request.getNote());
        avis.setCommentaire(request.getCommentaire());
        avis.setDateAvis(LocalDateTime.now());
        avis.setStatut(StatutAvis.PUBLIE);

        Avis updated = avisRepository.save(avis);
        log.info("Avis ID: {} mis à jour et publié avec succès", avisId);

        return mapper.toResponse(updated);
    }

    @Override
    public void supprimerAvis(Long avisId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Suppression de l'avis ID: {} par l'utilisateur ID: {}", avisId, user.getId());

        Avis avis = avisRepository.findByIdAndUtilisateurId(avisId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Avis", "id", avisId));

        avisRepository.delete(avis);
        log.info("Avis ID: {} supprimé avec succès", avisId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PopulationAvisResponse> getMesAvis(StatutAvis statut, Pageable pageable) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation des avis pour l'utilisateur ID: {} (filtre statut: {})", user.getId(), statut);

        Page<Avis> page = (statut != null)
                ? avisRepository.findByUtilisateurIdAndStatut(user.getId(), statut, pageable)
                : avisRepository.findByUtilisateurId(user.getId(), pageable);

        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PopulationAvisResponse> getAvisByPharmacopee(Long pharmacopeeId, Pageable pageable) {
        log.info("Consultation publique des avis pour la pharmacopée ID: {}", pharmacopeeId);
        return avisRepository.findByPharmacopeeIdAndStatut(pharmacopeeId, StatutAvis.PUBLIE, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PopulationAvisResponse getAvisDetail(Long avisId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation du détail de l'avis ID: {} pour l'utilisateur ID: {}", avisId, user.getId());

        Avis avis = avisRepository.findByIdAndUtilisateurId(avisId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Avis", "id", avisId));

        return mapper.toResponse(avis);
    }
}
