package com.pharmacopee.ladafura.services.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.dashboard.AgentDashboardResponse;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.mappers.AgentCollecteMapper;
import com.pharmacopee.ladafura.mappers.AgentNotificationMapper;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;
import com.pharmacopee.ladafura.services.interfaces.IAgentDashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentDashboardServiceImpl implements IAgentDashboardService {

    private final IAgentAuthService agentAuthService;
    private final CollecteRepository collecteRepository;
    private final NotificationRepository notificationRepository;
    private final AgentCollecteMapper agentCollecteMapper;
    private final AgentNotificationMapper agentNotificationMapper;

    @Override
    @Transactional(readOnly = true)
    public AgentDashboardResponse getDashboard() {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Long agentId = currentAgent.getId();

        log.info("Génération du tableau de bord consolidé pour l'agent ID: {} ({})", agentId, currentAgent.getEmail());

        // 1. Volet Informations Agent
        AgentDashboardResponse.AgentInfoSection agentInfo = AgentDashboardResponse.AgentInfoSection.builder()
                .agentId(agentId)
                .nom(currentAgent.getNom())
                .prenom(currentAgent.getPrenom())
                .email(currentAgent.getEmail())
                .telephone(currentAgent.getTelephone())
                .matricule(currentAgent.getMatricule())
                .zoneCouverture(currentAgent.getZoneCouverture())
                .build();

        // 2. Volet Statistiques des Collectes
        long totalCollectes = collecteRepository.countByAgentCollecteId(agentId);
        long brouillons = collecteRepository.countByAgentCollecteIdAndStatut(agentId, StatutCollecte.BROUILLON);
        long soumises = collecteRepository.countByAgentCollecteIdAndStatut(agentId, StatutCollecte.SOUMISE);
        long enExamen = collecteRepository.countByAgentCollecteIdAndStatut(agentId, StatutCollecte.EN_EXAMEN);
        long enAttente = soumises + enExamen;
        long validees = collecteRepository.countByAgentCollecteIdAndStatut(agentId, StatutCollecte.VALIDEE);
        long rejetees = collecteRepository.countByAgentCollecteIdAndStatut(agentId, StatutCollecte.REJETEE);

        long instruites = validees + rejetees;
        double tauxValidation = 0.0;
        if (instruites > 0) {
            tauxValidation = Math.round(((double) validees / (double) instruites) * 1000.0) / 10.0;
        }

        AgentDashboardResponse.CollectesStatsSection statsSection = AgentDashboardResponse.CollectesStatsSection.builder()
                .total(totalCollectes)
                .brouillons(brouillons)
                .enAttente(enAttente)
                .soumises(soumises)
                .enExamen(enExamen)
                .validees(validees)
                .rejetees(rejetees)
                .tauxValidation(tauxValidation)
                .build();

        // 3. Volet Notifications & Alertes
        long totalNotifications = notificationRepository.countByUtilisateurId(agentId);
        long nonLues = notificationRepository.countByUtilisateurIdAndLueFalse(agentId);
        List<AgentNotificationResponse> dernieresNotifications = notificationRepository
                .findByUtilisateurId(agentId, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dateNotification")))
                .getContent()
                .stream()
                .map(agentNotificationMapper::toDto)
                .toList();

        AgentDashboardResponse.NotificationsSection notificationsSection = AgentDashboardResponse.NotificationsSection.builder()
                .total(totalNotifications)
                .nonLues(nonLues)
                .dernieresNotifications(dernieresNotifications)
                .build();

        // 4. Liste des 5 dernières collectes terrain
        List<AgentCollecteSummaryResponse> dernieresCollectes = collecteRepository
                .findByAgentCollecteId(agentId, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dateCollecte")))
                .getContent()
                .stream()
                .map(agentCollecteMapper::toSummaryDto)
                .toList();

        log.info("Tableau de bord agent ID: {} généré : {} collectes, {} en attente, {} notifs non lues",
                agentId, totalCollectes, enAttente, nonLues);

        return AgentDashboardResponse.builder()
                .agent(agentInfo)
                .statistiques(statsSection)
                .notifications(notificationsSection)
                .dernieresCollectes(dernieresCollectes)
                .build();
    }
}
