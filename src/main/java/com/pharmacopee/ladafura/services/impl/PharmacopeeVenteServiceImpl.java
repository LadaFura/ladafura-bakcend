package com.pharmacopee.ladafura.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Client;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.LigneVente;
import com.pharmacopee.ladafura.Models.MouvementStock;
import com.pharmacopee.ladafura.Models.PaiementVente;
import com.pharmacopee.ladafura.Models.Vente;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.EnregistrerVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.LigneVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.PaiementVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.PaiementVenteResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.ValiderVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.VenteResponse;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutEcriture;
import com.pharmacopee.ladafura.enums.StatutVente;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.VenteMapper;
import com.pharmacopee.ladafura.repository.ClientRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.MouvementStockRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.PaiementVenteRepository;
import com.pharmacopee.ladafura.repository.VenteRepository;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService.OrigineMouvement;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService.NouvelleOperation;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeVenteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacopeeVenteServiceImpl implements IPharmacopeeVenteService {

    private final IPharmacopeeContexteService contexteService;
    private final IPharmacopeeQuotaService quotaService;
    private final IPharmacopeeMouvementStockService mouvementStockService;
    private final IPharmacopeeOperationCaisseService operationCaisseService;
    private final IJournalAuditService journalAuditService;
    private final VenteRepository venteRepository;
    private final ClientRepository clientRepository;
    private final DisponibiliteProduitRepository disponibiliteRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final PaiementVenteRepository paiementRepository;
    private final OperationCaisseRepository operationCaisseRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<VenteResponse> getVentes(StatutVente statut, Long clientId, Pageable pageable) {
        Page<Vente> ventes = venteRepository.rechercher(pharmacopeeEnLecture(), statut, clientId, pageable);
        Map<Long, BigDecimal> payes = ventes.isEmpty() ? Map.of()
                : paiementRepository.sommesValidesParVente(ventes.map(Vente::getId).getContent()).stream()
                        .collect(Collectors.toMap(l -> (Long) l[0], l -> (BigDecimal) l[1]));
        return ventes.map(v -> VenteMapper.vente(v, payes.getOrDefault(v.getId(), BigDecimal.ZERO), List.of()));
    }

    @Override
    @Transactional(readOnly = true)
    public VenteResponse getVente(Long id) {
        return versReponse(venteDe(pharmacopeeEnLecture(), id));
    }

    @Override
    public VenteResponse creer(EnregistrerVenteRequest request) {
        ContexteMembre contexte = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.VENTES_CREER);
        Vente vente = Vente.builder()
                .pharmacopee(contexte.pharmacopee())
                .numeroVente(prochainNumero(contexte.pharmacopee().getId()))
                .vendeur(contexte.membre())
                .build();
        remplir(vente, request, contexte);
        return versReponse(venteRepository.saveAndFlush(vente));
    }

    @Override
    public VenteResponse modifier(Long id, EnregistrerVenteRequest request) {
        ContexteMembre contexte = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.VENTES_CREER);
        Vente vente = venteDe(contexte.pharmacopee().getId(), id);
        exigerEnAttente(vente);
        vente.getLignes().clear();
        remplir(vente, request, contexte);
        return versReponse(venteRepository.saveAndFlush(vente));
    }

    private void remplir(Vente vente, EnregistrerVenteRequest request, ContexteMembre contexte) {
        Long pharmacopeeId = contexte.pharmacopee().getId();
        vente.setClient(clientActif(request.getClientId(), pharmacopeeId));
        vente.setMontantRemise(request.getMontantRemise() != null ? request.getMontantRemise() : BigDecimal.ZERO);
        vente.setObservation(request.getObservation());
        List<LigneVenteRequest> lignes = request.getLignes() != null ? request.getLignes() : List.of();
        for (LigneVenteRequest ligne : lignes) {
            DisponibiliteProduit fiche = disponibiliteRepository.findByIdAndPharmacopeeId(ligne.getDisponibiliteProduitId(), pharmacopeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Fiche de stock", "id", ligne.getDisponibiliteProduitId()));
            vente.ajouterLigne(LigneVente.builder()
                    .disponibiliteProduit(fiche)
                    .quantite(ligne.getQuantite())
                    .prixUnitaire(prixDe(fiche))
                    .remise(ligne.getRemise())
                    .build());
        }
        vente.recalculerMontants();
        exigerMontantsCoherents(vente);
        exigerDroitDeRemise(vente, contexte);
    }

    private Client clientActif(Long clientId, Long pharmacopeeId) {
        if (clientId == null) {
            return null;
        }
        Client client = clientRepository.findByIdAndPharmacopeeId(clientId, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));
        if (!Boolean.TRUE.equals(client.getActif())) {
            throw new BadRequestException("Ce client est désactivé.");
        }
        return client;
    }

    /** Le prix de la pharmacopée, sinon le prix national du produit. */
    private static BigDecimal prixDe(DisponibiliteProduit fiche) {
        Double prix = fiche.getPrix() != null ? fiche.getPrix() : fiche.getProduit().getPrix();
        return BigDecimal.valueOf(prix);
    }

    private static void exigerMontantsCoherents(Vente vente) {
        if (vente.getMontantTotal().signum() < 0 || vente.getLignes().stream().anyMatch(l -> l.getSousTotal().signum() < 0)) {
            throw new BadRequestException("Les remises ne peuvent pas dépasser le montant de la vente.");
        }
    }

    private static void exigerDroitDeRemise(Vente vente, ContexteMembre contexte) {
        boolean remise = vente.getMontantRemise().signum() > 0 || vente.getLignes().stream().anyMatch(l -> l.getRemise().signum() > 0);
        if (remise && !contexte.permissions().contains(IPharmacopeeContexteService.VENTES_REMISE)) {
            throw new ForbiddenException("Vous n'avez pas la permission d'accorder une remise.");
        }
    }

    private String prochainNumero(Long pharmacopeeId) {
        String prefixe = "VTE-" + Year.now().getValue() + "-";
        int suivant = venteRepository.findFirstByPharmacopeeIdAndNumeroVenteStartingWithOrderByNumeroVenteDesc(pharmacopeeId, prefixe)
                .map(v -> Integer.parseInt(v.getNumeroVente().substring(prefixe.length())) + 1)
                .orElse(1);
        return prefixe + String.format("%06d", suivant);
    }

    @Override
    public VenteResponse abandonner(Long id) {
        ContexteMembre contexte = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.VENTES_CREER);
        Vente vente = venteDe(contexte.pharmacopee().getId(), id);
        exigerEnAttente(vente);
        vente.setStatut(StatutVente.ABANDONNEE);
        return versReponse(vente);
    }

    @Override
    public VenteResponse valider(Long id, ValiderVenteRequest request) {
        ContexteMembre contexte = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.VENTES_CREER);
        Long pharmacopeeId = contexte.pharmacopee().getId();
        Vente vente = venteDe(pharmacopeeId, id);
        exigerEnAttente(vente);
        if (vente.getLignes().isEmpty()) {
            throw new BadRequestException("Une vente doit contenir au moins une ligne pour être validée.");
        }
        quotaService.verifierQuota(pharmacopeeId, IPharmacopeeQuotaService.MAX_VENTES_MENSUELLES);

        vente.getLignes().forEach(l -> l.setPrixUnitaire(prixDe(l.getDisponibiliteProduit())));
        vente.recalculerMontants();
        exigerMontantsCoherents(vente);

        List<PaiementVenteRequest> paiements = request != null && request.getPaiements() != null ? request.getPaiements() : List.of();
        exigerPaiementsValides(vente, paiements, contexte);

        for (LigneVente ligne : vente.getLignes()) {
            mouvementStockService.sortir(pharmacopeeId, ligne.getDisponibiliteProduit().getId(), ligne.getQuantite(),
                    TypeMouvementStock.SORTIE_VENTE, OrigineMouvement.vente(ligne), "Vente " + vente.getNumeroVente(), contexte.membre());
        }
        vente.setStatut(StatutVente.VALIDEE);
        vente.setDateVente(LocalDateTime.now());
        vente.setDateValidation(LocalDateTime.now());
        vente.setValidePar(contexte.membre());
        paiements.forEach(p -> enregistrer(vente, p, contexte));
        return versReponse(vente);
    }

    private static void exigerPaiementsValides(Vente vente, List<PaiementVenteRequest> paiements, ContexteMembre contexte) {
        if (!paiements.isEmpty() && !contexte.permissions().contains(IPharmacopeeContexteService.PAIEMENTS_ENCAISSER)) {
            throw new ForbiddenException("Vous n'avez pas la permission d'encaisser un paiement.");
        }
        BigDecimal recu = paiements.stream().map(PaiementVenteRequest::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
        int comparaison = recu.compareTo(vente.getMontantTotal());
        if (comparaison > 0) {
            throw new BadRequestException("Les paiements dépassent le montant de la vente.");
        }
        if (comparaison < 0 && vente.getClient() == null) {
            throw new BadRequestException("Une vente à crédit doit être rattachée à un client.");
        }
    }

    private PaiementVente enregistrer(Vente vente, PaiementVenteRequest request, ContexteMembre contexte) {
        PaiementVente paiement = paiementRepository.save(PaiementVente.builder()
                .vente(vente)
                .reference("PVT-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase())
                .montant(request.getMontant())
                .methode(request.getMethode())
                .referenceTransaction(request.getReferenceTransaction())
                .observation(request.getObservation())
                .enregistrePar(contexte.membre())
                .build());
        operationCaisseService.enregistrer(contexte.pharmacopee().getId(), NouvelleOperation.builder()
                .caisseId(request.getCaisseId())
                .sens(SensOperation.ENTREE)
                .nature(NatureOperationCaisse.VENTE)
                .montant(request.getMontant())
                .methode(request.getMethode())
                .libelle("Paiement de la vente " + vente.getNumeroVente())
                .auteur(contexte.membre())
                .paiementVente(paiement)
                .build());
        return paiement;
    }

    @Override
    public VenteResponse annuler(Long id, String motif) {
        ContexteMembre contexte = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.VENTES_ANNULER);
        Long pharmacopeeId = contexte.pharmacopee().getId();
        Vente vente = venteDe(pharmacopeeId, id);
        if (!vente.estValidee()) {
            throw new ConflictException("Seule une vente validée peut être annulée.");
        }
        List<MouvementStock> sorties = new ArrayList<>();
        for (LigneVente ligne : vente.getLignes()) {
            sorties.addAll(mouvementStockRepository.findByLigneVenteId(ligne.getId()));
        }
        mouvementStockService.annuler(sorties, TypeMouvementStock.ANNULATION_VENTE, motif, contexte.membre());
        for (PaiementVente paiement : paiementRepository.findByVenteId(vente.getId())) {
            if (paiement.getStatut() == StatutEcriture.VALIDE) {
                contrePasser(paiement, motif, contexte);
            }
        }
        vente.setStatut(StatutVente.ANNULEE);
        vente.setDateAnnulation(LocalDateTime.now());
        vente.setMotifAnnulation(motif);
        vente.setAnnulePar(contexte.membre());
        tracer(contexte, TypeActionAudit.VENTE_ANNULEE, "Vente", vente.getId(), motif);
        return versReponse(vente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaiementVenteResponse> getPaiements(Long venteId) {
        return paiementRepository.findByVenteId(venteDe(pharmacopeeEnLecture(), venteId).getId()).stream().map(VenteMapper::paiement).toList();
    }

    @Override
    public PaiementVenteResponse enregistrerPaiement(Long venteId, PaiementVenteRequest request) {
        ContexteMembre contexte = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.PAIEMENTS_ENCAISSER);
        Vente vente = venteDe(contexte.pharmacopee().getId(), venteId);
        if (!vente.estValidee()) {
            throw new ConflictException("Seule une vente validée peut recevoir un paiement.");
        }
        BigDecimal reste = vente.getMontantTotal().subtract(paiementRepository.sumMontantValide(vente.getId()));
        if (request.getMontant().compareTo(reste) > 0) {
            throw new BadRequestException("Le paiement dépasse le reste à payer (" + reste + ").");
        }
        return VenteMapper.paiement(enregistrer(vente, request, contexte));
    }

    @Override
    public PaiementVenteResponse annulerPaiement(Long venteId, Long paiementId, String motif) {
        ContexteMembre contexte = contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.VENTES_ANNULER);
        PaiementVente paiement = paiementRepository.findByIdAndVentePharmacopeeId(paiementId, contexte.pharmacopee().getId())
                .filter(p -> p.getVente().getId().equals(venteId))
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "id", paiementId));
        if (paiement.getStatut() != StatutEcriture.VALIDE) {
            throw new ConflictException("Ce paiement est déjà annulé.");
        }
        contrePasser(paiement, motif, contexte);
        tracer(contexte, TypeActionAudit.PAIEMENT_VENTE_ANNULE, "PaiementVente", paiement.getId(), motif);
        return VenteMapper.paiement(paiement);
    }

    private void contrePasser(PaiementVente paiement, String motif, ContexteMembre contexte) {
        Long operationId = operationCaisseRepository
                .findFirstByPaiementVenteIdAndNatureNot(paiement.getId(), NatureOperationCaisse.ANNULATION)
                .orElseThrow(() -> new ResourceNotFoundException("Opération de caisse du paiement", "id", paiement.getId()))
                .getId();
        operationCaisseService.contrePasser(contexte.pharmacopee().getId(), operationId, "Annulation d'un paiement : " + motif, contexte.membre());
        paiement.setStatut(StatutEcriture.ANNULE);
        paiement.setDateAnnulation(LocalDateTime.now());
        paiement.setMotifAnnulation(motif);
    }

    private Long pharmacopeeEnLecture() {
        return contexteService.verifierPermission(IPharmacopeeContexteService.VENTES_CONSULTER).pharmacopee().getId();
    }

    private Vente venteDe(Long pharmacopeeId, Long id) {
        return venteRepository.findByIdAndPharmacopeeId(id, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Vente", "id", id));
    }

    private static void exigerEnAttente(Vente vente) {
        if (!vente.estEnAttente()) {
            throw new ConflictException("Seule une vente en attente peut être modifiée, abandonnée ou validée.");
        }
    }

    private VenteResponse versReponse(Vente vente) {
        return VenteMapper.vente(vente, paiementRepository.sumMontantValide(vente.getId()),
                vente.getLignes().stream().map(VenteMapper::ligne).toList());
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
