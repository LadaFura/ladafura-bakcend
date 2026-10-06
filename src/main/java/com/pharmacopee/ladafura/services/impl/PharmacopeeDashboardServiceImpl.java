package com.pharmacopee.ladafura.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.PharmacopeeCommandeItemResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.dashboard.PharmacopeeDashboardResponse;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeDashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PharmacopeeDashboardServiceImpl implements IPharmacopeeDashboardService {

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final CommandeRepository commandeRepository;
    private final AvisRepository avisRepository;
    private final NotificationRepository notificationRepository;
    private final ModeRetraitRepository modeRetraitRepository;

    @Override
    public PharmacopeeDashboardResponse getDashboard() {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Génération du tableau de bord consolidé pour la pharmacopée ID {} ({})", pId, pharmacopee.getNom());

        // 1. Volet Référencement & Établissement
        PharmacopeeDashboardResponse.ReferencementSection referencementSection = buildReferencementSection(pharmacopee);

        // 2. Volet Produits, Stocks & Valeur Marchande
        PharmacopeeDashboardResponse.ProduitsStockSection inventaireSection = buildInventaireSection(pId);

        // 3. Volet Commandes & Ventes
        PharmacopeeDashboardResponse.CommandesSection commandesSection = buildCommandesSection(pId);

        // 4. Volet Avis Clients & Satisfaction
        PharmacopeeDashboardResponse.AvisSection avisSection = buildAvisSection(pId);

        // 5. Volet Notifications & Alertes
        PharmacopeeDashboardResponse.NotificationsSection notificationsSection = buildNotificationsSection(pId);

        return PharmacopeeDashboardResponse.builder()
                .referencement(referencementSection)
                .inventaire(inventaireSection)
                .commandes(commandesSection)
                .avis(avisSection)
                .notifications(notificationsSection)
                .build();
    }

    private PharmacopeeDashboardResponse.ReferencementSection buildReferencementSection(Pharmacopee p) {
        Localisation loc = p.getLocalisation();

        ModeRetrait liv = modeRetraitRepository.findByPharmacopeeIdAndType(p.getId(), TypeModeRetrait.LIVRAISON).orElse(null);
        ModeRetrait pick = modeRetraitRepository.findByPharmacopeeIdAndType(p.getId(), TypeModeRetrait.PICKUP).orElse(null);

        return PharmacopeeDashboardResponse.ReferencementSection.builder()
                .pharmacopeeId(p.getId())
                .nomPharmacopee(p.getNom())
                .telephone(p.getTelephone())
                .statut(p.getStatut())
                .estValidee(p.getStatut() == StatutPharmacopee.VALIDEE)
                .region(loc != null ? loc.getRegion() : null)
                .cercle(loc != null ? loc.getCercle() : null)
                .commune(loc != null ? loc.getCommune() : null)
                .localite(loc != null ? loc.getLocalite() : null)
                .latitude(loc != null ? loc.getLatitude() : null)
                .longitude(loc != null ? loc.getLongitude() : null)
                .livraisonActive(liv != null && Boolean.TRUE.equals(liv.getActif()))
                .fraisLivraison(liv != null ? liv.getFrais() : 0.0)
                .pickupActif(pick != null && Boolean.TRUE.equals(pick.getActif()))
                .build();
    }

    private PharmacopeeDashboardResponse.ProduitsStockSection buildInventaireSection(Long pId) {
        List<DisponibiliteProduit> dispoList = disponibiliteProduitRepository.findByPharmacopeeId(pId);

        long totalRef = dispoList.size();
        long refActives = dispoList.stream().filter(d -> Boolean.TRUE.equals(d.getDisponible())).count();
        long refDesactivees = totalRef - refActives;
        double tauxDispo = totalRef > 0 ? Math.round((refActives * 100.0 / totalRef) * 10.0) / 10.0 : 0.0;

        long refEnStock = dispoList.stream()
                .filter(d -> d.getQuantiteStock() != null && d.getQuantiteStock() > 0)
                .count();
        long refEnRupture = dispoList.stream()
                .filter(d -> d.getQuantiteStock() == null || d.getQuantiteStock() <= 0)
                .count();

        double valeurTotale = dispoList.stream().mapToDouble(d -> {
            double prix = (d.getPrix() != null)
                    ? d.getPrix()
                    : (d.getProduit() != null && d.getProduit().getPrix() != null ? d.getProduit().getPrix() : 0.0);
            int quantite = (d.getQuantiteStock() != null) ? d.getQuantiteStock() : 0;
            return prix * quantite;
        }).sum();

        return PharmacopeeDashboardResponse.ProduitsStockSection.builder()
                .totalReferences(totalRef)
                .referencesActives(refActives)
                .referencesDesactivees(refDesactivees)
                .tauxDisponibilite(tauxDispo)
                .referencesEnStock(refEnStock)
                .referencesEnRupture(refEnRupture)
                .valeurTotaleStock(valeurTotale)
                .build();
    }

    private PharmacopeeDashboardResponse.CommandesSection buildCommandesSection(Long pId) {
        long totalCmd = commandeRepository.countByPharmacopeeId(pId);
        long attente = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.EN_ATTENTE);
        long confirmees = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.CONFIRMEE);
        long preparees = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.PREPAREE);
        long enLivraison = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.EN_LIVRAISON);
        long dispoPickup = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.DISPONIBLE_PICKUP);
        long livrees = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.LIVREE);
        long retirees = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.RETIREE);
        long annulees = commandeRepository.countByPharmacopeeIdAndStatut(pId, StatutCommande.ANNULEE);

        Double ca = commandeRepository.sumMontantTotalByPharmacopeeIdAndStatuts(
                pId, List.of(StatutCommande.LIVREE, StatutCommande.RETIREE));

        // 5 dernières commandes
        Page<Commande> dernieresPage = commandeRepository.findByPharmacopeeId(
                pId, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dateCommande")));

        List<PharmacopeeCommandeItemResponse> dernieresCommandes = dernieresPage.getContent().stream()
                .map(this::mapToCommandeItemResponse)
                .toList();

        return PharmacopeeDashboardResponse.CommandesSection.builder()
                .totalCommandes(totalCmd)
                .commandesEnAttente(attente)
                .commandesConfirmees(confirmees)
                .commandesPreparees(preparees)
                .commandesEnCoursAcheminement(enLivraison + dispoPickup)
                .commandesTerminees(livrees + retirees)
                .commandesAnnulees(annulees)
                .chiffreAffairesTotal(ca != null ? ca : 0.0)
                .dernieresCommandes(dernieresCommandes)
                .build();
    }

    private PharmacopeeDashboardResponse.AvisSection buildAvisSection(Long pId) {
        long totalAvis = avisRepository.countByPharmacopeeIdAndStatut(pId, StatutAvis.PUBLIE);
        Double noteMoy = avisRepository.findAverageNoteByPharmacopeeIdAndStatut(pId, StatutAvis.PUBLIE);
        Double noteArrondie = noteMoy != null ? Math.round(noteMoy * 10.0) / 10.0 : 0.0;

        int nbProdEvalues = (int) avisRepository.countByPharmacopeeIdAndStatut(pId, StatutAvis.PUBLIE);

        long s5 = avisRepository.countByPharmacopeeIdAndStatutAndNote(pId, StatutAvis.PUBLIE, 5);
        long s4 = avisRepository.countByPharmacopeeIdAndStatutAndNote(pId, StatutAvis.PUBLIE, 4);
        long s3 = avisRepository.countByPharmacopeeIdAndStatutAndNote(pId, StatutAvis.PUBLIE, 3);
        long s2 = avisRepository.countByPharmacopeeIdAndStatutAndNote(pId, StatutAvis.PUBLIE, 2);
        long s1 = avisRepository.countByPharmacopeeIdAndStatutAndNote(pId, StatutAvis.PUBLIE, 1);

        return PharmacopeeDashboardResponse.AvisSection.builder()
                .totalAvis(totalAvis)
                .noteMoyenneGlobale(noteArrondie)
                .nombreProduitsEvalues(nbProdEvalues)
                .total5Etoiles(s5)
                .total4Etoiles(s4)
                .total3Etoiles(s3)
                .total2Etoiles(s2)
                .total1Etoile(s1)
                .build();
    }

    private PharmacopeeDashboardResponse.NotificationsSection buildNotificationsSection(Long pId) {
        long nonLues = notificationRepository.countByPharmacopeeIdAndLueFalse(pId);
        long nonLuesCmd = notificationRepository.countByPharmacopeeIdAndLueFalseAndType(pId, TypeNotification.COMMANDE_NOUVELLE)
                + notificationRepository.countByPharmacopeeIdAndLueFalseAndType(pId, TypeNotification.COMMANDE_STATUT);
        long nonLuesStock = notificationRepository.countByPharmacopeeIdAndLueFalseAndType(pId, TypeNotification.STOCK_ALERTE);

        return PharmacopeeDashboardResponse.NotificationsSection.builder()
                .totalNonLues(nonLues)
                .nonLuesCommandes(nonLuesCmd)
                .nonLuesAlertesStock(nonLuesStock)
                .build();
    }

    private PharmacopeeCommandeItemResponse mapToCommandeItemResponse(Commande c) {
        String clientNom = "Client Inconnu";
        String clientTel = null;
        if (c.getUtilisateur() != null) {
            clientNom = ((c.getUtilisateur().getPrenom() != null ? c.getUtilisateur().getPrenom() + " " : "")
                    + (c.getUtilisateur().getNom() != null ? c.getUtilisateur().getNom() : "")).trim();
            clientTel = c.getUtilisateur().getTelephone();
        }

        TypeModeRetrait typeRetrait = c.getModeRetrait() != null ? c.getModeRetrait().getType() : null;
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
                .clientNomComplet(clientNom)
                .clientTelephone(clientTel)
                .nombreArticles(nbArticles)
                .build();
    }
}
