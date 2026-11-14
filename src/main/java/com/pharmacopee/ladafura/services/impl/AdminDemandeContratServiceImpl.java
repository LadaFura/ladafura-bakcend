package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.DemandeContrat;
import com.pharmacopee.ladafura.Models.LimiteOffre;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.admin.abonnement.CreerAbonnementContratRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.LigneQuotaRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.TraiterDemandeContratRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DemandeContratResponse;
import com.pharmacopee.ladafura.enums.StatutDemandeContrat;
import com.pharmacopee.ladafura.enums.TypeEvenementAbonnement;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AbonnementMapper;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.DemandeContratRepository;
import com.pharmacopee.ladafura.repository.LimiteOffreRepository;
import com.pharmacopee.ladafura.repository.OffreAbonnementRepository;
import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService;
import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService.QuotaNegocie;
import com.pharmacopee.ladafura.services.interfaces.IAdminDemandeContratService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminDemandeContratServiceImpl implements IAdminDemandeContratService {

    private static final String OFFRE_CONTRAT = "CONTRAT_PERSONNALISE";

    private final DemandeContratRepository demandeRepository;
    private final AbonnementRepository abonnementRepository;
    private final OffreAbonnementRepository offreRepository;
    private final LimiteOffreRepository limiteRepository;
    private final IAbonnementCycleService cycleService;
    private final IPharmacopeeAuthService pharmacopeeAuthService;

    @Override
    @Transactional(readOnly = true)
    public Page<DemandeContratResponse> getDemandes(StatutDemandeContrat statut, Pageable pageable) {
        Page<DemandeContrat> demandes = statut != null ? demandeRepository.findByStatut(statut, pageable) : demandeRepository.findAll(pageable);
        return demandes.map(AbonnementMapper::demande);
    }

    @Override
    public DemandeContratResponse traiter(Long id, TraiterDemandeContratRequest request) {
        DemandeContrat demande = demande(id);
        if (!transitionPermise(demande.getStatut(), request.getStatut())) {
            throw new ConflictException("Passage de " + demande.getStatut() + " à " + request.getStatut() + " impossible.");
        }
        demande.setStatut(request.getStatut());
        demande.setObservation(request.getObservation());
        demande.setDateTraitement(LocalDateTime.now());
        demande.setTraitePar(pharmacopeeAuthService.getCurrentUtilisateur());
        return AbonnementMapper.demande(demande);
    }

    private static boolean transitionPermise(StatutDemandeContrat actuel, StatutDemandeContrat cible) {
        return switch (actuel) {
            case NOUVELLE -> cible == StatutDemandeContrat.EN_COURS;
            case EN_COURS -> cible == StatutDemandeContrat.ACCEPTEE || cible == StatutDemandeContrat.REFUSEE;
            case ACCEPTEE, REFUSEE -> cible == StatutDemandeContrat.CLOTUREE;
            case CLOTUREE -> false;
        };
    }

    @Override
    public AbonnementResponse creerAbonnement(Long id, CreerAbonnementContratRequest request) {
        DemandeContrat demande = demande(id);
        if (demande.getStatut() != StatutDemandeContrat.ACCEPTEE) {
            throw new ConflictException("Seule une demande acceptée peut donner lieu à un abonnement.");
        }
        if (demande.getPharmacopee() == null) {
            throw new BadRequestException("Cette demande n'est rattachée à aucune pharmacopée.");
        }
        if (abonnementRepository.existsByDemandeContratId(id)) {
            throw new ConflictException("Un abonnement a déjà été créé pour cette demande.");
        }
        OffreAbonnement offre = offreRepository.findByCode(OFFRE_CONTRAT)
                .orElseThrow(() -> new ResourceNotFoundException("Offre", "code", OFFRE_CONTRAT));
        Utilisateur administrateur = pharmacopeeAuthService.getCurrentUtilisateur();
        Abonnement abonnement = cycleService.creerNegocie(demande.getPharmacopee(), offre, request.getPrix(), request.getDureeMois(),
                request.getJoursGrace(), resoudreQuotas(request.getQuotas()), demande, administrateur);
        cycleService.historiser(abonnement, TypeEvenementAbonnement.SOUSCRIPTION, null, abonnement.getStatut().name(), "Contrat personnalisé", administrateur);
        return AbonnementMapper.abonnement(abonnement, List.of());
    }

    private List<QuotaNegocie> resoudreQuotas(List<LigneQuotaRequest> lignes) {
        List<QuotaNegocie> quotas = new ArrayList<>();
        for (LigneQuotaRequest ligne : lignes) {
            boolean illimite = Boolean.TRUE.equals(ligne.getIllimite());
            if (!illimite && ligne.getValeurMaximale() == null) {
                throw new BadRequestException("Renseignez une valeur maximale, ou indiquez que la limite est illimitée.");
            }
            LimiteOffre limite = limiteRepository.findById(ligne.getLimiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Limite", "id", ligne.getLimiteId()));
            quotas.add(new QuotaNegocie(limite, ligne.getValeurMaximale(), illimite));
        }
        return quotas;
    }

    private DemandeContrat demande(Long id) {
        return demandeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Demande de contrat", "id", id));
    }
}
