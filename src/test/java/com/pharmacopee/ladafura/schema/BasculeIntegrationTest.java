package com.pharmacopee.ladafura.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.LigneCommande;
import com.pharmacopee.ladafura.Models.LignePanier;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Panier;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.auth.PharmacopeeAuthResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.auth.PraticienRegisterRequest;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeDetailResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCreateCommandeRequest;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.mappers.PopulationCommandeMapper;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.AttributionRolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.CaisseRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.MouvementStockRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.services.impl.AbonnementCycleServiceImpl;
import com.pharmacopee.ladafura.services.impl.CommandeStockCaisseServiceImpl;
import com.pharmacopee.ladafura.services.impl.JournalAuditServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeAuthServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeContexteServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeInitialisationServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeInvitationServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeMouvementStockServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeOperationCaisseServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeQuotaServiceImpl;
import com.pharmacopee.ladafura.services.impl.PopulationCommandeServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeInvitationService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeNotificationService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationCommandeService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationNotificationService;

/**
 * Parcours de la bascule sur une base MySQL VIERGE, avec les services réels.
 * Mêmes prérequis que {@link SchemaMigrationIntegrationTest}. Ne JAMAIS pointer vers Aiven.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "LADAFURA_TEST_DB_URL", matches = ".+")
@Import({PharmacopeeAuthServiceImpl.class, PharmacopeeContexteServiceImpl.class, PharmacopeeQuotaServiceImpl.class,
        JournalAuditServiceImpl.class, AbonnementCycleServiceImpl.class, PharmacopeeInitialisationServiceImpl.class,
        PharmacopeeMouvementStockServiceImpl.class, PharmacopeeOperationCaisseServiceImpl.class, CommandeStockCaisseServiceImpl.class,
        PopulationCommandeServiceImpl.class,
        com.pharmacopee.ladafura.services.impl.PaiementDeclareServiceImpl.class, PopulationCommandeMapper.class, PharmacopeeInvitationServiceImpl.class})
class BasculeIntegrationTest {

    private static final String EMAIL_GERANT = "gerant-bascule@test.ml";

    @DynamicPropertySource
    static void proprietes(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("LADAFURA_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> envOuDefaut("LADAFURA_TEST_DB_USER", "root"));
        registry.add("spring.datasource.password", () -> envOuDefaut("LADAFURA_TEST_DB_PASSWORD", ""));
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    private static String envOuDefaut(String nom, String defaut) {
        String valeur = System.getenv(nom);
        return valeur != null ? valeur : defaut;
    }

    @Autowired private TestEntityManager em;
    @Autowired private IPharmacopeeAuthService authService;
    @Autowired private IPharmacopeeInvitationService invitationService;
    @Autowired private IPopulationCommandeService commandeService;
    @Autowired private MembrePharmacopeeRepository membreRepository;
    @Autowired private AttributionRolePharmacopeeRepository attributionRepository;
    @Autowired private AbonnementRepository abonnementRepository;
    @Autowired private CaisseRepository caisseRepository;
    @Autowired private MouvementStockRepository mouvementStockRepository;
    @Autowired private OperationCaisseRepository operationCaisseRepository;
    @Autowired private com.pharmacopee.ladafura.services.interfaces.ICommandeStockCaisseService commandeStockCaisseService;
    @MockitoBean private IFirebaseAuthService firebaseAuthService;
    @MockitoBean private IPopulationAuthService populationAuthService;
    @MockitoBean private IPharmacopeeNotificationService pharmacopeeNotificationService;
    @MockitoBean private IPopulationNotificationService populationNotificationService;

    private final MockHttpServletRequest requete = new MockHttpServletRequest();

    @BeforeEach
    void preparer() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requete));
        when(firebaseAuthService.createUser(anyString(), anyString(), anyString())).thenReturn("uid-gerant-bascule");
    }

    @AfterEach
    void nettoyer() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    private static void connecter(String email) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email, "jeton", List.of()));
    }

    private PharmacopeeAuthResponse inscrire() {
        PraticienRegisterRequest demande = new PraticienRegisterRequest();
        demande.setNom("Diarra");
        demande.setPrenom("Aminata");
        demande.setEmail(EMAIL_GERANT);
        demande.setMotDePasse("MotDePasse1!");
        demande.setNomPharmacopee("Officine de Mopti");
        demande.setTelephonePharmacopee("+223 20 42 00 01");
        PharmacopeeAuthResponse reponse = authService.registerPraticien(demande);
        em.flush();
        return reponse;
    }

    private Utilisateur citoyen(String email) {
        Utilisateur citoyen = new Utilisateur();
        citoyen.setNom("Sidibé");
        citoyen.setPrenom("Oumar");
        citoyen.setEmail(email);
        citoyen.setRole(Role.POPULATION);
        citoyen.setStatut(StatutUtilisateur.ACTIF);
        return em.persist(citoyen);
    }

    private Pharmacopee officineValidee(Long id) {
        Pharmacopee officine = em.find(Pharmacopee.class, id);
        officine.setStatut(StatutPharmacopee.VALIDEE);
        return officine;
    }

    @Test
    @DisplayName("l'inscription crée le gérant, l'abonnement gratuit et les trois comptes")
    void inscription() {
        PharmacopeeAuthResponse inscription = inscrire();
        Long pharmacopeeId = inscription.getPharmacopeeId();

        MembrePharmacopee gerant = membreRepository.findFirstByPharmacopeeIdAndEstProprietaireTrue(pharmacopeeId).orElseThrow();
        assertThat(attributionRepository.findByMembrePharmacopeeId(gerant.getId()))
                .singleElement().satisfies(a -> assertThat(a.getRolePharmacopee().getCode()).isEqualTo("GERANT"));
        Abonnement abonnement = abonnementRepository.findEnVigueur(pharmacopeeId).orElseThrow();
        assertThat(abonnement.getCodeOffreSouscrite()).isEqualTo("GRATUITE");
        assertThat(abonnement.getStatut()).isEqualTo(StatutAbonnement.ACTIF);
        assertThat(caisseRepository.findByPharmacopeeIdAndActifTrue(pharmacopeeId)).extracting(Caisse::getType)
                .containsExactlyInAnyOrder(TypeCaisse.PHYSIQUE, TypeCaisse.MOBILE_MONEY, TypeCaisse.BANQUE);

        connecter(EMAIL_GERANT);
        PharmacopeeAuthResponse me = authService.getMe();
        assertThat(me.getRoles()).containsExactly("GERANT");
        assertThat(me.getPermissions()).contains("MEMBRES_INVITER", "ABONNEMENT_GERER");
        assertThat(me.getPlanAbonnement()).isEqualTo("GRATUIT");
    }

    @Test
    @DisplayName("une commande sort du stock et entre en trésorerie ; son annulation inverse les deux")
    void commandeEtAnnulation() {
        Long pharmacopeeId = inscrire().getPharmacopeeId();
        Pharmacopee officine = officineValidee(pharmacopeeId);
        Produit produit = em.persist(Produit.builder().nom("Tisane de kinkéliba").prix(1500.0).statut(StatutProduit.VALIDE).build());
        DisponibiliteProduit fiche = em.persist(DisponibiliteProduit.builder().pharmacopee(officine).produit(produit)
                .disponible(true).quantiteStock(10).prix(2000.0).build());
        ModeRetrait mode = em.persist(ModeRetrait.builder().pharmacopee(officine).type(TypeModeRetrait.LIVRAISON).actif(true).frais(500.0).build());
        Utilisateur client = citoyen("client-bascule@test.ml");
        Panier panier = em.persist(Panier.builder().utilisateur(client).dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now()).lignes(new ArrayList<>()).build());
        LignePanier ligne = em.persist(LignePanier.builder().panier(panier).produit(produit).quantite(2).prixUnitaire(2000.0).sousTotal(4000.0).build());
        panier.getLignes().add(ligne);
        em.flush();
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(client);

        Long compteId = caisseRepository.findFirstByPharmacopeeIdAndTypeAndActifTrue(pharmacopeeId, TypeCaisse.MOBILE_MONEY).orElseThrow().getId();
        PopulationCommandeDetailResponse commande = commandeService.passerCommande(PopulationCreateCommandeRequest.builder()
                .pharmacopeeId(pharmacopeeId).modeRetraitId(mode.getId()).adresseLivraison("Sévaré, Mopti")
                .methode(MethodePaiement.MOBILE_MONEY).telephoneMobileMoney("+223 70 11 22 33")
                .compteMobileMoneyId(compteId).referenceTransaction("OM-BASCULE-1").build());
        em.flush();

        assertThat(fiche.getQuantiteStock()).isEqualTo(8);
        LigneCommande ligneCommande = em.find(Commande.class, commande.getId()).getLignes().get(0);
        assertThat(mouvementStockRepository.findByLigneCommandeId(ligneCommande.getId())).singleElement()
                .satisfies(m -> assertThat(m.getType()).isEqualTo(TypeMouvementStock.SORTIE_COMMANDE));
        // La déclaration du client ne fait entrer aucun argent en trésorerie.
        Paiement paiement = em.find(Commande.class, commande.getId()).getPaiement();
        assertThat(paiement.getStatut()).isEqualTo(StatutPaiement.EN_ATTENTE);
        assertThat(operationCaisseRepository.soldeTheorique(compteId)).isEqualByComparingTo("0");

        paiement.setStatut(StatutPaiement.REUSSI);
        commandeStockCaisseService.encaisser(paiement, compteId, null);
        em.flush();
        assertThat(operationCaisseRepository.soldeTheorique(compteId)).isEqualByComparingTo("4500");

        commandeService.annulerCommande(commande.getId());
        em.flush();

        assertThat(fiche.getQuantiteStock()).isEqualTo(10);
        assertThat(operationCaisseRepository.soldeTheorique(compteId)).isEqualByComparingTo("0");
        assertThat(em.find(Commande.class, commande.getId()).getPaiement().getStatut()).isEqualTo(StatutPaiement.ANNULE);
    }

    @Test
    @DisplayName("un abonnement en lecture seule refuse les nouvelles commandes")
    void lectureSeuleRefuseLesCommandes() {
        Long pharmacopeeId = inscrire().getPharmacopeeId();
        officineValidee(pharmacopeeId);
        abonnementRepository.findEnVigueur(pharmacopeeId).orElseThrow().setStatut(StatutAbonnement.EN_GRACE);
        em.flush();
        Utilisateur client = citoyen("client-grace@test.ml");
        Produit produit = em.persist(Produit.builder().nom("Poudre de baobab").prix(1500.0).statut(StatutProduit.VALIDE).build());
        Panier panier = em.persist(Panier.builder().utilisateur(client).dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now()).lignes(new ArrayList<>()).build());
        panier.getLignes().add(em.persist(LignePanier.builder().panier(panier).produit(produit).quantite(1).prixUnitaire(1500.0).sousTotal(1500.0).build()));
        em.flush();
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(client);

        assertThatThrownBy(() -> commandeService.passerCommande(PopulationCreateCommandeRequest.builder()
                .pharmacopeeId(pharmacopeeId).modeRetraitId(1L).methode(MethodePaiement.CASH).build()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("nouvelles commandes");
    }

    @Test
    @DisplayName("un citoyen invité accepte, travaille dans sa pharmacopée, et reste refusé ailleurs")
    void citoyenInvite() {
        Long pharmacopeeId = inscrire().getPharmacopeeId();
        Pharmacopee autre = em.persist(Pharmacopee.builder().nom("Autre officine").statut(StatutPharmacopee.VALIDEE).build());
        em.getEntityManager().createQuery("update LimiteAbonnement l set l.valeurMaximale = 5 where l.codeLimite = 'MAX_UTILISATEURS_ACTIFS'").executeUpdate();
        em.clear();
        Pharmacopee officine = em.find(Pharmacopee.class, pharmacopeeId);
        Utilisateur employe = citoyen("employe-bascule@test.ml");
        MembrePharmacopee invitation = em.persist(MembrePharmacopee.builder().utilisateur(employe).pharmacopee(officine)
                .statut(StatutMembre.INVITE).estProprietaire(false).dateInvitation(LocalDateTime.now())
                .dateExpirationInvitation(LocalDateTime.now().plusDays(7)).build());
        em.flush();

        connecter(employe.getEmail());
        invitationService.accepter(invitation.getId());
        em.flush();

        assertThat(authService.getCurrentPharmacopee().getId()).isEqualTo(pharmacopeeId);
        requete.addHeader("X-Pharmacopee-Id", String.valueOf(autre.getId()));
        assertThatThrownBy(() -> authService.getMembreCourant()).isInstanceOf(ForbiddenException.class);
    }
}
