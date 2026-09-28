package com.pharmacopee.ladafura.services.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationMethodePaiementInfoDto;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationPaiementResponse;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationProcessPaiementRequest;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationPaiementMapper;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.PaiementRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationPaiementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PopulationPaiementServiceImpl implements IPopulationPaiementService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final IPopulationAuthService populationAuthService;
    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;
    private final PopulationPaiementMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PopulationMethodePaiementInfoDto> getMethodesPaiement() {
        log.info("Consultation des méthodes de paiement disponibles");
        return mapper.getAvailableMethodes();
    }

    @Override
    public PopulationPaiementResponse payerCommande(PopulationProcessPaiementRequest request) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Initiation de paiement pour la commande ID: {} par l'utilisateur ID: {} (méthode: {})",
                request.getCommandeId(), user.getId(), request.getMethode());

        Commande commande = commandeRepository.findByIdAndUtilisateurId(request.getCommandeId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Commande", "id", request.getCommandeId()));

        if (commande.getStatut() == StatutCommande.ANNULEE) {
            throw new BadRequestException("Impossible d'effectuer un paiement sur une commande annulée.");
        }

        Optional<Paiement> existingPaiementOpt = paiementRepository.findByCommandeId(commande.getId());
        if (existingPaiementOpt.isPresent() && existingPaiementOpt.get().getStatut() == StatutPaiement.REUSSI) {
            throw new BadRequestException("Cette commande a déjà été intégralement réglée.");
        }

        Paiement paiement = existingPaiementOpt.orElseGet(() -> Paiement.builder()
                .commande(commande)
                .montant(commande.getMontantTotal())
                .build());

        paiement.setMethode(request.getMethode());
        paiement.setMontant(commande.getMontantTotal());
        paiement.setDatePaiement(LocalDateTime.now());

        // Référence de paiement
        if (request.getReferenceTransaction() != null && !request.getReferenceTransaction().isBlank()) {
            paiement.setReference(request.getReferenceTransaction().trim());
        } else {
            paiement.setReference(generateReference(request.getMethode(), request.getOperateur()));
        }

        boolean simulerReussite = Boolean.TRUE.equals(request.getSimulerSucces());
        String message;

        if (request.getMethode() == MethodePaiement.CASH) {
            paiement.setStatut(StatutPaiement.EN_ATTENTE);
            if (commande.getStatut() == StatutCommande.EN_ATTENTE) {
                commande.setStatut(StatutCommande.CONFIRMEE);
                commande.setDateMiseAJour(LocalDateTime.now());
                commandeRepository.save(commande);
            }
            message = "Option de paiement en espèces sélectionnée. Vous réglerez la somme de "
                    + commande.getMontantTotal() + " FCFA lors de la livraison ou au comptoir de l'officine.";
        } else if (simulerReussite) {
            paiement.setStatut(StatutPaiement.REUSSI);
            if (commande.getStatut() == StatutCommande.EN_ATTENTE) {
                commande.setStatut(StatutCommande.CONFIRMEE);
                commande.setDateMiseAJour(LocalDateTime.now());
                commandeRepository.save(commande);
                log.info("Commande #{} automatiquement confirmée suite au succès du paiement", commande.getNumero());
            }
            String libelle = mapper.formatLibelleMethode(request.getMethode(), request.getOperateur());
            message = "Paiement de " + commande.getMontantTotal() + " FCFA validé avec succès par "
                    + libelle + ". Votre commande est confirmée.";
        } else {
            paiement.setStatut(StatutPaiement.ECHOUE);
            String libelle = mapper.formatLibelleMethode(request.getMethode(), request.getOperateur());
            message = "La transaction via " + libelle + " n'a pas pu aboutir. Veuillez vérifier vos fonds ou utiliser un autre mode de paiement.";
        }

        Paiement savedPaiement = paiementRepository.save(paiement);
        log.info("Paiement ID: {} enregistré avec statut: {} pour la commande ID: {}",
                savedPaiement.getId(), savedPaiement.getStatut(), commande.getId());

        return mapper.toResponse(savedPaiement, commande, message);
    }

    @Override
    @Transactional(readOnly = true)
    public PopulationPaiementResponse getPaiementByCommande(Long commandeId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation du règlement de la commande ID: {} pour l'utilisateur ID: {}", commandeId, user.getId());

        Commande commande = commandeRepository.findByIdAndUtilisateurId(commandeId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Commande", "id", commandeId));

        Paiement paiement = paiementRepository.findByCommandeId(commande.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Aucun règlement enregistré pour la commande ID " + commandeId));

        String message = (paiement.getStatut() == StatutPaiement.REUSSI)
                ? "Paiement validé avec succès."
                : (paiement.getStatut() == StatutPaiement.EN_ATTENTE)
                ? "Règlement en attente de perception."
                : "Transaction non finalisée.";

        return mapper.toResponse(paiement, commande, message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PopulationPaiementResponse> getHistoriquePaiements(Pageable pageable) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation de l'historique des paiements pour l'utilisateur ID: {}", user.getId());

        Page<Paiement> page = paiementRepository.findByCommandeUtilisateurId(user.getId(), pageable);
        return page.map(p -> mapper.toResponse(p, p.getCommande(), "Statut: " + p.getStatut()));
    }

    private String generateReference(MethodePaiement methode, String operateur) {
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
