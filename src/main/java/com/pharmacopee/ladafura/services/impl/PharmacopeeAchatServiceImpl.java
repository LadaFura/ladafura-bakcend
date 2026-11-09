package com.pharmacopee.ladafura.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Achat;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Fournisseur;
import com.pharmacopee.ladafura.Models.LigneAchat;
import com.pharmacopee.ladafura.Models.Lot;
import com.pharmacopee.ladafura.Models.MouvementStock;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.ReglementAchat;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.AchatResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.EnregistrerAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.EnregistrerReglementAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.LigneAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.ReglementAchatResponse;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutAchat;
import com.pharmacopee.ladafura.enums.StatutEcriture;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AchatMapper;
import com.pharmacopee.ladafura.repository.AchatRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.FournisseurRepository;
import com.pharmacopee.ladafura.repository.LotRepository;
import com.pharmacopee.ladafura.repository.MouvementStockRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.repository.ReglementAchatRepository;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAchatService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService.OrigineMouvement;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService.NouvelleOperation;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacopeeAchatServiceImpl implements IPharmacopeeAchatService {

    private final IPharmacopeeContexteService contexteService;
    private final IPharmacopeeQuotaService quotaService;
    private final IPharmacopeeMouvementStockService mouvementStockService;
    private final IPharmacopeeOperationCaisseService operationCaisseService;
    private final IJournalAuditService journalAuditService;
    private final AchatRepository achatRepository;
    private final FournisseurRepository fournisseurRepository;
    private final ProduitRepository produitRepository;
    private final DisponibiliteProduitRepository disponibiliteRepository;
    private final LotRepository lotRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final ReglementAchatRepository reglementRepository;
    private final OperationCaisseRepository operationCaisseRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AchatResponse> getAchats(StatutAchat statut, Long fournisseurId, Pageable pageable) {
        Long pharmacopeeId = contexteService.verifierPermission(IPharmacopeeContexteService.ACHATS_CONSULTER).pharmacopee().getId();
        Page<Achat> achats = achatRepository.rechercher(pharmacopeeId, statut, fournisseurId, pageable);
        Map<Long, BigDecimal> payes = achats.isEmpty() ? Map.of()
                : reglementRepository.sommesValidesParAchat(achats.map(Achat::getId).getContent()).stream()
                        .collect(Collectors.toMap(l -> (Long) l[0], l -> (BigDecimal) l[1]));
        return achats.map(a -> AchatMapper.achat(a, payes.getOrDefault(a.getId(), BigDecimal.ZERO), List.of()));
    }

    @Override
    @Transactional(readOnly = true)
    public AchatResponse getAchat(Long id) {
        Long pharmacopeeId = contexteService.verifierPermission(IPharmacopeeContexteService.ACHATS_CONSULTER).pharmacopee().getId();
        return versReponse(achatDe(pharmacopeeId, id));
    }

    @Override
    public AchatResponse creer(EnregistrerAchatRequest request) {
        ContexteMembre contexte = ecriture();
        Pharmacopee pharmacopee = contexte.pharmacopee();
        Achat achat = Achat.builder()
                .pharmacopee(pharmacopee)
                .numeroAchat(prochainNumero(pharmacopee.getId()))
                .enregistrePar(contexte.membre())
                .build();
        remplir(achat, request, pharmacopee.getId());
        return versReponse(achatRepository.saveAndFlush(achat));
    }

    @Override
    public AchatResponse modifier(Long id, EnregistrerAchatRequest request) {
        ContexteMembre contexte = ecriture();
        Achat achat = achatDe(contexte.pharmacopee().getId(), id);
        exigerBrouillon(achat);
        achat.getLignes().clear();
        remplir(achat, request, contexte.pharmacopee().getId());
        return versReponse(achatRepository.saveAndFlush(achat));
    }

    private void remplir(Achat achat, EnregistrerAchatRequest request, Long pharmacopeeId) {
        achat.setFournisseur(fournisseurActif(request.getFournisseurId(), pharmacopeeId));
        if (request.getDateAchat() != null) {
            achat.setDateAchat(request.getDateAchat());
        }
        achat.setMontantRemise(request.getMontantRemise() != null ? request.getMontantRemise() : BigDecimal.ZERO);
        achat.setObservation(request.getObservation());
        List<LigneAchatRequest> lignes = request.getLignes() != null ? request.getLignes() : List.of();
        for (LigneAchatRequest ligne : lignes) {
            achat.ajouterLigne(LigneAchat.builder()
                    .produit(produitValide(ligne.getProduitId()))
                    .quantite(ligne.getQuantite())
                    .prixUnitaire(ligne.getPrixUnitaire())
                    .remise(ligne.getRemise())
                    .numeroLot(ligne.getNumeroLot())
                    .datePeremption(ligne.getDatePeremption())
                    .build());
        }
        achat.recalculerMontants();
        if (achat.getMontantTotal().signum() < 0 || achat.getLignes().stream().anyMatch(l -> l.getSousTotal().signum() < 0)) {
            throw new BadRequestException("Les remises ne peuvent pas dépasser le montant de l'achat.");
        }
    }

    private Fournisseur fournisseurActif(Long fournisseurId, Long pharmacopeeId) {
        if (fournisseurId == null) {
            return null;
        }
        Fournisseur fournisseur = fournisseurRepository.findByIdAndPharmacopeeId(fournisseurId, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", "id", fournisseurId));
        if (!fournisseur.estActif()) {
            throw new BadRequestException("Ce fournisseur est inactif.");
        }
        return fournisseur;
    }

    private Produit produitValide(Long id) {
        Produit produit = produitRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Produit", "id", id));
        if (produit.getStatut() != StatutProduit.VALIDE) {
            throw new BadRequestException("Le produit « " + produit.getNom() + " » n'est pas validé au catalogue.");
        }
        return produit;
    }

    private String prochainNumero(Long pharmacopeeId) {
        String prefixe = "ACH-" + Year.now().getValue() + "-";
        int suivant = achatRepository.findFirstByPharmacopeeIdAndNumeroAchatStartingWithOrderByNumeroAchatDesc(pharmacopeeId, prefixe)
                .map(a -> Integer.parseInt(a.getNumeroAchat().substring(prefixe.length())) + 1)
                .orElse(1);
        return prefixe + String.format("%06d", suivant);
    }

    @Override
    public void supprimer(Long id) {
        Achat achat = achatDe(ecriture().pharmacopee().getId(), id);
        exigerBrouillon(achat);
        achatRepository.delete(achat);
    }

    @Override
    public AchatResponse valider(Long id) {
        ContexteMembre contexte = ecriture();
        Long pharmacopeeId = contexte.pharmacopee().getId();
        Achat achat = achatDe(pharmacopeeId, id);
        exigerBrouillon(achat);
        if (achat.getLignes().isEmpty()) {
            throw new BadRequestException("Un achat doit contenir au moins une ligne pour être validé.");
        }
        for (LigneAchat ligne : achat.getLignes()) {
            entrerEnStock(ligne, contexte, achat.getNumeroAchat());
        }
        achat.setStatut(StatutAchat.VALIDE);
        achat.setDateValidation(LocalDateTime.now());
        achat.setValidePar(contexte.membre());
        return versReponse(achat);
    }

    private void entrerEnStock(LigneAchat ligne, ContexteMembre contexte, String numeroAchat) {
        Produit produit = produitValide(ligne.getProduit().getId());
        Pharmacopee pharmacopee = contexte.pharmacopee();
        DisponibiliteProduit fiche = disponibiliteRepository.findByPharmacopeeIdAndProduitIdForUpdate(pharmacopee.getId(), produit.getId())
                .orElseGet(() -> creerFiche(pharmacopee, produit));
        Lot lot = null;
        if (ligne.getNumeroLot() != null || ligne.getDatePeremption() != null) {
            lot = lotRepository.save(Lot.builder()
                    .pharmacopee(pharmacopee)
                    .disponibiliteProduit(fiche)
                    .ligneAchat(ligne)
                    .numeroLot(ligne.getNumeroLot())
                    .datePeremption(ligne.getDatePeremption())
                    .quantiteInitiale(ligne.getQuantite())
                    .build());
        }
        mouvementStockService.entrer(pharmacopee.getId(), fiche.getId(), ligne.getQuantite(), TypeMouvementStock.ENTREE_ACHAT,
                lot, OrigineMouvement.achat(ligne), "Achat " + numeroAchat, contexte.membre());
    }

    /** Non visible en vente tant que la pharmacopée ne l'a pas activée. */
    private DisponibiliteProduit creerFiche(Pharmacopee pharmacopee, Produit produit) {
        quotaService.verifierQuota(pharmacopee.getId(), IPharmacopeeQuotaService.MAX_PRODUITS);
        return disponibiliteRepository.saveAndFlush(DisponibiliteProduit.builder()
                .pharmacopee(pharmacopee).produit(produit).disponible(false).quantiteStock(0).build());
    }

    @Override
    public AchatResponse annuler(Long id, String motif) {
        ContexteMembre contexte = ecriture();
        Achat achat = achatDe(contexte.pharmacopee().getId(), id);
        if (achat.getStatut() == StatutAchat.ANNULE) {
            throw new ConflictException("Cet achat est déjà annulé.");
        }
        if (achat.estValide()) {
            annulerEntrees(achat, motif, contexte);
        }
        achat.setStatut(StatutAchat.ANNULE);
        achat.setDateAnnulation(LocalDateTime.now());
        achat.setMotifAnnulation(motif);
        achat.setAnnulePar(contexte.membre());
        tracer(contexte, TypeActionAudit.ACHAT_ANNULE, "Achat", achat.getId(), motif);
        return versReponse(achat);
    }

    private void annulerEntrees(Achat achat, String motif, ContexteMembre contexte) {
        if (reglementRepository.sumMontantValide(achat.getId()).signum() > 0) {
            throw new ConflictException("Annulez d'abord les règlements de cet achat.");
        }
        List<MouvementStock> entrees = new ArrayList<>();
        for (LigneAchat ligne : achat.getLignes()) {
            entrees.addAll(mouvementStockRepository.findByLigneAchatId(ligne.getId()));
        }
        mouvementStockService.annuler(entrees, TypeMouvementStock.ANNULATION_ACHAT, motif, contexte.membre());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReglementAchatResponse> getReglements(Long achatId) {
        Long pharmacopeeId = contexteService.verifierPermission(IPharmacopeeContexteService.ACHATS_CONSULTER).pharmacopee().getId();
        return reglementRepository.findByAchatId(achatDe(pharmacopeeId, achatId).getId()).stream().map(AchatMapper::reglement).toList();
    }

    @Override
    public ReglementAchatResponse enregistrerReglement(Long achatId, EnregistrerReglementAchatRequest request) {
        ContexteMembre contexte = ecriture();
        Long pharmacopeeId = contexte.pharmacopee().getId();
        Achat achat = achatDe(pharmacopeeId, achatId);
        if (!achat.estValide()) {
            throw new ConflictException("Seul un achat validé peut être réglé.");
        }
        BigDecimal reste = achat.getMontantTotal().subtract(reglementRepository.sumMontantValide(achat.getId()));
        if (request.getMontant().compareTo(reste) > 0) {
            throw new BadRequestException("Le règlement dépasse le reste à payer (" + reste + ").");
        }
        ReglementAchat reglement = reglementRepository.save(ReglementAchat.builder()
                .achat(achat)
                .montant(request.getMontant())
                .dateReglement(request.getDateReglement())
                .methode(request.getMethode())
                .reference(request.getReference())
                .observation(request.getObservation())
                .enregistrePar(contexte.membre())
                .build());
        operationCaisseService.enregistrer(pharmacopeeId, NouvelleOperation.builder()
                .caisseId(request.getCaisseId())
                .sens(SensOperation.SORTIE)
                .nature(NatureOperationCaisse.ACHAT)
                .montant(request.getMontant())
                .methode(request.getMethode())
                .libelle("Règlement de l'achat " + achat.getNumeroAchat())
                .auteur(contexte.membre())
                .reglementAchat(reglement)
                .build());
        return AchatMapper.reglement(reglement);
    }

    @Override
    public ReglementAchatResponse annulerReglement(Long achatId, Long reglementId, String motif) {
        ContexteMembre contexte = ecriture();
        Long pharmacopeeId = contexte.pharmacopee().getId();
        ReglementAchat reglement = reglementRepository.findByIdAndAchatPharmacopeeId(reglementId, pharmacopeeId)
                .filter(r -> r.getAchat().getId().equals(achatId))
                .orElseThrow(() -> new ResourceNotFoundException("Règlement", "id", reglementId));
        if (reglement.getStatut() != StatutEcriture.VALIDE) {
            throw new ConflictException("Ce règlement est déjà annulé.");
        }
        Long operationId = operationCaisseRepository
                .findFirstByReglementAchatIdAndNatureNot(reglement.getId(), NatureOperationCaisse.ANNULATION)
                .orElseThrow(() -> new ResourceNotFoundException("Opération de caisse du règlement", "id", reglementId))
                .getId();
        operationCaisseService.contrePasser(pharmacopeeId, operationId, "Annulation d'un règlement : " + motif, contexte.membre());
        reglement.setStatut(StatutEcriture.ANNULE);
        reglement.setDateAnnulation(LocalDateTime.now());
        reglement.setMotifAnnulation(motif);
        tracer(contexte, TypeActionAudit.REGLEMENT_ACHAT_ANNULE, "ReglementAchat", reglement.getId(), motif);
        return AchatMapper.reglement(reglement);
    }

    private ContexteMembre ecriture() {
        return contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.ACHATS_GERER);
    }

    private Achat achatDe(Long pharmacopeeId, Long id) {
        return achatRepository.findByIdAndPharmacopeeId(id, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Achat", "id", id));
    }

    private static void exigerBrouillon(Achat achat) {
        if (!achat.estBrouillon()) {
            throw new ConflictException("Seul un achat en brouillon peut être modifié, supprimé ou validé.");
        }
    }

    private AchatResponse versReponse(Achat achat) {
        return AchatMapper.achat(achat, reglementRepository.sumMontantValide(achat.getId()),
                achat.getLignes().stream().map(AchatMapper::ligne).toList());
    }

    private void tracer(ContexteMembre contexte, TypeActionAudit action, String typeEntite, Long entiteId, String description) {
        journalAuditService.tracer(EvenementAudit.builder()
                .action(action)
                .origine(OrigineAction.PHARMACOPEE)
                .pharmacopee(contexte.pharmacopee())
                .utilisateur(contexte.membre().getUtilisateur())
                .typeEntite(typeEntite)
                .entiteId(entiteId)
                .description(description)
                .build());
    }
}
