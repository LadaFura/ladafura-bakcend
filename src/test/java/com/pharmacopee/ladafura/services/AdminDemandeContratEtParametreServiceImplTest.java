package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.DemandeContrat;
import com.pharmacopee.ladafura.Models.LimiteOffre;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.ParametrePlateforme;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.admin.abonnement.CreerAbonnementContratRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.LigneQuotaRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.TraiterDemandeContratRequest;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutDemandeContrat;
import com.pharmacopee.ladafura.enums.TypeValeurParametre;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.DemandeContratRepository;
import com.pharmacopee.ladafura.repository.LimiteOffreRepository;
import com.pharmacopee.ladafura.repository.OffreAbonnementRepository;
import com.pharmacopee.ladafura.repository.ParametrePlateformeRepository;
import com.pharmacopee.ladafura.services.impl.AdminDemandeContratServiceImpl;
import com.pharmacopee.ladafura.services.impl.AdminParametreServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;

class AdminDemandeContratEtParametreServiceImplTest {

    private final DemandeContratRepository demandeRepository = mock(DemandeContratRepository.class);
    private final AbonnementRepository abonnementRepository = mock(AbonnementRepository.class);
    private final OffreAbonnementRepository offreRepository = mock(OffreAbonnementRepository.class);
    private final LimiteOffreRepository limiteRepository = mock(LimiteOffreRepository.class);
    private final IAbonnementCycleService cycleService = mock(IAbonnementCycleService.class);
    private final IPharmacopeeAuthService authService = mock(IPharmacopeeAuthService.class);
    private final ParametrePlateformeRepository parametreRepository = mock(ParametrePlateformeRepository.class);

    private final AdminDemandeContratServiceImpl demandes = new AdminDemandeContratServiceImpl(
            demandeRepository, abonnementRepository, offreRepository, limiteRepository, cycleService, authService);
    private final AdminParametreServiceImpl parametres = new AdminParametreServiceImpl(
            parametreRepository, mock(IJournalAuditService.class), authService);

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(5L).nom("Officine").build();

    @BeforeEach
    void preparer() {
        when(authService.getCurrentUtilisateur()).thenReturn(new Utilisateur());
    }

    private DemandeContrat demande(StatutDemandeContrat statut, Pharmacopee ph) {
        DemandeContrat d = DemandeContrat.builder().id(1L).statut(statut).pharmacopee(ph).build();
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(d));
        return d;
    }

    private static CreerAbonnementContratRequest conditions() {
        return CreerAbonnementContratRequest.builder().prix(new BigDecimal("500000")).dureeMois(24).joursGrace(15)
                .quotas(List.of(new LigneQuotaRequest(9L, null, true))).build();
    }

    @Test
    @DisplayName("une demande avance selon son cycle")
    void transitionValide() {
        DemandeContrat d = demande(StatutDemandeContrat.NOUVELLE, pharmacopee);

        demandes.traiter(1L, new TraiterDemandeContratRequest(StatutDemandeContrat.EN_COURS, "Appel prévu"));

        assertThat(d.getStatut()).isEqualTo(StatutDemandeContrat.EN_COURS);
        assertThat(d.getDateTraitement()).isNotNull();
    }

    @Test
    @DisplayName("une transition hors cycle est refusée")
    void transitionInvalide() {
        demande(StatutDemandeContrat.NOUVELLE, pharmacopee);

        assertThatThrownBy(() -> demandes.traiter(1L, new TraiterDemandeContratRequest(StatutDemandeContrat.ACCEPTEE, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("une demande non acceptée ne crée pas d'abonnement")
    void demandeNonAcceptee() {
        demande(StatutDemandeContrat.EN_COURS, pharmacopee);

        assertThatThrownBy(() -> demandes.creerAbonnement(1L, conditions())).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("la demande d'un prospect sans pharmacopée ne crée pas d'abonnement")
    void prospect() {
        demande(StatutDemandeContrat.ACCEPTEE, null);

        assertThatThrownBy(() -> demandes.creerAbonnement(1L, conditions())).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("une demande acceptée crée l'abonnement négocié, éventuellement au-delà de 12 mois")
    void contratNegocie() {
        DemandeContrat d = demande(StatutDemandeContrat.ACCEPTEE, pharmacopee);
        OffreAbonnement contrat = OffreAbonnement.builder().code("CONTRAT_PERSONNALISE").build();
        when(abonnementRepository.existsByDemandeContratId(1L)).thenReturn(false);
        when(offreRepository.findByCode("CONTRAT_PERSONNALISE")).thenReturn(Optional.of(contrat));
        when(limiteRepository.findById(9L)).thenReturn(Optional.of(LimiteOffre.builder().id(9L).codeLimite("MAX_PRODUITS").build()));
        Abonnement cree = Abonnement.builder().id(50L).pharmacopee(pharmacopee).statut(StatutAbonnement.EN_ATTENTE_PAIEMENT).build();
        when(cycleService.creerNegocie(eq(pharmacopee), eq(contrat), any(), eq(24), anyInt(), any(), eq(d), any())).thenReturn(cree);

        assertThat(demandes.creerAbonnement(1L, conditions()).getId()).isEqualTo(50L);
    }

    @Test
    @DisplayName("une demande ne donne lieu qu'à un seul abonnement")
    void abonnementDejaCree() {
        demande(StatutDemandeContrat.ACCEPTEE, pharmacopee);
        when(abonnementRepository.existsByDemandeContratId(1L)).thenReturn(true);

        assertThatThrownBy(() -> demandes.creerAbonnement(1L, conditions())).isInstanceOf(ConflictException.class);
    }

    private ParametrePlateforme parametre(String code, TypeValeurParametre type) {
        ParametrePlateforme p = ParametrePlateforme.builder().code(code).valeur("2").typeValeur(type).build();
        when(parametreRepository.findByCode(code)).thenReturn(Optional.of(p));
        return p;
    }

    @Test
    @DisplayName("un paramètre entier valide est enregistré")
    void parametreValide() {
        ParametrePlateforme p = parametre("MAX_PHARMACOPEES_PAR_GERANT", TypeValeurParametre.ENTIER);

        parametres.modifier("MAX_PHARMACOPEES_PAR_GERANT", " 3 ");

        assertThat(p.getValeur()).isEqualTo("3");
    }

    @Test
    @DisplayName("une valeur incompatible avec le type est refusée")
    void parametreInvalide() {
        parametre("MAX_PHARMACOPEES_PAR_GERANT", TypeValeurParametre.ENTIER);
        parametre("TAUX_TAXE_ABONNEMENT", TypeValeurParametre.DECIMAL);

        assertThatThrownBy(() -> parametres.modifier("MAX_PHARMACOPEES_PAR_GERANT", "deux")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> parametres.modifier("TAUX_TAXE_ABONNEMENT", "-1")).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("un paramètre inconnu donne 404")
    void parametreInconnu() {
        when(parametreRepository.findByCode("INCONNU")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parametres.modifier("INCONNU", "1")).isInstanceOf(ResourceNotFoundException.class);
        verify(parametreRepository).findByCode("INCONNU");
    }
}
