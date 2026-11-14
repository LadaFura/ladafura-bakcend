package com.pharmacopee.ladafura.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import jakarta.persistence.PersistenceException;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.pharmacopee.ladafura.Models.AttributionRolePharmacopee;
import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.ConfigurationLimiteOffre;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.RolePharmacopee;
import com.pharmacopee.ladafura.Models.SessionCaisse;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.enums.PorteePermission;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.repository.PermissionRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeePermissionRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.RolePlateformePermissionRepository;

/**
 * Vérifie les migrations Flyway V1→V5 sur une base MySQL VIERGE :
 * application des scripts, validation du schéma par Hibernate (ddl-auto=validate),
 * catalogue des rôles/permissions et isolation des permissions entre pharmacopées.
 * <p>
 * Test désactivé par défaut. Il ne s'exécute que si une base dédiée est fournie :
 * <pre>
 * LADAFURA_TEST_DB_URL='jdbc:mysql://127.0.0.1:3306/ladafura_test?useSSL=false&amp;allowPublicKeyRetrieval=true' \
 * LADAFURA_TEST_DB_USER=root LADAFURA_TEST_DB_PASSWORD= \
 * ./mvnw test -Dtest=SchemaMigrationIntegrationTest
 * </pre>
 * Ne JAMAIS pointer cette variable vers la base Aiven.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "LADAFURA_TEST_DB_URL", matches = ".+")
class SchemaMigrationIntegrationTest {

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

    @Autowired private Flyway flyway;
    @Autowired private TestEntityManager em;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private RolePharmacopeeRepository rolePharmacopeeRepository;
    @Autowired private RolePharmacopeePermissionRepository rolePharmacopeePermissionRepository;
    @Autowired private RolePlateformePermissionRepository rolePlateformePermissionRepository;

    @Test
    @DisplayName("Flyway applique V1 à V5 sans échec et Hibernate valide le schéma")
    void migrations_appliquees_et_schema_valide() {
        // Le simple démarrage du contexte prouve la validation Hibernate (ddl-auto=validate).
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("5");
        for (MigrationInfo info : flyway.info().applied()) {
            assertThat(info.getState().isFailed()).as(info.getScript()).isFalse();
        }
    }

    @Test
    @DisplayName("Le catalogue de permissions et les rôles système sont chargés")
    void catalogue_initial_present() {
        assertThat(permissionRepository.findByPorteeOrderByModuleAscCodeAsc(PorteePermission.PLATEFORME)).isNotEmpty();
        long nbPermissionsPharmacopee = permissionRepository.findByPorteeOrderByModuleAscCodeAsc(PorteePermission.PHARMACOPEE).size();
        assertThat(nbPermissionsPharmacopee).isGreaterThan(0);

        RolePharmacopee gerant = rolePharmacopeeRepository.findByCodeAndPharmacopeeIsNull("GERANT").orElseThrow();
        assertThat(rolePharmacopeePermissionRepository.findByRolePharmacopeeId(gerant.getId()))
                .hasSize((int) nbPermissionsPharmacopee);

        for (String code : new String[] {"MANAGER", "VENDEUR_CAISSIER", "COMPTABLE", "GESTIONNAIRE_STOCK", "GESTIONNAIRE_ACHATS"}) {
            RolePharmacopee role = rolePharmacopeeRepository.findByCodeAndPharmacopeeIsNull(code).orElseThrow();
            assertThat(rolePharmacopeePermissionRepository.findByRolePharmacopeeId(role.getId())).as(code).isNotEmpty();
        }
        for (String ancienCode : new String[] {"PROPRIETAIRE", "VENDEUR", "CAISSIER", "MAGASINIER", "COLLABORATEUR"}) {
            assertThat(rolePharmacopeeRepository.findByCodeAndPharmacopeeIsNull(ancienCode)).as(ancienCode).isEmpty();
        }
    }

    @Test
    @DisplayName("Un vendeur n'a ses permissions que dans sa pharmacopée, et seulement s'il est ACTIF")
    void permissions_isolees_par_pharmacopee() {
        Utilisateur vendeur = utilisateur("vendeur@test.ml");
        Pharmacopee pharmacopeeA = pharmacopee("Officine A");
        Pharmacopee pharmacopeeB = pharmacopee("Officine B");
        RolePharmacopee roleVendeur = rolePharmacopeeRepository.findByCodeAndPharmacopeeIsNull("VENDEUR_CAISSIER").orElseThrow();

        MembrePharmacopee membre = em.persist(MembrePharmacopee.builder()
                .utilisateur(vendeur).pharmacopee(pharmacopeeA).statut(StatutMembre.ACTIF).estProprietaire(false).build());
        em.persist(AttributionRolePharmacopee.builder().membrePharmacopee(membre).rolePharmacopee(roleVendeur).build());
        em.flush();

        Set<String> permissionsA = rolePharmacopeePermissionRepository.findCodesPermissionsEffectives(vendeur.getId(), pharmacopeeA.getId());
        assertThat(permissionsA).contains("VENTES_CREER").doesNotContain("ROLES_GERER", "MEMBRES_GERER");
        assertThat(rolePharmacopeePermissionRepository.findCodesPermissionsEffectives(vendeur.getId(), pharmacopeeB.getId())).isEmpty();

        membre.setStatut(StatutMembre.SUSPENDU);
        em.flush();
        assertThat(rolePharmacopeePermissionRepository.findCodesPermissionsEffectives(vendeur.getId(), pharmacopeeA.getId())).isEmpty();
    }

    @Test
    @DisplayName("Un rôle personnalisé n'est visible que dans sa pharmacopée")
    void role_personnalise_invisible_pour_autre_pharmacopee() {
        Pharmacopee pharmacopeeA = pharmacopee("Officine A");
        Pharmacopee pharmacopeeB = pharmacopee("Officine B");
        RolePharmacopee rolePerso = em.persist(RolePharmacopee.builder()
                .code("VENDEUR_SENIOR").libelle("Vendeur senior").pharmacopee(pharmacopeeB).systeme(false).actif(true).build());
        em.flush();

        assertThat(rolePharmacopeeRepository.findDisponiblesPourPharmacopee(pharmacopeeA.getId()))
                .extracting(RolePharmacopee::getCode).doesNotContain("VENDEUR_SENIOR").contains("VENDEUR_CAISSIER");
        assertThat(rolePharmacopeeRepository.findDisponibleByIdPourPharmacopee(rolePerso.getId(), pharmacopeeA.getId())).isEmpty();
        assertThat(rolePharmacopeeRepository.findDisponibleByIdPourPharmacopee(rolePerso.getId(), pharmacopeeB.getId())).isPresent();
    }

    @Test
    @DisplayName("Les rôles de plateforme portent leurs permissions")
    void permissions_plateforme() {
        Utilisateur admin = utilisateur("admin@test.ml");
        var roleAdmin = em.getEntityManager()
                .createQuery("select r from RolePlateforme r where r.code = 'ADMINISTRATEUR'", com.pharmacopee.ladafura.Models.RolePlateforme.class)
                .getSingleResult();
        em.persist(com.pharmacopee.ladafura.Models.AttributionRolePlateforme.builder().utilisateur(admin).rolePlateforme(roleAdmin).build());
        em.flush();

        assertThat(rolePlateformePermissionRepository.findCodesPermissionsByUtilisateurId(admin.getId()))
                .contains("PLATEFORME_PHARMACOPEES_VALIDER")
                .doesNotContain("VENTES_CREER");
    }

    @Test
    @DisplayName("Seule l'offre gratuite a des quotas ; les offres payantes restent inactives")
    void catalogue_des_offres() {
        OffreAbonnement gratuite = offre("GRATUITE");
        assertThat(gratuite.getActif()).isTrue();
        for (String code : new String[] {"MENSUELLE", "SEMESTRIELLE", "ANNUELLE"}) {
            assertThat(offre(code).getActif()).as(code).isFalse();
        }

        List<ConfigurationLimiteOffre> quotas = em.getEntityManager()
                .createQuery("select c from ConfigurationLimiteOffre c join fetch c.limite", ConfigurationLimiteOffre.class)
                .getResultList();
        assertThat(quotas).hasSize(4).allMatch(c -> c.getOffre().getId().equals(gratuite.getId()));
        assertThat(quotas).extracting(c -> c.getLimite().getCodeLimite() + "=" + c.getValeurMaximale())
                .containsExactlyInAnyOrder("MAX_UTILISATEURS_ACTIFS=1", "MAX_PRODUITS=20", "MAX_VENTES_MENSUELLES=50", "MAX_PHARMACOPEES=2");
    }

    @Test
    @DisplayName("Un deuxième abonnement en vigueur est refusé, l'historique reste possible")
    void un_seul_abonnement_en_vigueur() {
        Pharmacopee officine = pharmacopee("Officine A");
        em.persist(abonnement(officine, StatutAbonnement.EXPIRE));
        em.persist(abonnement(officine, StatutAbonnement.ACTIF));
        em.flush();

        assertThatThrownBy(() -> {
            em.persist(abonnement(officine, StatutAbonnement.EN_GRACE));
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Un deuxième propriétaire pour une pharmacopée est refusé")
    void un_seul_proprietaire() {
        Pharmacopee officine = pharmacopee("Officine A");
        em.persist(membre(utilisateur("gerant@test.ml"), officine, true));
        em.persist(membre(utilisateur("membre@test.ml"), officine, false));
        em.flush();

        assertThatThrownBy(() -> {
            em.persist(membre(utilisateur("second@test.ml"), officine, true));
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Une caisse n'a qu'une session ouverte ; une nouvelle est possible après clôture")
    void une_seule_session_ouverte() {
        Pharmacopee officine = pharmacopee("Officine A");
        MembrePharmacopee caissier = em.persist(membre(utilisateur("caissier@test.ml"), officine, false));
        Caisse caisse = em.persist(Caisse.builder().pharmacopee(officine).nom("Caisse principale").type(TypeCaisse.PHYSIQUE).build());
        SessionCaisse premiere = em.persist(SessionCaisse.builder().caisse(caisse).ouvertePar(caissier).build());
        em.flush();

        premiere.cloturer(BigDecimal.ZERO, BigDecimal.ZERO, caissier);
        em.flush();
        em.persist(SessionCaisse.builder().caisse(caisse).ouvertePar(caissier).build());
        em.flush();

        assertThatThrownBy(() -> {
            em.persist(SessionCaisse.builder().caisse(caisse).ouvertePar(caissier).build());
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    private OffreAbonnement offre(String code) {
        return em.getEntityManager()
                .createQuery("select o from OffreAbonnement o where o.code = :code", OffreAbonnement.class)
                .setParameter("code", code)
                .getSingleResult();
    }

    private Abonnement abonnement(Pharmacopee officine, StatutAbonnement statut) {
        OffreAbonnement gratuite = offre("GRATUITE");
        return Abonnement.builder()
                .pharmacopee(officine).offre(gratuite).statut(statut)
                .codeOffreSouscrite(gratuite.getCode()).nomOffreSouscrite(gratuite.getNom())
                .prixSouscrit(gratuite.getPrix()).deviseSouscrite(gratuite.getDevise())
                .build();
    }

    private MembrePharmacopee membre(Utilisateur utilisateur, Pharmacopee officine, boolean proprietaire) {
        return MembrePharmacopee.builder()
                .utilisateur(utilisateur).pharmacopee(officine).statut(StatutMembre.ACTIF).estProprietaire(proprietaire).build();
    }

    private Utilisateur utilisateur(String email) {
        Utilisateur u = new Utilisateur();
        u.setNom("Traoré");
        u.setPrenom("Awa");
        u.setEmail(email);
        u.setRole(Role.POPULATION);
        return em.persist(u);
    }

    private Pharmacopee pharmacopee(String nom) {
        return em.persist(Pharmacopee.builder().nom(nom).statut(StatutPharmacopee.VALIDEE).build());
    }
}
