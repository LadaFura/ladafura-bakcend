package com.pharmacopee.ladafura.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.dto.agent.profil.AgentProfileResponse;
import com.pharmacopee.ladafura.dto.agent.profil.AgentUpdateProfileRequest;
import com.pharmacopee.ladafura.repository.AgentCollecteRepository;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;
import com.pharmacopee.ladafura.services.interfaces.IAgentProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AgentProfileServiceImpl implements IAgentProfileService {

    private final IAgentAuthService agentAuthService;
    private final AgentCollecteRepository agentCollecteRepository;
    private final CollecteRepository collecteRepository;

    @Override
    @Transactional(readOnly = true)
    public AgentProfileResponse getProfile() {
        AgentCollecte agent = agentAuthService.getCurrentAgent();
        long totalCollectes = collecteRepository.countByAgentCollecteId(agent.getId());
        return mapToProfileResponse(agent, totalCollectes);
    }

    @Override
    public AgentProfileResponse updateProfile(AgentUpdateProfileRequest request) {
        AgentCollecte agent = agentAuthService.getCurrentAgent();

        log.info("Mise à jour des informations autorisées pour l'agent ID {}", agent.getId());

        agent.setNom(request.getNom().trim());
        agent.setPrenom(request.getPrenom().trim());

        if (request.getTelephone() != null) {
            agent.setTelephone(request.getTelephone().trim());
        }

        if (request.getZoneCouverture() != null) {
            agent.setZoneCouverture(request.getZoneCouverture().trim());
        }

        // Le rôle, le statut, le matricule et l'email restent strictement inchangés
        AgentCollecte saved = agentCollecteRepository.save(agent);
        log.info("Profil de l'agent ID {} mis à jour avec succès", saved.getId());

        long totalCollectes = collecteRepository.countByAgentCollecteId(saved.getId());
        return mapToProfileResponse(saved, totalCollectes);
    }

    private AgentProfileResponse mapToProfileResponse(AgentCollecte agent, long totalCollectes) {
        return AgentProfileResponse.builder()
                .id(agent.getId())
                .matricule(agent.getMatricule())
                .zoneCouverture(agent.getZoneCouverture())
                .nom(agent.getNom())
                .prenom(agent.getPrenom())
                .email(agent.getEmail())
                .telephone(agent.getTelephone())
                .role(agent.getRole())
                .statut(agent.getStatut())
                .firebaseUid(agent.getFirebaseUid())
                .dateCreation(agent.getDateCreation())
                .nombreTotalCollectes(totalCollectes)
                .build();
    }
}
