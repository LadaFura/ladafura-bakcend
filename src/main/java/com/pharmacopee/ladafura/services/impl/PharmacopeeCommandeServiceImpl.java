package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.LigneCommande;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.ClientInfoResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.LigneCommandeResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.PaiementInfoResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.PharmacopeeCommandeDetailResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.PharmacopeeCommandeItemResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.PharmacopeeCommandeSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.UpdateStatutCommandeRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.retrait.ModeRetraitResponse;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeCommandeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacopeeCommandeServiceImpl implements IPharmacopeeCommandeService {

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final CommandeRepository commandeRepository;
    private final com.pharmacopee.ladafura.services.interfaces.IPharmacopeeNotificationService pharmacopeeNotificationService;
    private final com.pharmacopee.ladafura.services.interfaces.IPopulationNotificationService populationNotificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<PharmacopeeCommandeItemResponse> getCommandes(StatutCommande statut, Pageable pageable) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        log.info("Consultation paginée des commandes pour la pharmacopée ID {} (statut={})",
                pharmacopee.getId(), statut);

        Page<Commande> page;
        if (statut != null) {
            page = commandeRepository.findByPharmacopeeIdAndStatut(pharmacopee.getId(), statut, pageable);
        } else {
            page = commandeRepository.findByPharmacopeeId(pharmacopee.getId(), pageable);
        }

        return page.map(this::mapToItemResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacopeeCommandeSummaryResponse getSummary() {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Calcul du résumé des commandes pour la pharmacopée ID {}", pId);

        long total = commandeRepository.countByPharmacopeeId(pId);
        long enAttente = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.EN_ATTENTE);
        long confirmees = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.CONFIRMEE);
        long preparees = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.PREPAREE);
        long enLivraison = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.EN_LIVRAISON);
        long dispoPickup = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.DISPONIBLE_PICKUP);
        long livree = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.LIVREE);
        long retiree = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.RETIREE);
        long annulees = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.ANNULEE);

        Double ca = commandeRepository.sumMontantTotalByPharmacopeeIdAndStatuts(
                pId, List.of(StatutCommande.LIVREE, StatutCommande.RETIREE));

        return PharmacopeeCommandeSummaryResponse.builder()
                .totalCommandes(total)
                .totalEnAttente(enAttente)
                .totalConfirmees(confirmees)
                .totalPreparees(preparees)
                .totalEnCoursAcheminement(enLivraison + dispoPickup)
                .totalTerminees(livree + retiree)
                .totalAnnulees(annulees)
                .chiffreAffairesTermine(ca != null ? ca : 0.0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacopeeCommandeDetailResponse getCommandeDetail(Long id) {
        Commande commande = loadCommandeForCurrentPharmacopee(id);
        log.info("Consultation du détail de la commande #{} (ID {})", commande.getNumero(), id);
        return mapToDetailResponse(commande);
    }

    @Override
    public PharmacopeeCommandeDetailResponse updateStatut(Long id, UpdateStatutCommandeRequest request) {
        Commande commande = loadCommandeForCurrentPharmacopee(id);
        StatutCommande statutActuel = commande.getStatut();
        StatutCommande cible = request.getNouveauStatut();

        log.info("Tentative de transition de statut pour la commande ID {} : {} -> {}", id, statutActuel, cible);

        List<StatutCommande> autorises = determineProchainsStatutsAutorises(commande);
        if (!autorises.contains(cible)) {
            log.warn("Transition refusée pour la commande ID {} : transition de {} vers {} non permise. Autorisés : {}",
                    id, statutActuel, cible, autorises);
            throw new BadRequestException("Transition de statut non autorisée : impossible de passer de "
                    + statutActuel + " à " + cible + ". Statuts autorisés : " + autorises);
        }

        commande.setStatut(cible);
        commande.setDateMiseAJour(LocalDateTime.now());
        Commande saved = commandeRepository.save(commande);
        log.info("Statut mis à jour avec succès pour la commande ID {} : {}", id, cible);

        notifierEvolutionStatutCommande(saved, cible, request.getMotif());

        return mapToDetailResponse(saved);
    }

    private void notifierEvolutionStatutCommande(Commande cmd, StatutCommande nouveauStatut, String motif) {
        if (cmd == null) return;
        String refId = String.valueOf(cmd.getId());
        String nomPharma = cmd.getPharmacopee() != null ? cmd.getPharmacopee().getNom() : "la pharmacopée";
        String titreClient;
        String msgClient;
        com.pharmacopee.ladafura.enums.NiveauNotification niveau = com.pharmacopee.ladafura.enums.NiveauNotification.INFO;

        switch (nouveauStatut) {
            case CONFIRMEE -> {
                titreClient = "Commande " + cmd.getNumero() + " confirmée";
                msgClient = "Votre commande " + cmd.getNumero() + " a été confirmée par « " + nomPharma + " ».";
                niveau = com.pharmacopee.ladafura.enums.NiveauNotification.SUCCES;
            }
            case PREPAREE -> {
                titreClient = "Commande " + cmd.getNumero() + " préparée";
                msgClient = "Les produits de votre commande " + cmd.getNumero() + " sont prêts chez « " + nomPharma + " ».";
                niveau = com.pharmacopee.ladafura.enums.NiveauNotification.INFO;
            }
            case EN_LIVRAISON -> {
                titreClient = "Commande " + cmd.getNumero() + " en cours de livraison";
                msgClient = "Votre commande " + cmd.getNumero() + " est en cours d'acheminement vers votre adresse.";
                niveau = com.pharmacopee.ladafura.enums.NiveauNotification.INFO;
            }
            case DISPONIBLE_PICKUP -> {
                titreClient = "Commande " + cmd.getNumero() + " prête pour retrait";
                msgClient = "Votre commande " + cmd.getNumero() + " est disponible au comptoir de « " + nomPharma + " ».";
                niveau = com.pharmacopee.ladafura.enums.NiveauNotification.SUCCES;
            }
            case LIVREE -> {
                titreClient = "Commande " + cmd.getNumero() + " livrée";
                msgClient = "Votre commande " + cmd.getNumero() + " a été livrée avec succès. Merci de votre confiance !";
                niveau = com.pharmacopee.ladafura.enums.NiveauNotification.SUCCES;
            }
            case RETIREE -> {
                titreClient = "Commande " + cmd.getNumero() + " retirée";
                msgClient = "Vous avez retiré votre commande " + cmd.getNumero() + " auprès de « " + nomPharma + " ». Merci !";
                niveau = com.pharmacopee.ladafura.enums.NiveauNotification.SUCCES;
            }
            case ANNULEE -> {
                titreClient = "Commande " + cmd.getNumero() + " annulée";
                String detailMotif = (motif != null && !motif.isBlank()) ? " Motif : " + motif : "";
                msgClient = "Votre commande " + cmd.getNumero() + " auprès de « " + nomPharma + " » a été annulée." + detailMotif;
                niveau = com.pharmacopee.ladafura.enums.NiveauNotification.ATTENTION;
            }
            default -> {
                titreClient = "Mise à jour de votre commande " + cmd.getNumero();
                msgClient = "Le statut de votre commande " + cmd.getNumero() + " est passé à " + nouveauStatut + ".";
            }
        }

        if (cmd.getUtilisateur() != null && populationNotificationService != null) {
            populationNotificationService.envoyerNotification(
                    cmd.getUtilisateur(),
                    titreClient,
                    msgClient,
                    com.pharmacopee.ladafura.enums.TypeNotification.COMMANDE_STATUT,
                    niveau,
                    refId,
                    "/citizen/commandes/" + cmd.getId()
            );
        }

        if (cmd.getPharmacopee() != null && pharmacopeeNotificationService != null) {
            pharmacopeeNotificationService.notifier(
                    cmd.getPharmacopee(),
                    com.pharmacopee.ladafura.enums.TypeNotification.COMMANDE_STATUT,
                    niveau,
                    titreClient,
                    "La commande " + cmd.getNumero() + " est passée au statut " + nouveauStatut + ".",
                    refId,
                    "/pharmacopee/commandes/" + cmd.getId()
            );
        }
    }

    @Override
    public PharmacopeeCommandeDetailResponse confirmerCommande(Long id) {
        return updateStatut(id, UpdateStatutCommandeRequest.builder()
                .nouveauStatut(StatutCommande.CONFIRMEE)
                .build());
    }

    @Override
    public PharmacopeeCommandeDetailResponse preparerCommande(Long id) {
        return updateStatut(id, UpdateStatutCommandeRequest.builder()
                .nouveauStatut(StatutCommande.PREPAREE)
                .build());
    }

    @Override
    public PharmacopeeCommandeDetailResponse acheminerCommande(Long id) {
        Commande commande = loadCommandeForCurrentPharmacopee(id);
        TypeModeRetrait typeRetrait = resolveTypeRetrait(commande);

        StatutCommande cible;
        if (typeRetrait == TypeModeRetrait.LIVRAISON) {
            cible = StatutCommande.EN_LIVRAISON;
        } else if (typeRetrait == TypeModeRetrait.PICKUP) {
            cible = StatutCommande.DISPONIBLE_PICKUP;
        } else {
            throw new BadRequestException("Impossible d'acheminer la commande ID " + id + " : aucun mode de retrait (Livraison ou Pickup) n'est associé.");
        }

        return updateStatut(id, UpdateStatutCommandeRequest.builder()
                .nouveauStatut(cible)
                .build());
    }

    @Override
    public PharmacopeeCommandeDetailResponse finaliserCommande(Long id) {
        Commande commande = loadCommandeForCurrentPharmacopee(id);
        StatutCommande statutActuel = commande.getStatut();

        StatutCommande cible;
        if (statutActuel == StatutCommande.EN_LIVRAISON) {
            cible = StatutCommande.LIVREE;
        } else if (statutActuel == StatutCommande.DISPONIBLE_PICKUP) {
            cible = StatutCommande.RETIREE;
        } else {
            throw new BadRequestException("Seules les commandes en cours de livraison ou prêtes en retrait peuvent être finalisées. Statut actuel : " + statutActuel);
        }

        return updateStatut(id, UpdateStatutCommandeRequest.builder()
                .nouveauStatut(cible)
                .build());
    }

    @Override
    public PharmacopeeCommandeDetailResponse annulerCommande(Long id, String motif) {
        log.info("Annulation de la commande ID {} avec motif : {}", id, motif);
        return updateStatut(id, UpdateStatutCommandeRequest.builder()
                .nouveauStatut(StatutCommande.ANNULEE)
                .motif(motif)
                .build());
    }

    /**
     * Charge une commande en garantissant l'étanchéité stricte :
     * Une pharmacopée ne peut JAMAIS charger une commande appartenant à une autre pharmacopée.
     */
    private Commande loadCommandeForCurrentPharmacopee(Long commandeId) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();

        return commandeRepository.findByIdAndPharmacopeeId(commandeId, pharmacopee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec l'ID " + commandeId
                        + " pour votre établissement de pharmacopée."));
    }

    /**
     * Détermine dynamiquement la liste stricte des statuts accessibles depuis l'état actuel de la commande.
     */
    private List<StatutCommande> determineProchainsStatutsAutorises(Commande commande) {
        StatutCommande actuel = commande.getStatut();
        if (actuel == null) {
            return Collections.emptyList();
        }

        TypeModeRetrait type = resolveTypeRetrait(commande);

        switch (actuel) {
            case EN_ATTENTE:
                return List.of(StatutCommande.CONFIRMEE, StatutCommande.ANNULEE);

            case CONFIRMEE:
                return List.of(StatutCommande.PREPAREE, StatutCommande.ANNULEE);

            case PREPAREE:
                List<StatutCommande> autorises = new ArrayList<>();
                if (type == TypeModeRetrait.LIVRAISON) {
                    autorises.add(StatutCommande.EN_LIVRAISON);
                } else if (type == TypeModeRetrait.PICKUP) {
                    autorises.add(StatutCommande.DISPONIBLE_PICKUP);
                } else {
                    autorises.add(StatutCommande.EN_LIVRAISON);
                    autorises.add(StatutCommande.DISPONIBLE_PICKUP);
                }
                autorises.add(StatutCommande.ANNULEE);
                return autorises;

            case EN_LIVRAISON:
                return List.of(StatutCommande.LIVREE);

            case DISPONIBLE_PICKUP:
                return List.of(StatutCommande.RETIREE);

            case LIVREE:
            case RETIREE:
            case ANNULEE:
            default:
                return Collections.emptyList();
        }
    }

    private TypeModeRetrait resolveTypeRetrait(Commande commande) {
        if (commande.getModeRetrait() != null) {
            return commande.getModeRetrait().getType();
        }
        return null;
    }

    private PharmacopeeCommandeItemResponse mapToItemResponse(Commande c) {
        String clientNomComplet = "Client Inconnu";
        String clientTelephone = null;
        if (c.getUtilisateur() != null) {
            clientNomComplet = ((c.getUtilisateur().getPrenom() != null ? c.getUtilisateur().getPrenom() + " " : "")
                    + (c.getUtilisateur().getNom() != null ? c.getUtilisateur().getNom() : "")).trim();
            clientTelephone = c.getUtilisateur().getTelephone();
        }

        TypeModeRetrait typeRetrait = resolveTypeRetrait(c);

        int nbArticles = c.getLignes() != null ? c.getLignes().size() : 0;

        return PharmacopeeCommandeItemResponse.builder()
                .id(c.getId())
                .numero(c.getNumero())
                .dateCommande(c.getDateCommande())
                .statut(c.getStatut())
                .totalProduit(c.getTotalProduit())
                .montantLivraison(c.getMontantLivraison())
                .montantTotal(c.getMontantTotal())
                .typeRetrait(typeRetrait)
                .clientNomComplet(clientNomComplet)
                .clientTelephone(clientTelephone)
                .nombreArticles(nbArticles)
                .build();
    }

    private PharmacopeeCommandeDetailResponse mapToDetailResponse(Commande c) {
        // Client
        ClientInfoResponse clientResp = null;
        if (c.getUtilisateur() != null) {
            Utilisateur u = c.getUtilisateur();
            String complet = ((u.getPrenom() != null ? u.getPrenom() + " " : "")
                    + (u.getNom() != null ? u.getNom() : "")).trim();
            clientResp = ClientInfoResponse.builder()
                    .id(u.getId())
                    .nom(u.getNom())
                    .prenom(u.getPrenom())
                    .nomComplet(complet)
                    .email(u.getEmail())
                    .telephone(u.getTelephone())
                    .build();
        }

        // Mode Retrait
        ModeRetraitResponse retraitResp = null;
        if (c.getModeRetrait() != null) {
            ModeRetrait m = c.getModeRetrait();
            String libelle = m.getType() == TypeModeRetrait.LIVRAISON
                    ? "Livraison à domicile"
                    : "Retrait en pharmacopée (Pickup)";
            retraitResp = ModeRetraitResponse.builder()
                    .id(m.getId())
                    .type(m.getType())
                    .libelle(libelle)
                    .actif(Boolean.TRUE.equals(m.getActif()))
                    .frais(m.getFrais())
                    .build();
        }

        // Lignes de commande
        List<LigneCommandeResponse> lignesResp = new ArrayList<>();
        if (c.getLignes() != null) {
            for (LigneCommande lc : c.getLignes()) {
                Produit p = lc.getProduit();
                lignesResp.add(LigneCommandeResponse.builder()
                        .id(lc.getId())
                        .produitId(p != null ? p.getId() : null)
                        .nomProduit(p != null ? p.getNom() : "Produit non référencé")
                        .forme(p != null ? p.getForme() : null)
                        .quantite(lc.getQuantite())
                        .prixUnitaire(lc.getPrixUnitaire())
                        .sousTotal(lc.getSousTotal())
                        .build());
            }
        }

        // Paiement
        PaiementInfoResponse paiementResp = null;
        if (c.getPaiement() != null) {
            Paiement p = c.getPaiement();
            paiementResp = PaiementInfoResponse.builder()
                    .id(p.getId())
                    .montant(p.getMontant())
                    .methode(p.getMethode())
                    .statut(p.getStatut())
                    .reference(p.getReference())
                    .datePaiement(p.getDatePaiement())
                    .build();
        }

        return PharmacopeeCommandeDetailResponse.builder()
                .id(c.getId())
                .numero(c.getNumero())
                .dateCommande(c.getDateCommande())
                .dateMiseAJour(c.getDateMiseAJour())
                .statut(c.getStatut())
                .totalProduit(c.getTotalProduit())
                .montantLivraison(c.getMontantLivraison())
                .montantTotal(c.getMontantTotal())
                .adresseLivraison(c.getAdresseLivraison())
                .client(clientResp)
                .modeRetrait(retraitResp)
                .lignes(lignesResp)
                .paiement(paiementResp)
                .prochainsStatutsAutorises(determineProchainsStatutsAutorises(c))
                .build();
    }
}
