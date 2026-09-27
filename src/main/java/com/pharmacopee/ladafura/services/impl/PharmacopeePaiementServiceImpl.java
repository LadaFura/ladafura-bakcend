package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.EncaisserCashRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.PharmacopeePaiementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.PharmacopeePaiementSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.SimulerPaiementRequest;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.PaiementRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeePaiementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacopeePaiementServiceImpl implements IPharmacopeePaiementService {

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PharmacopeePaiementResponse> getPaiements(StatutPaiement statut, MethodePaiement methode, Pageable pageable) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Consultation paginée des règlements pour la pharmacopée ID {} (statut={}, methode={})", pId, statut, methode);

        Page<Paiement> page;
        if (statut != null) {
            page = paiementRepository.findByCommandePharmacopeeIdAndStatut(pId, statut, pageable);
        } else if (methode != null) {
            page = paiementRepository.findByCommandePharmacopeeIdAndMethode(pId, methode, pageable);
        } else {
            page = paiementRepository.findByCommandePharmacopeeId(pId, pageable);
        }

        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacopeePaiementSummaryResponse getSummary() {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Calcul de la synthèse financière pour la pharmacopée ID {}", pId);

        long reussis = paiementRepository.countByCommandePharmacopeeIdAndStatut(pId, StatutPaiement.REUSSI);
        long enAttente = paiementRepository.countByCommandePharmacopeeIdAndStatut(pId, StatutPaiement.EN_ATTENTE);
        long echoues = paiementRepository.countByCommandePharmacopeeIdAndStatut(pId, StatutPaiement.ECHOUE);
        long total = reussis + enAttente + echoues;

        Double totalEncaisse = paiementRepository.sumMontantByPharmacopeeIdAndStatut(pId, StatutPaiement.REUSSI);
        Double mmEncaisse = paiementRepository.sumMontantByPharmacopeeIdAndMethodeAndStatut(
                pId, MethodePaiement.MOBILE_MONEY, StatutPaiement.REUSSI);
        Double cashEncaisse = paiementRepository.sumMontantByPharmacopeeIdAndMethodeAndStatut(
                pId, MethodePaiement.CASH, StatutPaiement.REUSSI);
        Double cbEncaisse = paiementRepository.sumMontantByPharmacopeeIdAndMethodeAndStatut(
                pId, MethodePaiement.CARTE_BANCAIRE, StatutPaiement.REUSSI);

        return PharmacopeePaiementSummaryResponse.builder()
                .totalTransactions(total)
                .totalReussis(reussis)
                .totalEnAttente(enAttente)
                .totalEchoues(echoues)
                .totalEncaisse(totalEncaisse != null ? totalEncaisse : 0.0)
                .encaisseMobileMoney(mmEncaisse != null ? mmEncaisse : 0.0)
                .encaisseCash(cashEncaisse != null ? cashEncaisse : 0.0)
                .encaisseCarteBancaire(cbEncaisse != null ? cbEncaisse : 0.0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacopeePaiementResponse getPaiementDetail(Long id) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        Paiement paiement = paiementRepository.findByIdAndCommandePharmacopeeId(id, pharmacopee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Règlement financier introuvable avec l'ID " + id
                        + " pour votre pharmacopée."));

        return mapToResponse(paiement);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacopeePaiementResponse getPaiementByCommande(Long commandeId) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        Paiement paiement = paiementRepository.findByCommandeIdAndCommandePharmacopeeId(commandeId, pharmacopee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Aucun règlement financier trouvé pour la commande ID " + commandeId
                        + " dans votre officine."));

        return mapToResponse(paiement);
    }

    @Override
    public PharmacopeePaiementResponse simulerPaiement(SimulerPaiementRequest request) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        log.info("Simulation de paiement pour la commande ID {} (méthode={}, succès={})",
                request.getCommandeId(), request.getMethode(), request.getSimulerSucces());

        Commande commande = commandeRepository.findByIdAndPharmacopeeId(request.getCommandeId(), pharmacopee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec l'ID " + request.getCommandeId()
                        + " pour votre pharmacopée."));

        Paiement paiement = paiementRepository.findByCommandeId(commande.getId())
                .orElseGet(() -> Paiement.builder()
                        .commande(commande)
                        .montant(commande.getMontantTotal())
                        .build());

        paiement.setMethode(request.getMethode());
        paiement.setMontant(commande.getMontantTotal());
        paiement.setDatePaiement(LocalDateTime.now());

        // Génération ou conservation de la référence
        if (request.getReferencePaiement() != null && !request.getReferencePaiement().isBlank()) {
            paiement.setReference(request.getReferencePaiement());
        } else {
            String prefix = switch (request.getMethode()) {
                case MOBILE_MONEY -> "SIM-OM-";
                case CASH -> "SIM-CASH-";
                case CARTE_BANCAIRE -> "SIM-CB-";
            };
            paiement.setReference(prefix + System.currentTimeMillis() + "-" + (int) (Math.random() * 900 + 100));
        }

        boolean succes = Boolean.TRUE.equals(request.getSimulerSucces());
        paiement.setStatut(succes ? StatutPaiement.REUSSI : StatutPaiement.ECHOUE);

        Paiement savedPaiement = paiementRepository.save(paiement);

        // Si le paiement réussit et que la commande était en attente, confirmation automatique
        if (succes && commande.getStatut() == StatutCommande.EN_ATTENTE) {
            commande.setStatut(StatutCommande.CONFIRMEE);
            commande.setDateMiseAJour(LocalDateTime.now());
            commandeRepository.save(commande);
            log.info("Commande #{} automatiquement passée à CONFIRMEE suite au succès du paiement simulé", commande.getNumero());
        }

        return mapToResponse(savedPaiement);
    }

    @Override
    public PharmacopeePaiementResponse encaisserCash(Long paiementId, EncaisserCashRequest request) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        Paiement paiement = paiementRepository.findByIdAndCommandePharmacopeeId(paiementId, pharmacopee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Règlement financier introuvable avec l'ID " + paiementId
                        + " pour votre pharmacopée."));

        if (paiement.getMethode() != MethodePaiement.CASH) {
            throw new BadRequestException("Seuls les règlements de type CASH (espèces) peuvent faire l'objet d'un encaissement guichet.");
        }

        paiement.setStatut(StatutPaiement.REUSSI);
        paiement.setDatePaiement(LocalDateTime.now());
        if (paiement.getReference() == null || paiement.getReference().isBlank()) {
            paiement.setReference("CASH-RECU-" + System.currentTimeMillis());
        }

        Paiement saved = paiementRepository.save(paiement);

        // Si la commande associée était encore EN_ATTENTE, on la confirme
        Commande commande = paiement.getCommande();
        if (commande != null && commande.getStatut() == StatutCommande.EN_ATTENTE) {
            commande.setStatut(StatutCommande.CONFIRMEE);
            commande.setDateMiseAJour(LocalDateTime.now());
            commandeRepository.save(commande);
            log.info("Commande #{} confirmée suite à l'encaissement effectif des espèces", commande.getNumero());
        }

        return mapToResponse(saved);
    }

    private PharmacopeePaiementResponse mapToResponse(Paiement p) {
        String clientNomComplet = "Client Inconnu";
        String clientTelephone = null;
        Long commandeId = null;
        String commandeNumero = null;

        if (p.getCommande() != null) {
            commandeId = p.getCommande().getId();
            commandeNumero = p.getCommande().getNumero();
            Utilisateur u = p.getCommande().getUtilisateur();
            if (u != null) {
                clientNomComplet = ((u.getPrenom() != null ? u.getPrenom() + " " : "")
                        + (u.getNom() != null ? u.getNom() : "")).trim();
                clientTelephone = u.getTelephone();
            }
        }

        String libelleMethode = switch (p.getMethode()) {
            case MOBILE_MONEY -> "Mobile Money (Orange Money / Wave)";
            case CASH -> "Espèces (Cash à la livraison ou au comptoir)";
            case CARTE_BANCAIRE -> "Carte bancaire (Visa / Mastercard)";
        };

        return PharmacopeePaiementResponse.builder()
                .id(p.getId())
                .montant(p.getMontant())
                .datePaiement(p.getDatePaiement())
                .methode(p.getMethode())
                .libelleMethode(libelleMethode)
                .statut(p.getStatut())
                .reference(p.getReference())
                .commandeId(commandeId)
                .commandeNumero(commandeNumero)
                .clientNomComplet(clientNomComplet)
                .clientTelephone(clientTelephone)
                .build();
    }
}
