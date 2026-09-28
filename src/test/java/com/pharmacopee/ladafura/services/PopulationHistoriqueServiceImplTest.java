package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.Favori;
import com.pharmacopee.ladafura.Models.LigneCommande;
import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.historique.PopulationDepensesSyntheseResponse;
import com.pharmacopee.ladafura.dto.population.historique.PopulationJournalActiviteItem;
import com.pharmacopee.ladafura.dto.population.historique.PopulationProduitAcheteItem;
import com.pharmacopee.ladafura.dto.population.historique.PopulationReleveActiviteResponse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.enums.TypeEvenementHistorique;
import com.pharmacopee.ladafura.mappers.PopulationHistoriqueMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.FavoriRepository;
import com.pharmacopee.ladafura.repository.LigneCommandeRepository;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.repository.PaiementRepository;
import com.pharmacopee.ladafura.services.impl.PopulationHistoriqueServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

@ExtendWith(MockitoExtension.class)
class PopulationHistoriqueServiceImplTest {

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private LigneCommandeRepository ligneCommandeRepository;

    @Mock
    private AvisRepository avisRepository;

    @Mock
    private FavoriRepository favoriRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private IPopulationAuthService populationAuthService;

    @Spy
    private PopulationHistoriqueMapper mapper = new PopulationHistoriqueMapper();

    @InjectMocks
    private PopulationHistoriqueServiceImpl historiqueService;

    private Utilisateur currentUser;
    private Pharmacopee pharmacopee;
    private Produit produitArtémisia;
    private Commande commande;
    private Paiement paiement;
    private Avis avis;
    private Favori favori;
    private Notification notification;

    @BeforeEach
    void setUp() {
        currentUser = new Utilisateur();
        currentUser.setId(10L);
        currentUser.setEmail("patient@ladafura.ml");
        currentUser.setPrenom("Ousmane");
        currentUser.setNom("Diarra");
        currentUser.setRole(Role.POPULATION);
        currentUser.setStatut(StatutUtilisateur.ACTIF);

        pharmacopee = Pharmacopee.builder()
                .id(1L)
                .nom("Herboristerie Bandiagara")
                .build();

        produitArtémisia = Produit.builder()
                .id(101L)
                .nom("Tisane Artémisia Annua")
                .forme("Infusion sachet")
                .prix(2500.0)
                .build();

        commande = Commande.builder()
                .id(501L)
                .numero("CMD-2026-0001")
                .utilisateur(currentUser)
                .pharmacopee(pharmacopee)
                .statut(StatutCommande.LIVREE)
                .montantTotal(5000.0)
                .dateCommande(LocalDateTime.of(2026, 9, 20, 10, 0))
                .build();

        paiement = Paiement.builder()
                .id(601L)
                .commande(commande)
                .reference("PAY-OM-88392")
                .montant(5000.0)
                .methode(MethodePaiement.MOBILE_MONEY)
                .statut(StatutPaiement.REUSSI)
                .datePaiement(LocalDateTime.of(2026, 9, 20, 10, 5))
                .build();

        avis = Avis.builder()
                .id(701L)
                .utilisateur(currentUser)
                .produit(produitArtémisia)
                .note(5)
                .commentaire("Très efficace contre les accès palustres.")
                .statut(StatutAvis.PUBLIE)
                .dateAvis(LocalDateTime.of(2026, 9, 22, 14, 30))
                .build();

        favori = Favori.builder()
                .id(801L)
                .utilisateur(currentUser)
                .produit(produitArtémisia)
                .dateAjout(LocalDateTime.of(2026, 9, 18, 9, 0))
                .build();

        notification = Notification.builder()
                .id(901L)
                .utilisateur(currentUser)
                .titre("Commande livrée")
                .message("Votre colis a été remis au destinataire.")
                .lue(true)
                .referenceId("501")
                .lien("/api/v1/population/commandes/501")
                .dateNotification(LocalDateTime.of(2026, 9, 21, 16, 0))
                .build();
    }

