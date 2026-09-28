package com.pharmacopee.ladafura.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.dto.agent.source.AgentCreateSourceRequest;
import com.pharmacopee.ladafura.dto.agent.source.AgentSourceResponse;
import com.pharmacopee.ladafura.dto.agent.source.AgentUpdateSourceRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.SourceRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;
import com.pharmacopee.ladafura.services.interfaces.IAgentSourceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentSourceServiceImpl implements IAgentSourceService {

    private final SourceRepository sourceRepository;
    private final CollecteRepository collecteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final IAgentAuthService agentAuthService;

    @Override
    @Transactional
    public AgentSourceResponse creerSource(AgentCreateSourceRequest request) {
        String email = request.getEmail();
        if (email != null && !email.isBlank()) {
            if (utilisateurRepository.existsByEmail(email)) {
                throw new ConflictException("Une source ou un utilisateur avec cet email existe déjà : " + email);
            }
        } else {
            // Génération d'une référence traçable unique conforme à la contrainte MySQL (sans créer de compte utilisateur)
            String sanitized = (request.getPrenom() + "." + request.getNom()).toLowerCase().replaceAll("[^a-z0-9.]", "");
            email = "source." + sanitized + "." + System.currentTimeMillis() + "@terrain.ladafura.ml";
        }

        Source source = new Source();
        source.setNom(request.getNom());
        source.setPrenom(request.getPrenom());
        source.setEmail(email);
        source.setTelephone(request.getTelephone());
        source.setAdresse(request.getAdresse());
        source.setSpecialite(request.getSpecialite());
        source.setAnneesExperience(request.getAnneesExperience());
        source.setRole(request.getRole() != null ? request.getRole() : Role.THERAPEUTE);
        source.setStatut(StatutUtilisateur.ACTIF);
        source.setFirebaseUid(null); // Ne devient pas un utilisateur direct de la plateforme
        source.setMotDePasse(null);

        Source savedSource = sourceRepository.save(source);
        log.info("Nouvelle source de terrain créée avec succès : ID {} ('{} {}', {})",
                savedSource.getId(), savedSource.getPrenom(), savedSource.getNom(), savedSource.getRole());

        // Association immédiate à une collecte si spécifiée
        if (request.getCollecteId() != null) {
            associerSourceACollecte(request.getCollecteId(), savedSource.getId());
        }

        return mapToResponse(savedSource);
    }

    @Override
    @Transactional
    public AgentSourceResponse modifierSource(Long id, AgentUpdateSourceRequest request) {
        Source source = sourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source", "id", id));

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(source.getEmail())) {
            if (utilisateurRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Une ressource avec cet email existe déjà : " + request.getEmail());
            }
            source.setEmail(request.getEmail());
        }

        if (request.getNom() != null) source.setNom(request.getNom());
        if (request.getPrenom() != null) source.setPrenom(request.getPrenom());
        if (request.getTelephone() != null) source.setTelephone(request.getTelephone());
        if (request.getAdresse() != null) source.setAdresse(request.getAdresse());
        if (request.getSpecialite() != null) source.setSpecialite(request.getSpecialite());
        if (request.getAnneesExperience() != null) source.setAnneesExperience(request.getAnneesExperience());
        if (request.getRole() != null) source.setRole(request.getRole());

        Source updatedSource = sourceRepository.save(source);
        log.info("Source ID {} mise à jour avec succès", id);
        return mapToResponse(updatedSource);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentSourceResponse getSourceById(Long id) {
        Source source = sourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source", "id", id));

        return mapToResponse(source);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentSourceResponse> rechercherSources(String query, Role role, Pageable pageable) {
        Page<Source> sources = sourceRepository.searchSources(query, role, pageable);
        return sources.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void associerSourceACollecte(Long collecteId, Long sourceId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnershipAndModifiability(collecte, currentAgent);

        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source", "id", sourceId));

        collecte.setSource(source);
        collecteRepository.save(collecte);
        log.info("Source ID {} associée avec succès à la collecte ID {}", sourceId, collecteId);
    }

    @Override
    @Transactional
    public void dissocierSourceDeCollecte(Long collecteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnershipAndModifiability(collecte, currentAgent);

        collecte.setSource(null);
        collecteRepository.save(collecte);
        log.info("Source dissociée de la collecte ID {}", collecteId);
    }

    private void verifyCollecteOwnershipAndModifiability(Collecte collecte, AgentCollecte currentAgent) {
        if (collecte.getAgentCollecte() == null || !collecte.getAgentCollecte().getId().equals(currentAgent.getId())) {
            throw new ForbiddenException("Accès refusé : cette collecte ne vous appartient pas.");
        }

        if (collecte.getStatut() != StatutCollecte.BROUILLON && collecte.getStatut() != StatutCollecte.REJETEE) {
            throw new BadRequestException("Impossible de modifier la source d'une collecte avec le statut " + collecte.getStatut()
                    + ". Seules les collectes en statut BROUILLON ou REJETEE peuvent être modifiées.");
        }
    }

    private AgentSourceResponse mapToResponse(Source source) {
        int nbCollectes = (source.getCollectes() != null) ? source.getCollectes().size() : 0;

        return AgentSourceResponse.builder()
                .id(source.getId())
                .nom(source.getNom())
                .prenom(source.getPrenom())
                .nomComplet(source.getPrenom() + " " + source.getNom())
                .telephone(source.getTelephone())
                .email(source.getEmail())
                .adresse(source.getAdresse())
                .specialite(source.getSpecialite())
                .anneesExperience(source.getAnneesExperience())
                .role(source.getRole())
                .nombreCollectes(nbCollectes)
                .dateCreation(source.getDateCreation())
                .estCompteUtilisateur(false)
                .noteTracabilite("Source d'information de terrain. Non utilisatrice de la plateforme LADAFURA.")
                .build();
    }
}
