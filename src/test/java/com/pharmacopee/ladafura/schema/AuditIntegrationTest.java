package com.pharmacopee.ladafura.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.pharmacopee.ladafura.Models.JournalAudit;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.services.impl.JournalAuditConsultationServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditConsultationService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;

/**
 * Consultation du journal d'audit sur une base MySQL VIERGE.
 * Mêmes prérequis que {@link SchemaMigrationIntegrationTest}. Ne JAMAIS pointer vers Aiven.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "LADAFURA_TEST_DB_URL", matches = ".+")
@Import(JournalAuditConsultationServiceImpl.class)
class AuditIntegrationTest {

    private static final Pageable PAGE = PageRequest.of(0, 20);

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
    @Autowired private IJournalAuditConsultationService service;
    @MockitoBean private IPharmacopeeContexteService contexteService;

    @Test
    @DisplayName("Chaque pharmacopée ne voit que ses entrées ; l'administrateur voit tout et filtre")
    void isolationEtFiltres() {
        Pharmacopee officineA = em.persist(Pharmacopee.builder().nom("Officine A").statut(StatutPharmacopee.VALIDEE).build());
        Pharmacopee officineB = em.persist(Pharmacopee.builder().nom("Officine B").statut(StatutPharmacopee.VALIDEE).build());
        entree(officineA, TypeActionAudit.VENTE_ANNULEE, OrigineAction.PHARMACOPEE, "Vente");
        entree(officineA, TypeActionAudit.ACHAT_ANNULE, OrigineAction.PHARMACOPEE, "Achat");
        entree(officineB, TypeActionAudit.VENTE_ANNULEE, OrigineAction.ADMINISTRATION, "Vente");
        entree(null, TypeActionAudit.PARAMETRE_MODIFIE, OrigineAction.ADMINISTRATION, "ParametrePlateforme");
        em.flush();
        when(contexteService.verifierPermission(IPharmacopeeContexteService.AUDIT_CONSULTER))
                .thenReturn(new ContexteMembre(new MembrePharmacopee(), officineA, Set.of()));

        assertThat(service.getJournalPharmacopee(null, null, null, null, PAGE).getTotalElements()).isEqualTo(2);
        assertThat(service.getJournalPharmacopee(TypeActionAudit.VENTE_ANNULEE, "Vente", null, null, PAGE).getTotalElements()).isEqualTo(1);

        assertThat(service.getJournalAdmin(officineB.getId(), null, null, null, null, null, PAGE).getTotalElements()).isEqualTo(1);
        assertThat(service.getJournalAdmin(officineA.getId(), TypeActionAudit.ACHAT_ANNULE, OrigineAction.PHARMACOPEE, "Achat", null, null, PAGE).getTotalElements()).isEqualTo(1);
        assertThat(service.getJournalAdmin(officineA.getId(), null, OrigineAction.ADMINISTRATION, null, null, null, PAGE).getTotalElements()).isZero();

        LocalDate aujourdhui = LocalDate.now();
        assertThat(service.getJournalPharmacopee(null, null, aujourdhui, aujourdhui, PAGE).getTotalElements()).isEqualTo(2);
        assertThat(service.getJournalPharmacopee(null, null, aujourdhui.plusDays(1), null, PAGE).getTotalElements()).isZero();
    }

    private void entree(Pharmacopee pharmacopee, TypeActionAudit action, OrigineAction origine, String typeEntite) {
        em.persist(JournalAudit.builder().pharmacopee(pharmacopee).action(action).origine(origine).typeEntite(typeEntite).build());
    }
}
