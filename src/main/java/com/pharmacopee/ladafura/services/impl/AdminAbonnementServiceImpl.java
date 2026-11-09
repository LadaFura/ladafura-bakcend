package com.pharmacopee.ladafura.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.FactureAbonnement;
import com.pharmacopee.ladafura.Models.PaiementAbonnement;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.EvenementAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.FactureAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.LimiteConsommationResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.PaiementAbonnementResponse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutFacture;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.enums.TypeEvenementAbonnement;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AbonnementMapper;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.FactureAbonnementRepository;
import com.pharmacopee.ladafura.repository.HistoriqueAbonnementRepository;
import com.pharmacopee.ladafura.repository.PaiementAbonnementRepository;
import com.pharmacopee.ladafura.repository.ParametrePlateformeRepository;
import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService;
import com.pharmacopee.ladafura.services.interfaces.IAdminAbonnementService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAbonnementServiceImpl implements IAdminAbonnementService {

    static final String PARAMETRE_TAUX_TAXE = "TAUX_TAXE_ABONNEMENT";
    private static final BigDecimal CENT = new BigDecimal("100");

    private final AbonnementRepository abonnementRepository;
    private final PaiementAbonnementRepository paiementRepository;
    private final FactureAbonnementRepository factureRepository;
    private final HistoriqueAbonnementRepository historiqueRepository;
    private final ParametrePlateformeRepository parametreRepository;
    private final IAbonnementCycleService cycleService;
    private final IJournalAuditService journalAuditService;
    private final IPharmacopeeAuthService pharmacopeeAuthService;

    @Override
    @Transactional(readOnly = true)
    public Page<AbonnementResponse> getAbonnements(StatutAbonnement statut, Long pharmacopeeId, Pageable pageable) {
        return abonnementRepository.rechercher(statut, pharmacopeeId, pageable).map(a -> AbonnementMapper.abonnement(a, List.of()));
    }

    @Override
    @Transactional(readOnly = true)
    public AbonnementResponse getAbonnement(Long id) {
        Abonnement abonnement = abonnement(id);
        List<LimiteConsommationResponse> limites = abonnement.getLimites().stream()
                .map(l -> LimiteConsommationResponse.builder().codeLimite(l.getCodeLimite())
                        .valeurMaximale(l.getValeurMaximale()).illimite(l.getIllimite()).build())
                .toList();
        return AbonnementMapper.abonnement(abonnement, limites);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EvenementAbonnementResponse> getHistorique(Long id, Pageable pageable) {
        return historiqueRepository.findByAbonnementIdOrderByDateEvenementDesc(abonnement(id).getId(), pageable).map(AbonnementMapper::evenement);
    }

    @Override
    public AbonnementResponse suspendre(Long id, String motif) {
        Abonnement abonnement = abonnement(id);
        if (!abonnement.estEnVigueur()) {
            throw new ConflictException("Seul un abonnement en vigueur peut être suspendu.");
        }
        changerStatut(abonnement, StatutAbonnement.SUSPENDU, TypeEvenementAbonnement.SUSPENSION, TypeActionAudit.ABONNEMENT_SUSPENDU, motif);
        return AbonnementMapper.abonnement(abonnement, List.of());
    }

    @Override
    public AbonnementResponse reactiver(Long id, String motif) {
        Abonnement abonnement = abonnement(id);
        if (abonnement.getStatut() != StatutAbonnement.SUSPENDU) {
            throw new ConflictException("Seul un abonnement suspendu peut être réactivé.");
        }
        changerStatut(abonnement, StatutAbonnement.ACTIF, TypeEvenementAbonnement.REACTIVATION, TypeActionAudit.ABONNEMENT_SUSPENDU, motif);
        return AbonnementMapper.abonnement(abonnement, List.of());
    }

    @Override
    public AbonnementResponse annuler(Long id, String motif) {
        Abonnement abonnement = abonnement(id);
        StatutAbonnement statut = abonnement.getStatut();
        if (statut == StatutAbonnement.EXPIRE || statut == StatutAbonnement.ANNULE) {
            throw new ConflictException("Cet abonnement est déjà terminé.");
        }
        if (IAbonnementCycleService.OFFRE_GRATUITE.equals(abonnement.getCodeOffreSouscrite())) {
            throw new ConflictException("L'offre gratuite ne peut pas être annulée.");
        }
        boolean remplacerParGratuit = abonnement.estEnVigueur() || statut == StatutAbonnement.SUSPENDU;
        Utilisateur administrateur = administrateur();
        abonnement.setDateAnnulation(LocalDateTime.now());
        abonnement.setMotifAnnulation(motif);
        changerStatut(abonnement, StatutAbonnement.ANNULE, TypeEvenementAbonnement.ANNULATION, TypeActionAudit.ABONNEMENT_ANNULE, motif);
        abonnementRepository.flush();
        if (remplacerParGratuit) {
            cycleService.retournerAuGratuit(abonnement.getPharmacopee(), abonnement, administrateur);
        }
        return AbonnementMapper.abonnement(abonnement, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaiementAbonnementResponse> getPaiements(StatutPaiement statut, Pageable pageable) {
        Page<PaiementAbonnement> paiements = statut != null ? paiementRepository.findByStatut(statut, pageable) : paiementRepository.findAll(pageable);
        return paiements.map(AbonnementMapper::paiement);
    }

    @Override
    public FactureAbonnementResponse confirmerPaiement(Long paiementId) {
        PaiementAbonnement paiement = paiementEnAttente(paiementId);
        Abonnement abonnement = paiement.getAbonnement();
        if (paiement.getMontant().compareTo(abonnement.getPrixSouscrit()) != 0) {
            throw new BadRequestException("Le montant déclaré ne correspond pas au prix de l'abonnement.");
        }
        exigerReferenceInedite(paiement);
        if (paiementRepository.existsByAbonnementIdAndStatut(abonnement.getId(), StatutPaiement.REUSSI)) {
            throw new ConflictException("Un paiement a déjà été confirmé pour cet abonnement.");
        }
        Utilisateur administrateur = administrateur();
        paiement.setStatut(StatutPaiement.REUSSI);
        paiement.setDateValidation(LocalDateTime.now());
        paiement.setValidePar(administrateur);

        cycleService.activerApresPaiement(abonnement, administrateur);
        cycleService.historiser(abonnement, TypeEvenementAbonnement.PAIEMENT_CONFIRME, null, paiement.getReference(), null, administrateur);
        FactureAbonnement facture = emettreFacture(abonnement, paiement, administrateur);
        tracerPaiement(paiement, administrateur, TypeActionAudit.PAIEMENT_ABONNEMENT_CONFIRME, "Paiement confirmé");
        return AbonnementMapper.facture(facture);
    }

    @Override
    public PaiementAbonnementResponse rejeterPaiement(Long paiementId, String motif) {
        PaiementAbonnement paiement = paiementEnAttente(paiementId);
        Utilisateur administrateur = administrateur();
        paiement.setStatut(StatutPaiement.ECHOUE);
        paiement.setMotifRejet(motif);
        paiement.setDateValidation(LocalDateTime.now());
        paiement.setValidePar(administrateur);
        cycleService.historiser(paiement.getAbonnement(), TypeEvenementAbonnement.PAIEMENT_REJETE, null, paiement.getReference(), motif, administrateur);
        tracerPaiement(paiement, administrateur, TypeActionAudit.PAIEMENT_ABONNEMENT_REJETE, motif);
        return AbonnementMapper.paiement(paiement);
    }

    private void exigerReferenceInedite(PaiementAbonnement paiement) {
        String reference = paiement.getReferenceTransaction();
        if (reference != null && !reference.isBlank()
                && paiementRepository.existsByModePaiementAndReferenceTransactionAndStatut(paiement.getModePaiement(), reference, StatutPaiement.REUSSI)) {
            throw new ConflictException("Cette référence de transaction a déjà servi à un paiement confirmé.");
        }
    }

    /** Le client paie le prix affiché : c'est donc un montant TTC, dont on extrait la taxe. */
    private FactureAbonnement emettreFacture(Abonnement abonnement, PaiementAbonnement paiement, Utilisateur administrateur) {
        BigDecimal taux = tauxTaxe();
        BigDecimal ttc = paiement.getMontant();
        BigDecimal ht = ttc.multiply(CENT).divide(CENT.add(taux), 2, RoundingMode.HALF_UP);
        FactureAbonnement facture = factureRepository.save(FactureAbonnement.builder()
                .abonnement(abonnement)
                .paiement(paiement)
                .numeroFacture(prochainNumero())
                .tauxTaxe(taux)
                .montantHT(ht)
                .montantTaxe(ttc.subtract(ht))
                .montantTTC(ttc)
                .devise(paiement.getDevise())
                .statut(StatutFacture.PAYEE)
                .build());
        cycleService.historiser(abonnement, TypeEvenementAbonnement.FACTURE_EMISE, null, facture.getNumeroFacture(), null, administrateur);
        return facture;
    }

    private BigDecimal tauxTaxe() {
        return parametreRepository.findByCode(PARAMETRE_TAUX_TAXE)
                .map(p -> new BigDecimal(p.getValeur()))
                .orElse(BigDecimal.ZERO);
    }

    private String prochainNumero() {
        String prefixe = "LDF-" + Year.now().getValue() + "-";
        int suivant = factureRepository.findFirstByNumeroFactureStartingWithOrderByNumeroFactureDesc(prefixe)
                .map(f -> Integer.parseInt(f.getNumeroFacture().substring(prefixe.length())) + 1)
                .orElse(1);
        return prefixe + String.format("%06d", suivant);
    }

    private void changerStatut(Abonnement abonnement, StatutAbonnement statut, TypeEvenementAbonnement evenement, TypeActionAudit action, String motif) {
        Utilisateur administrateur = administrateur();
        String ancien = abonnement.getStatut().name();
        abonnement.setStatut(statut);
        cycleService.historiser(abonnement, evenement, ancien, statut.name(), motif, administrateur);
        journalAuditService.tracer(EvenementAudit.builder()
                .action(action)
                .origine(OrigineAction.ADMINISTRATION)
                .pharmacopee(abonnement.getPharmacopee())
                .utilisateur(administrateur)
                .typeEntite("Abonnement")
                .entiteId(abonnement.getId())
                .ancienneValeur(ancien)
                .nouvelleValeur(statut.name())
                .description(motif)
                .build());
    }

    private void tracerPaiement(PaiementAbonnement paiement, Utilisateur administrateur, TypeActionAudit action, String description) {
        journalAuditService.tracer(EvenementAudit.builder()
                .action(action)
                .origine(OrigineAction.ADMINISTRATION)
                .pharmacopee(paiement.getAbonnement().getPharmacopee())
                .utilisateur(administrateur)
                .typeEntite("PaiementAbonnement")
                .entiteId(paiement.getId())
                .description(description)
                .build());
    }

    private Abonnement abonnement(Long id) {
        return abonnementRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Abonnement", "id", id));
    }

    private PaiementAbonnement paiementEnAttente(Long id) {
        PaiementAbonnement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "id", id));
        if (paiement.getStatut() != StatutPaiement.EN_ATTENTE) {
            throw new ConflictException("Ce paiement a déjà été traité.");
        }
        return paiement;
    }

    private Utilisateur administrateur() {
        return pharmacopeeAuthService.getCurrentUtilisateur();
    }
}
