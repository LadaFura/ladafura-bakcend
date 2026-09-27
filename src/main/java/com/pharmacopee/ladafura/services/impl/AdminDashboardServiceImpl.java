package com.pharmacopee.ladafura.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.dto.admin.dashboard.AdminDashboardStatsResponse;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.EtudeScientifiqueRepository;
import com.pharmacopee.ladafura.repository.MaladieRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.repository.SourceRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminDashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements IAdminDashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final SourceRepository sourceRepository;
    private final PharmacopeeRepository pharmacopeeRepository;
    private final PlanteRepository planteRepository;
    private final MaladieRepository maladieRepository;
    private final EtudeScientifiqueRepository etudeScientifiqueRepository;
    private final CollecteRepository collecteRepository;
    private final ProduitRepository produitRepository;
    private final AvisRepository avisRepository;

    @Override
    public AdminDashboardStatsResponse getDashboardStats() {
        log.info("Calcul des statistiques globales du tableau de bord administrateur");

        return AdminDashboardStatsResponse.builder()
                .totalUtilisateurs(utilisateurRepository.count())
                .nbAgentsCollecte(utilisateurRepository.countByRole(Role.AGENT_COLLECTE))
                .nbSources(sourceRepository.count())
                .nbPharmacopees(pharmacopeeRepository.count())
                .nbCitoyens(utilisateurRepository.countByRole(Role.POPULATION))
                .totalPlantes(planteRepository.count())
                .totalMaladies(maladieRepository.count())
                .totalEtudesScientifiques(etudeScientifiqueRepository.count())
                .collectesEnAttente(collecteRepository.countByStatut(StatutCollecte.SOUMISE))
                .collectesValidees(collecteRepository.countByStatut(StatutCollecte.VALIDEE))
                .pharmacopeesEnAttente(pharmacopeeRepository.countByStatut(StatutPharmacopee.EN_ATTENTE))
                .produitsEnAttente(produitRepository.countByStatut(StatutProduit.EN_ATTENTE))
                .avisEnAttente(avisRepository.countByStatut(StatutAvis.EN_ATTENTE))
                .build();
    }
}