    @Test
    @DisplayName("Journal d'activité - Tous les événements consolidés et triés par date décroissante")
    void getJournalActivite_allEvents_returnsConsolidatedSortedPage() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByUtilisateurId(10L)).thenReturn(List.of(commande));
        when(paiementRepository.findByCommandeUtilisateurId(10L)).thenReturn(List.of(paiement));
        when(avisRepository.findByUtilisateurId(10L)).thenReturn(List.of(avis));
        when(favoriRepository.findByUtilisateurId(10L)).thenReturn(List.of(favori));
        when(notificationRepository.findByUtilisateurId(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));

        Page<PopulationJournalActiviteItem> result =
                historiqueService.getJournalActivite(null, null, null, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(5);
        List<PopulationJournalActiviteItem> items = result.getContent();

        // Vérifier l'ordre chronologique inverse : avis (22/09) > notif (21/09) > paiement (20/09 10:05) > commande (20/09 10:00) > favori (18/09)
        assertThat(items.get(0).getType()).isEqualTo(TypeEvenementHistorique.AVIS);
        assertThat(items.get(1).getType()).isEqualTo(TypeEvenementHistorique.NOTIFICATION);
        assertThat(items.get(2).getType()).isEqualTo(TypeEvenementHistorique.PAIEMENT);
        assertThat(items.get(3).getType()).isEqualTo(TypeEvenementHistorique.COMMANDE);
        assertThat(items.get(4).getType()).isEqualTo(TypeEvenementHistorique.FAVORI);
    }

    @Test
    @DisplayName("Journal d'activité - Filtrer par type d'événement COMMANDE uniquement")
    void getJournalActivite_filterByType_returnsOnlyCommande() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByUtilisateurId(10L)).thenReturn(List.of(commande));

        Page<PopulationJournalActiviteItem> result =
                historiqueService.getJournalActivite(TypeEvenementHistorique.COMMANDE, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo(TypeEvenementHistorique.COMMANDE);
        assertThat(result.getContent().get(0).getReference()).isEqualTo("CMD-2026-0001");
    }

    @Test
    @DisplayName("Journal d'activité - Filtrer par plage de dates")
    void getJournalActivite_filterByDateRange() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByUtilisateurId(10L)).thenReturn(List.of(commande));
        when(paiementRepository.findByCommandeUtilisateurId(10L)).thenReturn(List.of(paiement));
        when(avisRepository.findByUtilisateurId(10L)).thenReturn(List.of(avis));
        when(favoriRepository.findByUtilisateurId(10L)).thenReturn(List.of(favori));
        when(notificationRepository.findByUtilisateurId(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));

        // Plage du 20/09/2026 au 21/09/2026 inclus -> doit garder commande, paiement et notif, exclure avis (22/09) et favori (18/09)
        LocalDateTime debut = LocalDateTime.of(2026, 9, 20, 0, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 9, 21, 23, 59);

        Page<PopulationJournalActiviteItem> result =
                historiqueService.getJournalActivite(null, debut, fin, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("Synthèse des dépenses - Calcul des totaux, moyennes, mois et méthodes")
    void getSyntheseDepenses_success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);

        Paiement p1 = Paiement.builder()
                .id(1L)
                .montant(10000.0)
                .methode(MethodePaiement.MOBILE_MONEY)
                .statut(StatutPaiement.REUSSI)
                .datePaiement(LocalDateTime.of(2026, 9, 10, 10, 0))
                .build();

        Paiement p2 = Paiement.builder()
                .id(2L)
                .montant(5000.0)
                .methode(MethodePaiement.CASH)
                .statut(StatutPaiement.REUSSI)
                .datePaiement(LocalDateTime.of(2026, 9, 15, 12, 0))
                .build();

        when(paiementRepository.findByCommandeUtilisateurIdAndStatut(10L, StatutPaiement.REUSSI))
                .thenReturn(List.of(p1, p2));

        PopulationDepensesSyntheseResponse synthese = historiqueService.getSyntheseDepenses();

        assertThat(synthese).isNotNull();
        assertThat(synthese.getMontantTotalDepense()).isEqualTo(15000.0);
        assertThat(synthese.getNombreTotalTransactions()).isEqualTo(2);
        assertThat(synthese.getPanierMoyen()).isEqualTo(7500.0);

        assertThat(synthese.getDepensesParMois()).hasSize(1);
        assertThat(synthese.getDepensesParMois().get(0).getMontant()).isEqualTo(15000.0);
        assertThat(synthese.getDepensesParMois().get(0).getNombreTransactions()).isEqualTo(2);

        assertThat(synthese.getDepensesParMethode()).hasSize(2);
    }

    @Test
    @DisplayName("Synthèse des dépenses - Aucune transaction réussie")
    void getSyntheseDepenses_noPayments_returnsZero() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(paiementRepository.findByCommandeUtilisateurIdAndStatut(10L, StatutPaiement.REUSSI))
                .thenReturn(Collections.emptyList());

        PopulationDepensesSyntheseResponse synthese = historiqueService.getSyntheseDepenses();

        assertThat(synthese.getMontantTotalDepense()).isEqualTo(0.0);
        assertThat(synthese.getNombreTotalTransactions()).isEqualTo(0);
        assertThat(synthese.getPanierMoyen()).isEqualTo(0.0);
        assertThat(synthese.getDepensesParMois()).isEmpty();
        assertThat(synthese.getDepensesParMethode()).isEmpty();
    }

    @Test
    @DisplayName("Historique des produits achetés - Regroupement par produit avec cumul")
    void getHistoriqueProduitsAchetes_success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);

        LigneCommande lc1 = LigneCommande.builder()
                .id(1L)
                .commande(commande)
                .produit(produitArtémisia)
                .quantite(2)
                .prixUnitaire(2500.0)
                .sousTotal(5000.0)
                .build();

        LigneCommande lc2 = LigneCommande.builder()
                .id(2L)
                .commande(commande)
                .produit(produitArtémisia)
                .quantite(1)
                .prixUnitaire(2500.0)
                .sousTotal(2500.0)
                .build();

        when(ligneCommandeRepository.findPurchasedLinesByUtilisateurId(10L))
                .thenReturn(List.of(lc1, lc2));
        when(avisRepository.findByUtilisateurIdAndProduitId(10L, 101L))
                .thenReturn(Optional.of(avis));

        Page<PopulationProduitAcheteItem> page =
                historiqueService.getHistoriqueProduitsAchetes(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        PopulationProduitAcheteItem item = page.getContent().get(0);
        assertThat(item.getProduitId()).isEqualTo(101L);
        assertThat(item.getNomProduit()).isEqualTo("Tisane Artémisia Annua");
        assertThat(item.getQuantiteTotaleAchetee()).isEqualTo(3);
        assertThat(item.getMontantTotalDepense()).isEqualTo(7500.0);
        assertThat(item.isDejaEvalue()).isTrue();
        assertThat(item.getAvisId()).isEqualTo(701L);
    }

    @Test
    @DisplayName("Relevé consolidé d'activité du compte - Succès")
    void getReleveActivite_success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByUtilisateurId(10L)).thenReturn(List.of(commande));
        when(paiementRepository.findByCommandeUtilisateurIdAndStatut(10L, StatutPaiement.REUSSI))
                .thenReturn(List.of(paiement));
        when(avisRepository.findByUtilisateurId(10L)).thenReturn(List.of(avis));
        when(favoriRepository.countByUtilisateurId(10L)).thenReturn(4L);
        when(favoriRepository.findByUtilisateurId(10L)).thenReturn(List.of(favori));
        when(paiementRepository.findByCommandeUtilisateurId(10L)).thenReturn(List.of(paiement));
        when(notificationRepository.findByUtilisateurId(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));

        PopulationReleveActiviteResponse releve = historiqueService.getReleveActivite();

        assertThat(releve).isNotNull();
        assertThat(releve.getUtilisateurId()).isEqualTo(10L);
        assertThat(releve.getNomComplet()).isEqualTo("Ousmane Diarra");
        assertThat(releve.getEmail()).isEqualTo("patient@ladafura.ml");
        assertThat(releve.getTotalCommandes()).isEqualTo(1);
        assertThat(releve.getTotalCommandesLivrees()).isEqualTo(1);
        assertThat(releve.getTotalDepense()).isEqualTo(5000.0);
        assertThat(releve.getTotalAvis()).isEqualTo(1);
        assertThat(releve.getTotalFavoris()).isEqualTo(4);
        assertThat(releve.getDernieresActivites()).isNotEmpty();
    }
}
