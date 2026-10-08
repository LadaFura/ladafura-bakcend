package com.pharmacopee.ladafura.services.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.LigneCommande;
import com.pharmacopee.ladafura.Models.LignePanier;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Panier;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeDetailResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeRecapitulatifRequest;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeRecapitulatifResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeStatutResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCreateCommandeRequest;
import com.pharmacopee.ladafura.dto.population.commande.PopulationLigneCommandeDto;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationCommandeMapper;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.LignePanierRepository;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.PaiementRepository;
import com.pharmacopee.ladafura.repository.PanierRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationCommandeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PopulationCommandeServiceImpl implements IPopulationCommandeService {

    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final IPopulationAuthService populationAuthService;
    private final CommandeRepository commandeRepository;
    private final PanierRepository panierRepository;
    private final LignePanierRepository lignePanierRepository;
    private final PharmacopeeRepository pharmacopeeRepository;
    private final ModeRetraitRepository modeRetraitRepository;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final PaiementRepository paiementRepository;
    private final PopulationCommandeMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PopulationCommandeRecapitulatifResponse getRecapitulatif(PopulationCommandeRecapitulatifRequest request) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Demande de récapitulatif de commande pour l'utilisateur ID: {} (pharmacopeeId: {}, modeRetraitId: {})",
                user.getId(), request.getPharmacopeeId(), request.getModeRetraitId());

        Panier panier = panierRepository.findByUtilisateurId(user.getId())
                .orElseThrow(() -> new BadRequestException("Votre panier est vide. Veuillez y ajouter des articles."));

        if (panier.getLignes() == null || panier.getLignes().isEmpty()) {
            throw new BadRequestException("Votre panier est vide. Veuillez y ajouter des articles.");
        }

        Pharmacopee pharmacopee = loadAndValidatePharmacopee(request.getPharmacopeeId());
        ModeRetrait modeRetrait = loadAndValidateModeRetrait(request.getModeRetraitId(), pharmacopee.getId());

        List<PopulationLigneCommandeDto> lignesDto = new ArrayList<>();
        double totalProduits = 0.0;
        int totalArticles = 0;

        for (LignePanier ligne : panier.getLignes()) {
            Produit produit = ligne.getProduit();
            DisponibiliteProduit disp = checkProductAvailabilityAndStock(pharmacopee.getId(), produit, ligne.getQuantite());

            double prixUnitaire = disp.getPrix() != null ? disp.getPrix() : (produit.getPrix() != null ? produit.getPrix() : 0.0);
            double sousTotal = Math.round(prixUnitaire * ligne.getQuantite() * 100.0) / 100.0;

            totalProduits += sousTotal;
            totalArticles += ligne.getQuantite();

            lignesDto.add(PopulationLigneCommandeDto.builder()
                    .produitId(produit.getId())
                    .nomProduit(produit.getNom())
                    .forme(produit.getForme())
                    .photoUrl(produit.getPhotoUrl())
                    .prixUnitaire(prixUnitaire)
                    .quantite(ligne.getQuantite())
                    .sousTotal(sousTotal)
                    .build());
        }

        double fraisLivraison = (modeRetrait.getType() == TypeModeRetrait.PICKUP)
                ? 0.0
                : (modeRetrait.getFrais() != null ? modeRetrait.getFrais() : 0.0);

        double montantTotal = Math.round((totalProduits + fraisLivraison) * 100.0) / 100.0;

        return PopulationCommandeRecapitulatifResponse.builder()
                .pharmacopeeId(pharmacopee.getId())
                .nomPharmacopee(pharmacopee.getNom())
                .telephonePharmacopee(pharmacopee.getTelephone())
                .modeRetrait(modeRetrait.getType().name())
                .fraisLivraison(fraisLivraison)
                .totalProduits(Math.round(totalProduits * 100.0) / 100.0)
                .montantTotal(montantTotal)
                .nombreArticles(totalArticles)
                .lignes(lignesDto)
                .build();
    }

    @Override
    public PopulationCommandeDetailResponse passerCommande(PopulationCreateCommandeRequest request) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Passage et validation de commande pour l'utilisateur ID: {} (pharmacopeeId: {}, modeRetraitId: {}, methode: {})",
                user.getId(), request.getPharmacopeeId(), request.getModeRetraitId(), request.getMethode());

        if (request.getMethode() == null) {
            throw new BadRequestException("Le moyen de paiement est obligatoire pour valider la commande.");
        }

        Panier panier = panierRepository.findByUtilisateurId(user.getId())
                .orElseThrow(() -> new BadRequestException("Votre panier est vide. Veuillez y ajouter des articles."));

        if (panier.getLignes() == null || panier.getLignes().isEmpty()) {
            throw new BadRequestException("Votre panier est vide. Veuillez y ajouter des articles.");
        }

        Pharmacopee pharmacopee = loadAndValidatePharmacopee(request.getPharmacopeeId());
        ModeRetrait modeRetrait = loadAndValidateModeRetrait(request.getModeRetraitId(), pharmacopee.getId());

        if (modeRetrait.getType() == TypeModeRetrait.LIVRAISON) {
            if (request.getAdresseLivraison() == null || request.getAdresseLivraison().trim().isEmpty()) {
                throw new BadRequestException("L'adresse de livraison est obligatoire pour le mode Livraison à domicile.");
            }
        }

        // 1. Calcul du montant et vérification préalable de la disponibilité sans impacter les stocks
        double totalProduits = 0.0;
        List<LigneCommande> lignesCommande = new ArrayList<>();

        Commande commande = Commande.builder()
                .numero(generateUniqueNumero())
                .dateCommande(LocalDateTime.now())
                .dateMiseAJour(LocalDateTime.now())
                .statut(StatutCommande.EN_ATTENTE)
                .utilisateur(user)
                .pharmacopee(pharmacopee)
                .modeRetrait(modeRetrait)
                .adresseLivraison(request.getAdresseLivraison())
                .build();

        for (LignePanier ligne : panier.getLignes()) {
            Produit produit = ligne.getProduit();
            DisponibiliteProduit disp = checkProductAvailabilityAndStock(pharmacopee.getId(), produit, ligne.getQuantite());

            double prixUnitaire = disp.getPrix() != null ? disp.getPrix() : (produit.getPrix() != null ? produit.getPrix() : 0.0);
            double sousTotal = Math.round(prixUnitaire * ligne.getQuantite() * 100.0) / 100.0;
            totalProduits += sousTotal;

            LigneCommande lc = LigneCommande.builder()
                    .commande(commande)
                    .produit(produit)
                    .quantite(ligne.getQuantite())
                    .prixUnitaire(prixUnitaire)
                    .sousTotal(sousTotal)
                    .build();
            lignesCommande.add(lc);
        }

        double fraisLivraison = (modeRetrait.getType() == TypeModeRetrait.PICKUP)
                ? 0.0
                : (modeRetrait.getFrais() != null ? modeRetrait.getFrais() : 0.0);

        commande.setLignes(lignesCommande);
        commande.setTotalProduit(Math.round(totalProduits * 100.0) / 100.0);
        commande.setMontantLivraison(fraisLivraison);
        commande.setMontantTotal(Math.round((totalProduits + fraisLivraison) * 100.0) / 100.0);

        // 2. VALIDATION STRICTE DU PAIEMENT / RÈGLEMENT
        // Règle métier : Tant que le paiement n'est pas validé, la commande NE DOIT PAS être créée en base,
        // les stocks NE DOIVENT PAS être décrémentés et le panier NE DOIT PAS être vidé.
        boolean simulerReussite = request.getSimulerSucces() == null || Boolean.TRUE.equals(request.getSimulerSucces());

        Paiement paiement = Paiement.builder()
                .commande(commande)
                .montant(commande.getMontantTotal())
                .methode(request.getMethode())
                .datePaiement(LocalDateTime.now())
                .build();

        if (request.getReferenceTransaction() != null && !request.getReferenceTransaction().isBlank()) {
            paiement.setReference(request.getReferenceTransaction().trim());
        } else {
            paiement.setReference(generatePaymentReference(request.getMethode(), request.getOperateur()));
        }

        if (request.getMethode() == MethodePaiement.MOBILE_MONEY) {
            if (request.getTelephoneMobileMoney() == null || request.getTelephoneMobileMoney().trim().isEmpty()) {
                throw new BadRequestException("Le numéro de téléphone Mobile Money est obligatoire pour effectuer le règlement.");
            }
            if (!simulerReussite) {
                throw new BadRequestException("Le règlement via Mobile Money a échoué. Aucun montant n'a été prélevé et la commande n'a pas été enregistrée.");
            }
            paiement.setStatut(StatutPaiement.REUSSI);
            commande.setStatut(StatutCommande.CONFIRMEE);
        } else if (request.getMethode() == MethodePaiement.CARTE_BANCAIRE) {
            if (!simulerReussite) {
                throw new BadRequestException("La transaction par carte bancaire a été rejetée. Votre commande n'a pas été enregistrée.");
            }
            paiement.setStatut(StatutPaiement.REUSSI);
            commande.setStatut(StatutCommande.CONFIRMEE);
        } else if (request.getMethode() == MethodePaiement.CASH) {
            paiement.setStatut(StatutPaiement.EN_ATTENTE);
            commande.setStatut(StatutCommande.CONFIRMEE);
        }

        // 3. PAIEMENT VALIDÉ -> Décrémentation définitive des stocks
        for (LignePanier ligne : panier.getLignes()) {
            Produit produit = ligne.getProduit();
            DisponibiliteProduit disp = checkProductAvailabilityAndStock(pharmacopee.getId(), produit, ligne.getQuantite());
            if (disp.getQuantiteStock() != null) {
                int nouveauStock = disp.getQuantiteStock() - ligne.getQuantite();
                disp.setQuantiteStock(nouveauStock);
                if (nouveauStock <= 0) {
                    disp.setDisponible(false);
                }
                disponibiliteProduitRepository.save(disp);
            }
        }

        // 4. Persistance atomique de la Commande et du Paiement
        commande.setPaiement(paiement);
        Commande savedCommande = commandeRepository.save(commande);
        paiementRepository.save(paiement);

        log.info("Commande #{} créée et confirmée avec succès (Paiement: {} - {}) pour un montant de {} FCFA",
                savedCommande.getNumero(), paiement.getMethode(), paiement.getStatut(), savedCommande.getMontantTotal());

        // 5. Vidage intégral du panier après paiement validé
        panier.getLignes().clear();
        lignePanierRepository.deleteByPanierId(panier.getId());
        panier.setDateModification(LocalDateTime.now());
        panierRepository.save(panier);
        log.info("Panier ID: {} vidé suite à la confirmation du paiement et de la commande ID: {}",
                panier.getId(), savedCommande.getId());

        return mapper.toDetailResponse(savedCommande);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PopulationCommandeSummaryResponse> getHistoriqueCommandes(StatutCommande statut, Pageable pageable) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation de l'historique des commandes pour l'utilisateur ID: {} (filtre statut: {})",
                user.getId(), statut);

        Page<Commande> page = (statut != null)
                ? commandeRepository.findByUtilisateurIdAndStatut(user.getId(), statut, pageable)
                : commandeRepository.findByUtilisateurId(user.getId(), pageable);

        return page.map(mapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PopulationCommandeDetailResponse getCommandeDetail(Long commandeId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation du détail de la commande ID: {} par l'utilisateur ID: {}", commandeId, user.getId());

        Commande commande = commandeRepository.findByIdAndUtilisateurId(commandeId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Commande", "id", commandeId));

        return mapper.toDetailResponse(commande);
    }

    @Override
    @Transactional(readOnly = true)
    public PopulationCommandeStatutResponse getCommandeStatut(Long commandeId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation du statut de la commande ID: {} par l'utilisateur ID: {}", commandeId, user.getId());

        Commande commande = commandeRepository.findByIdAndUtilisateurId(commandeId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Commande", "id", commandeId));

        return mapper.toStatutResponse(commande);
    }

    @Override
    public PopulationCommandeDetailResponse annulerCommande(Long commandeId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Demande d'annulation de la commande ID: {} par l'utilisateur ID: {}", commandeId, user.getId());

        Commande commande = commandeRepository.findByIdAndUtilisateurId(commandeId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Commande", "id", commandeId));

        if (commande.getStatut() == StatutCommande.ANNULEE) {
            throw new BadRequestException("Cette commande est déjà annulée.");
        }

        if (!mapper.isAnnulable(commande.getStatut())) {
            throw new BadRequestException("Impossible d'annuler cette commande car son traitement est déjà trop avancé (statut: "
                    + commande.getStatut() + ").");
        }

        // Réintégration des stocks en pharmacopée
        if (commande.getLignes() != null && commande.getPharmacopee() != null) {
            Long pharmacopeeId = commande.getPharmacopee().getId();
            for (LigneCommande lc : commande.getLignes()) {
                if (lc.getProduit() != null) {
                    disponibiliteProduitRepository.findByPharmacopeeIdAndProduitId(pharmacopeeId, lc.getProduit().getId())
                            .ifPresent(disp -> {
                                if (disp.getQuantiteStock() != null) {
                                    disp.setQuantiteStock(disp.getQuantiteStock() + lc.getQuantite());
                                    disp.setDisponible(true);
                                    disponibiliteProduitRepository.save(disp);
                                    log.info("Stock réintégré pour le produit ID: {} (+{}) dans la pharmacopée ID: {}",
                                            lc.getProduit().getId(), lc.getQuantite(), pharmacopeeId);
                                }
                            });
                }
            }
        }

        commande.setStatut(StatutCommande.ANNULEE);
        commande.setDateMiseAJour(LocalDateTime.now());
        Commande updated = commandeRepository.save(commande);

        log.info("Commande ID: {} annulée avec succès par l'utilisateur ID: {}", commandeId, user.getId());
        return mapper.toDetailResponse(updated);
    }

    private Pharmacopee loadAndValidatePharmacopee(Long pharmacopeeId) {
        Pharmacopee ph = pharmacopeeRepository.findById(pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", pharmacopeeId));

        if (ph.getStatut() != StatutPharmacopee.VALIDEE) {
            throw new BadRequestException("Cette pharmacopée n'est pas agréée pour accepter des commandes.");
        }
        return ph;
    }

    private ModeRetrait loadAndValidateModeRetrait(Long modeRetraitId, Long pharmacopeeId) {
        ModeRetrait mr = modeRetraitRepository.findById(modeRetraitId)
                .orElseThrow(() -> new ResourceNotFoundException("ModeRetrait", "id", modeRetraitId));

        if (mr.getPharmacopee() == null || !mr.getPharmacopee().getId().equals(pharmacopeeId)) {
            throw new BadRequestException("Le mode de retrait spécifié n'appartient pas à la pharmacopée choisie.");
        }

        if (!Boolean.TRUE.equals(mr.getActif())) {
            throw new BadRequestException("Ce mode de retrait n'est actuellement pas actif pour cette pharmacopée.");
        }

        return mr;
    }

    private DisponibiliteProduit checkProductAvailabilityAndStock(Long pharmacopeeId, Produit produit, int quantiteDemandee) {
        DisponibiliteProduit disp = disponibiliteProduitRepository.findByPharmacopeeIdAndProduitId(pharmacopeeId, produit.getId())
                .orElseThrow(() -> new BadRequestException("Le produit '" + produit.getNom()
                        + "' n'est pas référencé auprès de cette pharmacopée."));

        if (!Boolean.TRUE.equals(disp.getDisponible())) {
            throw new BadRequestException("Le produit '" + produit.getNom()
                    + "' est actuellement indisponible dans cette pharmacopée.");
        }

        if (disp.getQuantiteStock() != null && disp.getQuantiteStock() < quantiteDemandee) {
            throw new BadRequestException("Stock insuffisant pour le produit '" + produit.getNom()
                    + "' (demandé: " + quantiteDemandee + ", disponible: " + disp.getQuantiteStock() + ").");
        }

        return disp;
    }

    private String generateUniqueNumero() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String numero;
        do {
            StringBuilder suffix = new StringBuilder(5);
            for (int i = 0; i < 5; i++) {
                suffix.append(ALPHANUMERIC_CHARS.charAt(RANDOM.nextInt(ALPHANUMERIC_CHARS.length())));
            }
            numero = "CMD-" + datePrefix + "-" + suffix;
        } while (commandeRepository.existsByNumero(numero));

        return numero;
    }

    private String generatePaymentReference(MethodePaiement methode, String operateur) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = 10000 + RANDOM.nextInt(90000);
        String prefix = switch (methode) {
            case MOBILE_MONEY -> (operateur != null && operateur.toUpperCase().contains("ORANGE")) ? "PAY-OM-"
                    : (operateur != null && operateur.toUpperCase().contains("MOOV")) ? "PAY-MOOV-"
                    : (operateur != null && operateur.toUpperCase().contains("WAVE")) ? "PAY-WAVE-"
                    : "PAY-MM-";
            case CASH -> "PAY-CASH-";
            case CARTE_BANCAIRE -> "PAY-CB-";
        };
        return prefix + date + "-" + randomNum;
    }
}
