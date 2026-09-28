package com.pharmacopee.ladafura.services.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.agent.auth.AgentAuthResponse;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.UnauthorizedException;
import com.pharmacopee.ladafura.repository.AgentCollecteRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AgentAuthServiceImpl implements IAgentAuthService {

    private final AgentCollecteRepository agentCollecteRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public AgentCollecte getCurrentAgent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Tentative d'accès non authentifié sur un endpoint Agent de Collecte");
            throw new UnauthorizedException("Authentification requise pour effectuer cette opération.");
        }

        String identifier = authentication.getName();

        // 1. Recherche directe dans la table spécifique des agents
        AgentCollecte agent = agentCollecteRepository.findByEmail(identifier)
                .or(() -> agentCollecteRepository.findByFirebaseUid(identifier))
                .orElse(null);

        // 2. Si non trouvé directement, vérification dans la table globale utilisateurs pour diagnostic d'accès
        if (agent == null) {
            Utilisateur utilisateur = utilisateurRepository.findByEmail(identifier)
                    .or(() -> utilisateurRepository.findByFirebaseUid(identifier))
                    .orElseThrow(() -> new UnauthorizedException("Aucun compte utilisateur trouvé pour l'identifiant : " + identifier));

            if (utilisateur.getRole() != Role.AGENT_COLLECTE) {
                log.warn("Utilisateur ID: {} tente d'accéder au module Agent avec le rôle {}", utilisateur.getId(), utilisateur.getRole());
                throw new ForbiddenException("Accès refusé : votre compte ne dispose pas du rôle AGENT_COLLECTE.");
            }

            // Si c'est un AGENT_COLLECTE mais non chargé comme entité spécifique
            agent = agentCollecteRepository.findById(utilisateur.getId())
                    .orElseThrow(() -> new UnauthorizedException("Profil Agent de Collecte introuvable pour l'utilisateur ID : " + utilisateur.getId()));
        }

        // 3. Vérification du statut du compte
        if (agent.getStatut() != StatutUtilisateur.ACTIF) {
            log.warn("Compte inactif ou suspendu pour l'agent ID: {} (statut={})", agent.getId(), agent.getStatut());
            throw new ForbiddenException("Votre compte agent de collecte est inactif ou suspendu. Veuillez contacter un administrateur.");
        }

        // 4. Vérification stricte du rôle
        if (agent.getRole() != Role.AGENT_COLLECTE) {
            log.warn("Rôle non conforme pour l'agent ID: {} (rôle={})", agent.getId(), agent.getRole());
            throw new ForbiddenException("Accès réservé exclusivement aux agents de collecte.");
        }

        return agent;
    }

    @Override
    public AgentAuthResponse getMe() {
        AgentCollecte agent = getCurrentAgent();

        return AgentAuthResponse.builder()
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
                .build();
    }

    @Override
    public void verifyAgentOwnership(Long requestedAgentId) {
        if (requestedAgentId == null) {
            throw new ForbiddenException("Identifiant de l'agent manquant.");
        }

        AgentCollecte currentAgent = getCurrentAgent();

        if (!currentAgent.getId().equals(requestedAgentId)) {
            log.warn("Violation d'accès : l'agent ID {} a tenté d'accéder aux données de l'agent ID {}",
                    currentAgent.getId(), requestedAgentId);
            throw new ForbiddenException("Accès refusé : vous n'êtes pas autorisé à accéder aux données d'un autre agent de collecte.");
        }
    }
}
