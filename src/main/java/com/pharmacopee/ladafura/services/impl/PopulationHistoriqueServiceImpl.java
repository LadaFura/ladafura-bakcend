package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.Favori;
import com.pharmacopee.ladafura.Models.LigneCommande;
import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.historique.PopulationDepenseMensuelleDto;
import com.pharmacopee.ladafura.dto.population.historique.PopulationDepenseMethodeDto;
import com.pharmacopee.ladafura.dto.population.historique.PopulationDepensesSyntheseResponse;
import com.pharmacopee.ladafura.dto.population.historique.PopulationJournalActiviteItem;
import com.pharmacopee.ladafura.dto.population.historique.PopulationProduitAcheteItem;
import com.pharmacopee.ladafura.dto.population.historique.PopulationReleveActiviteResponse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.TypeEvenementHistorique;
import com.pharmacopee.ladafura.mappers.PopulationHistoriqueMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.FavoriRepository;
import com.pharmacopee.ladafura.repository.LigneCommandeRepository;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.repository.PaiementRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationHistoriqueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PopulationHistoriqueServiceImpl implements IPopulationHistoriqueService {

    private final CommandeRepository commandeRepository;
    private final PaiementRepository paiementRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final AvisRepository avisRepository;
    private final FavoriRepository favoriRepository;
    private final NotificationRepository notificationRepository;
    private final IPopulationAuthService populationAuthService;
    private final PopulationHistoriqueMapper mapper;

    @Override
    public Page<PopulationJournalActiviteItem> getJournalActivite(
            TypeEvenementHistorique type,
            LocalDateTime dateDebut,
            LocalDateTime dateFin,
            Pageable pageable) {

        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        Long userId = user.getId();
        log.info("Chargement du journal d'activité pour l'utilisateur ID: {} (type={}, dateDebut={}, dateFin={})",
                userId, type, dateDebut, dateFin);

        List<PopulationJournalActiviteItem> items = new ArrayList<>();

        // 1. Commandes
        if (type == null || type == TypeEvenementHistorique.COMMANDE) {
            List<Commande> commandes = commandeRepository.findByUtilisateurId(userId);
            for (Commande c : commandes) {
                if (c.getDateCommande() != null) {
                    items.add(mapper.mapCommande(c));
                }
            }
        }

        // 2. Paiements
        if (type == null || type == TypeEvenementHistorique.PAIEMENT) {
            List<Paiement> paiements = paiementRepository.findByCommandeUtilisateurId(userId);
            for (Paiement p : paiements) {
                if (p.getDatePaiement() != null) {
                    items.add(mapper.mapPaiement(p));
                }
            }
        }

        // 3. Avis
        if (type == null || type == TypeEvenementHistorique.AVIS) {
            List<Avis> avisList = avisRepository.findByUtilisateurId(userId);
            for (Avis a : avisList) {
                if (a.getDateAvis() != null) {
                    items.add(mapper.mapAvis(a));
                }
            }
        }

        // 4. Favoris
        if (type == null || type == TypeEvenementHistorique.FAVORI) {
            List<Favori> favoris = favoriRepository.findByUtilisateurId(userId);
            for (Favori f : favoris) {
                if (f.getDateAjout() != null) {
                    items.add(mapper.mapFavori(f));
                }
            }
        }

        // 5. Notifications
        if (type == null || type == TypeEvenementHistorique.NOTIFICATION) {
            Page<Notification> notifsPage = notificationRepository.findByUtilisateurId(userId, Pageable.unpaged());
            for (Notification n : notifsPage.getContent()) {
                if (n.getDateNotification() != null) {
                    items.add(mapper.mapNotification(n));
                }
            }
        }

        // Filtrage par période
        List<PopulationJournalActiviteItem> filtered = items.stream()
                .filter(i -> dateDebut == null || !i.getDateEvenement().isBefore(dateDebut))
                .filter(i -> dateFin == null || !i.getDateEvenement().isAfter(dateFin))
                .sorted(Comparator.comparing(PopulationJournalActiviteItem::getDateEvenement, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        // Pagination
        int total = filtered.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);

        List<PopulationJournalActiviteItem> pagedContent = (start <= total)
                ? filtered.subList(start, end)
                : Collections.emptyList();

        return new PageImpl<>(pagedContent, pageable, total);
    }

    @Override
    public PopulationDepensesSyntheseResponse getSyntheseDepenses() {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        Long userId = user.getId();
        log.info("Calcul de la synthèse des dépenses pour l'utilisateur ID: {}", userId);

        List<Paiement> paiementsReussis = paiementRepository.findByCommandeUtilisateurIdAndStatut(userId, StatutPaiement.REUSSI);

        double totalDepense = paiementsReussis.stream()
                .mapToDouble(p -> p.getMontant() != null ? p.getMontant() : 0.0)
                .sum();
        long totalTransactions = paiementsReussis.size();
        double panierMoyen = totalTransactions > 0 ? Math.round((totalDepense / totalTransactions) * 100.0) / 100.0 : 0.0;

        // Dépenses par mois
        Map<String, List<Paiement>> parMoisMap = paiementsReussis.stream()
                .filter(p -> p.getDatePaiement() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getDatePaiement().getYear() + "-" + String.format("%02d", p.getDatePaiement().getMonthValue()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<PopulationDepenseMensuelleDto> depensesMois = new ArrayList<>();
        parMoisMap.forEach((cle, list) -> {
            int annee = Integer.parseInt(cle.split("-")[0]);
            int mois = Integer.parseInt(cle.split("-")[1]);
            String nomMois = java.time.Month.of(mois).getDisplayName(TextStyle.FULL, Locale.FRENCH);
            String libelle = nomMois.substring(0, 1).toUpperCase() + nomMois.substring(1) + " " + annee;
            double montantMois = list.stream().mapToDouble(p -> p.getMontant() != null ? p.getMontant() : 0.0).sum();

            depensesMois.add(PopulationDepenseMensuelleDto.builder()
                    .annee(annee)
                    .mois(mois)
                    .libelleMois(libelle)
                    .montant(montantMois)
                    .nombreTransactions(list.size())
                    .build());
        });

        // Dépenses par méthode
        Map<MethodePaiement, List<Paiement>> parMethodeMap = paiementsReussis.stream()
                .filter(p -> p.getMethode() != null)
                .collect(Collectors.groupingBy(Paiement::getMethode));

        List<PopulationDepenseMethodeDto> depensesMethodes = new ArrayList<>();
        parMethodeMap.forEach((methode, list) -> {
            double montantMethode = list.stream().mapToDouble(p -> p.getMontant() != null ? p.getMontant() : 0.0).sum();
            double pourcentage = totalDepense > 0 ? Math.round((montantMethode / totalDepense) * 10000.0) / 100.0 : 0.0;

            String nomMethode = switch (methode) {
                case MOBILE_MONEY -> "Mobile Money (Orange / Moov)";
                case CASH -> "Paiement en espèces (Cash)";
                case CARTE_BANCAIRE -> "Carte bancaire (Visa / Mastercard)";
            };

            depensesMethodes.add(PopulationDepenseMethodeDto.builder()
                    .methode(methode)
                    .nomMethode(nomMethode)
                    .montant(montantMethode)
                    .nombrePaiements(list.size())
                    .pourcentage(pourcentage)
                    .build());
        });

        return mapper.toSyntheseDepenses(totalDepense, totalTransactions, panierMoyen, depensesMois, depensesMethodes);
    }

    @Override
    public Page<PopulationProduitAcheteItem> getHistoriqueProduitsAchetes(Pageable pageable) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        Long userId = user.getId();
        log.info("Chargement de l'historique des produits achetés pour l'utilisateur ID: {}", userId);

        List<LigneCommande> lignes = ligneCommandeRepository.findPurchasedLinesByUtilisateurId(userId);

        // Regrouper par Produit ID
        Map<Long, List<LigneCommande>> groupedByProduit = lignes.stream()
                .filter(lc -> lc.getProduit() != null)
                .collect(Collectors.groupingBy(lc -> lc.getProduit().getId(), LinkedHashMap::new, Collectors.toList()));

        List<PopulationProduitAcheteItem> items = new ArrayList<>();

        for (Map.Entry<Long, List<LigneCommande>> entry : groupedByProduit.entrySet()) {
            Long produitId = entry.getKey();
            List<LigneCommande> lcList = entry.getValue();
            LigneCommande plusRecente = lcList.get(0); // déjà ordonnées par dateCommande DESC
            Produit produit = plusRecente.getProduit();
            Commande derniereCommande = plusRecente.getCommande();

            int quantiteTotale = lcList.stream().mapToInt(lc -> lc.getQuantite() != null ? lc.getQuantite() : 0).sum();
            double montantTotal = lcList.stream().mapToDouble(lc -> lc.getSousTotal() != null ? lc.getSousTotal() : 0.0).sum();

            Long pharmacopeeId = derniereCommande != null && derniereCommande.getPharmacopee() != null ? derniereCommande.getPharmacopee().getId() : null;
            String pharmacopeeNom = derniereCommande != null && derniereCommande.getPharmacopee() != null ? derniereCommande.getPharmacopee().getNom() : null;
            Optional<Avis> avisOpt = pharmacopeeId != null ? avisRepository.findByUtilisateurIdAndPharmacopeeId(userId, pharmacopeeId) : Optional.empty();

            items.add(PopulationProduitAcheteItem.builder()
                    .produitId(produitId)
                    .nomProduit(produit.getNom())
                    .forme(produit.getForme())
                    .photoUrl(produit.getPhotoUrl())
                    .dernierPrixUnitaire(plusRecente.getPrixUnitaire())
                    .quantiteTotaleAchetee(quantiteTotale)
                    .montantTotalDepense(montantTotal)
                    .dateDernierAchat(derniereCommande != null ? derniereCommande.getDateCommande() : null)
                    .dernierNumeroCommande(derniereCommande != null ? derniereCommande.getNumero() : null)
                    .dernierePharmacopeeId(pharmacopeeId)
                    .dernierePharmacopeeNom(pharmacopeeNom)
                    .dejaEvalue(avisOpt.isPresent())
                    .avisId(avisOpt.map(Avis::getId).orElse(null))
                    .build());
        }

        // Pagination
        int total = items.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);

        List<PopulationProduitAcheteItem> pagedContent = (start <= total)
                ? items.subList(start, end)
                : Collections.emptyList();

        return new PageImpl<>(pagedContent, pageable, total);
    }

    @Override
    public PopulationReleveActiviteResponse getReleveActivite() {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        Long userId = user.getId();
        log.info("Génération du relevé d'activité consolidé pour l'utilisateur ID: {}", userId);

        List<Commande> commandes = commandeRepository.findByUtilisateurId(userId);
        long totalCommandes = commandes.size();
        long totalCommandesLivrees = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.LIVREE || c.getStatut() == StatutCommande.RETIREE)
                .count();

        List<Paiement> paiementsReussis = paiementRepository.findByCommandeUtilisateurIdAndStatut(userId, StatutPaiement.REUSSI);
        double totalDepense = paiementsReussis.stream()
                .mapToDouble(p -> p.getMontant() != null ? p.getMontant() : 0.0)
                .sum();

        long totalAvis = avisRepository.findByUtilisateurId(userId).size();
        long totalFavoris = favoriRepository.countByUtilisateurId(userId);

        // 10 dernières activités du journal
        Page<PopulationJournalActiviteItem> recentPage = getJournalActivite(null, null, null, PageRequest.of(0, 10));

        return mapper.toReleveActivite(
                user,
                totalCommandes,
                totalCommandesLivrees,
                totalDepense,
                totalAvis,
                totalFavoris,
                recentPage.getContent()
        );
    }
}
