package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.DemandeContrat;
import com.pharmacopee.ladafura.Models.LimiteAbonnement;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.PaiementAbonnement;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DeclarerPaiementRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DemandeContratRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DemandeContratResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.FactureAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.LimiteConsommationResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.MotifRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.OffreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.PaiementAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.SouscrireOffreRequest;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.enums.TypeEvenementAbonnement;
import com.pharmacopee.ladafura.enums.TypeOffre;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AbonnementMapper;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.ConfigurationLimiteOffreRepository;
import com.pharmacopee.ladafura.repository.DemandeContratRepository;
import com.pharmacopee.ladafura.repository.FactureAbonnementRepository;
import com.pharmacopee.ladafura.repository.OffreAbonnementRepository;
import com.pharmacopee.ladafura.repository.PaiementAbonnementRepository;
import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAbonnementService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacopeeAbonnementServiceImpl implements IPharmacopeeAbonnementService {

    private final IPharmacopeeContexteService contexteService;
    private final IPharmacopeeQuotaService quotaService;
    private final IAbonnementCycleService cycleService;
    private final IJournalAuditService journalAuditService;
    private final AbonnementRepository abonnementRepository;
    private final OffreAbonnementRepository offreRepository;
    private final ConfigurationLimiteOffreRepository configurationRepository;
    private final PaiementAbonnementRepository paiementRepository;
    private final FactureAbonnementRepository factureRepository;
    private final DemandeContratRepository demandeRepository;

    @Override
    @Transactional(readOnly = true)
    public AbonnementResponse getAbonnementEnVigueur() {
        Long pharmacopeeId = lecture().pharmacopee().getId();
        Abonnement abonnement = abonnementRepository.findEnVigueur(pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Cette pharmacopée n'a pas d'abonnement en vigueur."));
        List<LimiteConsommationResponse> limites = abonnement.getLimites().stream()
                .map(l -> consommation(l, pharmacopeeId))
                .toList();
        return AbonnementMapper.abonnement(abonnement, limites);
    }

    private LimiteConsommationResponse consommation(LimiteAbonnement limite, Long pharmacopeeId) {
        return LimiteConsommationResponse.builder()
                .codeLimite(limite.getCodeLimite())
                .valeurMaximale(limite.getValeurMaximale())
                .illimite(limite.getIllimite())
                .consommation(quotaService.consommationActuelle(pharmacopeeId, limite.getCodeLimite()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonnementResponse> getHistorique(Pageable pageable) {
        return abonnementRepository.findByPharmacopeeIdOrderByDateCreationDesc(lecture().pharmacopee().getId(), pageable)
                .map(a -> AbonnementMapper.abonnement(a, List.of()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FactureAbonnementResponse> getFactures(Pageable pageable) {
        return factureRepository.findByAbonnementPharmacopeeId(lecture().pharmacopee().getId(), pageable).map(AbonnementMapper::facture);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OffreResponse> getOffres() {
        lecture();
        return offreRepository.findByActifTrue().stream()
                .map(o -> AbonnementMapper.offre(o, configurationRepository.findByOffreId(o.getId())))
                .toList();
    }

    @Override
    public AbonnementResponse souscrire(SouscrireOffreRequest request) {
        ContexteMembre contexte = ecriture();
        OffreAbonnement offre = offreRepository.findById(request.getOffreId())
                .orElseThrow(() -> new ResourceNotFoundException("Offre", "id", request.getOffreId()));
        if (!Boolean.TRUE.equals(offre.getActif()) || offre.getTypeOffre() == TypeOffre.GRATUITE
                || offre.getTypeOffre() == TypeOffre.CONTRAT_PERSONNALISE) {
            throw new BadRequestException("Cette offre ne peut pas être souscrite directement.");
        }
        Long pharmacopeeId = contexte.pharmacopee().getId();
        if (abonnementRepository.existsByPharmacopeeIdAndStatut(pharmacopeeId, StatutAbonnement.EN_ATTENTE_PAIEMENT)) {
            throw new ConflictException("Une souscription est déjà en attente de paiement.");
        }
        Abonnement abonnement = cycleService.creerDepuisOffre(contexte.pharmacopee(), offre, contexte.membre());
        cycleService.historiser(abonnement, TypeEvenementAbonnement.SOUSCRIPTION, null, abonnement.getStatut().name(), offre.getCode(), contexte.membre().getUtilisateur());
        tracer(contexte, TypeActionAudit.ABONNEMENT_SOUSCRIT, abonnement, "Souscription à l'offre " + offre.getCode());
        return AbonnementMapper.abonnement(abonnement, List.of());
    }

    @Override
    public PaiementAbonnementResponse declarerPaiement(Long abonnementId, DeclarerPaiementRequest request) {
        ContexteMembre contexte = ecriture();
        Abonnement abonnement = abonnementDe(abonnementId, contexte);
        if (abonnement.getStatut() != StatutAbonnement.EN_ATTENTE_PAIEMENT) {
            throw new ConflictException("Cet abonnement n'est pas en attente de paiement.");
        }
        if (request.getMontant().compareTo(abonnement.getPrixSouscrit()) != 0) {
            throw new BadRequestException("Le montant doit être égal au prix de l'abonnement (" + abonnement.getPrixSouscrit() + ").");
        }
        if (paiementRepository.existsByAbonnementIdAndStatut(abonnement.getId(), StatutPaiement.EN_ATTENTE)) {
            throw new ConflictException("Un paiement déclaré est déjà en cours de vérification.");
        }
        PaiementAbonnement paiement = paiementRepository.save(PaiementAbonnement.builder()
                .abonnement(abonnement)
                .reference("PAB-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase())
                .montant(request.getMontant())
                .devise(abonnement.getDeviseSouscrite())
                .modePaiement(request.getModePaiement())
                .statut(StatutPaiement.EN_ATTENTE)
                .datePaiement(request.getDatePaiement() != null ? request.getDatePaiement() : LocalDateTime.now())
                .referenceTransaction(request.getReferenceTransaction())
                .observation(request.getObservation())
                .declarePar(contexte.membre())
                .build());
        cycleService.historiser(abonnement, TypeEvenementAbonnement.PAIEMENT_DECLARE, null, paiement.getReference(), null, contexte.membre().getUtilisateur());
        return AbonnementMapper.paiement(paiement);
    }

    @Override
    public AbonnementResponse annuler(Long abonnementId, MotifRequest request) {
        ContexteMembre contexte = ecriture();
        Abonnement abonnement = abonnementDe(abonnementId, contexte);
        StatutAbonnement statut = abonnement.getStatut();
        if (statut != StatutAbonnement.EN_ATTENTE_PAIEMENT && statut != StatutAbonnement.PROGRAMME) {
            throw new ConflictException("Seul un abonnement en attente de paiement ou programmé peut être annulé.");
        }
        abonnement.setStatut(StatutAbonnement.ANNULE);
        abonnement.setDateAnnulation(LocalDateTime.now());
        abonnement.setMotifAnnulation(request.getMotif());
        cycleService.historiser(abonnement, TypeEvenementAbonnement.ANNULATION, statut.name(), StatutAbonnement.ANNULE.name(), request.getMotif(), contexte.membre().getUtilisateur());
        tracer(contexte, TypeActionAudit.ABONNEMENT_ANNULE, abonnement, request.getMotif());
        return AbonnementMapper.abonnement(abonnement, List.of());
    }

    @Override
    public DemandeContratResponse demanderContrat(DemandeContratRequest request) {
        ContexteMembre contexte = ecriture();
        DemandeContrat demande = demandeRepository.save(DemandeContrat.builder()
                .pharmacopee(contexte.pharmacopee())
                .demandeur(contexte.membre().getUtilisateur())
                .dureeSouhaiteeMois(request.getDureeSouhaiteeMois())
                .nomContact(request.getNomContact())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .descriptionBesoin(request.getDescriptionBesoin())
                .build());
        return AbonnementMapper.demande(demande);
    }

    private ContexteMembre lecture() {
        return contexteService.verifierPermission(IPharmacopeeContexteService.ABONNEMENT_CONSULTER);
    }

    private ContexteMembre ecriture() {
        return contexteService.verifierPermission(IPharmacopeeContexteService.ABONNEMENT_GERER);
    }

    private Abonnement abonnementDe(Long id, ContexteMembre contexte) {
        return abonnementRepository.findByIdAndPharmacopeeId(id, contexte.pharmacopee().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement", "id", id));
    }

    private void tracer(ContexteMembre contexte, TypeActionAudit action, Abonnement abonnement, String description) {
        journalAuditService.tracer(EvenementAudit.builder()
                .action(action)
                .origine(OrigineAction.PHARMACOPEE)
                .pharmacopee(contexte.pharmacopee())
                .utilisateur(contexte.membre().getUtilisateur())
                .typeEntite("Abonnement")
                .entiteId(abonnement.getId())
                .description(description)
                .build());
    }
}
