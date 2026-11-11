package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.LimiteAbonnement;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.ParametrePlateforme;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutVente;
import com.pharmacopee.ladafura.exceptions.QuotaExceededException;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.LimiteAbonnementRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.VenteRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeQuotaServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

@ExtendWith(MockitoExtension.class)
class PharmacopeeQuotaServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;
    private static final Long ABONNEMENT_ID = 50L;

    @Mock private AbonnementRepository abonnementRepository;
    @Mock private LimiteAbonnementRepository limiteAbonnementRepository;
    @Mock private MembrePharmacopeeRepository membrePharmacopeeRepository;
    @Mock private DisponibiliteProduitRepository disponibiliteProduitRepository;
    @Mock private VenteRepository venteRepository;
    @Mock private com.pharmacopee.ladafura.repository.ParametrePlateformeRepository parametreRepository;

    @InjectMocks private PharmacopeeQuotaServiceImpl service;

    private void limite(String code, LimiteAbonnement limite) {
        when(abonnementRepository.findEnVigueur(PHARMACOPEE_ID)).thenReturn(Optional.of(Abonnement.builder().id(ABONNEMENT_ID).build()));
        when(limiteAbonnementRepository.findByAbonnementIdAndCodeLimite(ABONNEMENT_ID, code)).thenReturn(Optional.ofNullable(limite));
    }

    private static LimiteAbonnement max(int valeur) {
        return LimiteAbonnement.builder().valeurMaximale(valeur).illimite(false).build();
    }

    @Test
    @DisplayName("offre gratuite : un deuxième membre actif est refusé")
    void deuxiemeMembreRefuse() {
        limite(IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS, max(1));
        when(membrePharmacopeeRepository.countByPharmacopeeIdAndStatut(PHARMACOPEE_ID, StatutMembre.ACTIF)).thenReturn(1L);

        assertThatThrownBy(() -> service.verifierQuota(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    @DisplayName("sous la limite de produits, la création est permise")
    void produitsSousLaLimite() {
        limite(IPharmacopeeQuotaService.MAX_PRODUITS, max(20));
        when(disponibiliteProduitRepository.countByPharmacopeeId(PHARMACOPEE_ID)).thenReturn(19L);

        assertThatCode(() -> service.verifierQuota(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_PRODUITS)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("seules les ventes validées du mois consomment le quota mensuel")
    void ventesMensuelles() {
        limite(IPharmacopeeQuotaService.MAX_VENTES_MENSUELLES, max(50));
        when(venteRepository.countByPharmacopeeIdAndStatutAndDateVenteBetween(eq(PHARMACOPEE_ID), eq(StatutVente.VALIDEE), any(), any()))
                .thenReturn(50L);

        assertThatThrownBy(() -> service.verifierQuota(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_VENTES_MENSUELLES))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    @DisplayName("une limite illimitée accepte toujours")
    void illimite() {
        limite(IPharmacopeeQuotaService.MAX_PRODUITS, LimiteAbonnement.builder().illimite(true).build());
        when(disponibiliteProduitRepository.countByPharmacopeeId(PHARMACOPEE_ID)).thenReturn(100_000L);

        assertThatCode(() -> service.verifierQuota(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_PRODUITS)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sans abonnement en vigueur, le quota est refusé")
    void sansAbonnement() {
        when(abonnementRepository.findEnVigueur(PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifierQuota(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_PRODUITS))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    @DisplayName("une limite absente de l'abonnement est refusée par prudence")
    void limiteAbsente() {
        limite(IPharmacopeeQuotaService.MAX_PRODUITS, null);

        assertThatThrownBy(() -> service.verifierQuota(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_PRODUITS))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    @DisplayName("un code de limite inconnu est une erreur de programmation")
    void codeInconnu() {
        limite("MAX_INCONNU", max(1));

        assertThatThrownBy(() -> service.verifierQuota(PHARMACOPEE_ID, "MAX_INCONNU")).isInstanceOf(IllegalArgumentException.class);
    }

    private void gerantDe(Long utilisateurId, Long... pharmacopeesIds) {
        List<MembrePharmacopee> adhesions = new ArrayList<>();
        for (Long id : pharmacopeesIds) {
            Pharmacopee pharmacopee = new Pharmacopee();
            pharmacopee.setId(id);
            MembrePharmacopee membre = new MembrePharmacopee();
            membre.setPharmacopee(pharmacopee);
            membre.setEstProprietaire(true);
            adhesions.add(membre);
        }
        when(membrePharmacopeeRepository.findByUtilisateurIdAndStatut(utilisateurId, StatutMembre.ACTIF)).thenReturn(adhesions);
    }

    private void limitePharmacopees(Long pharmacopeeId, Long abonnementId, LimiteAbonnement limite) {
        Abonnement abonnement = new Abonnement();
        abonnement.setId(abonnementId);
        when(abonnementRepository.findEnVigueur(pharmacopeeId)).thenReturn(Optional.of(abonnement));
        when(limiteAbonnementRepository.findByAbonnementIdAndCodeLimite(abonnementId, IPharmacopeeQuotaService.MAX_PHARMACOPEES))
                .thenReturn(Optional.ofNullable(limite));
    }

    @Test
    @DisplayName("Nombre de pharmacopées : la formule la plus favorable du gérant s'applique")
    void pharmacopees_FormuleLaPlusFavorable() {
        gerantDe(1L, 5L, 6L);
        limitePharmacopees(5L, 50L, max(2));
        limitePharmacopees(6L, 60L, max(5));

        assertThat(service.maxPharmacopeesParGerant(1L)).isEqualTo(5);
    }

    @Test
    @DisplayName("Nombre de pharmacopées : une formule illimitée lève le plafond")
    void pharmacopees_Illimite() {
        LimiteAbonnement illimitee = max(0);
        illimitee.setIllimite(true);
        gerantDe(1L, 5L);
        limitePharmacopees(5L, 50L, illimitee);

        assertThat(service.maxPharmacopeesParGerant(1L)).isEqualTo(IPharmacopeeQuotaService.PHARMACOPEES_ILLIMITEES);
    }

    @Test
    @DisplayName("Nombre de pharmacopées : sans limite dans l'abonnement, le paramètre plateforme s'applique")
    void pharmacopees_RepliSurParametre() {
        ParametrePlateforme parametre = new ParametrePlateforme();
        parametre.setValeur("3");
        gerantDe(1L, 5L);
        limitePharmacopees(5L, 50L, null);
        when(parametreRepository.findByCode("MAX_PHARMACOPEES_PAR_GERANT")).thenReturn(Optional.of(parametre));

        assertThat(service.maxPharmacopeesParGerant(1L)).isEqualTo(3);
    }

    @Test
    @DisplayName("Consommation : nombre de pharmacopées détenues par le gérant de cette pharmacopée")
    void pharmacopees_Consommation() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        MembrePharmacopee gerant = new MembrePharmacopee();
        gerant.setUtilisateur(utilisateur);
        when(membrePharmacopeeRepository.findFirstByPharmacopeeIdAndEstProprietaireTrue(PHARMACOPEE_ID)).thenReturn(Optional.of(gerant));
        when(membrePharmacopeeRepository.countByUtilisateurIdAndEstProprietaireTrueAndStatut(1L, StatutMembre.ACTIF)).thenReturn(2L);

        assertThat(service.consommationActuelle(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_PHARMACOPEES)).isEqualTo(2L);
    }

    @Test
    @DisplayName("Formule du gérant : sa 2e pharmacopée en offre gratuite profite des limites de sa formule payante")
    void formuleDuGerantCouvreSesPharmacopees() {
        Utilisateur gerant = new Utilisateur();
        gerant.setId(1L);
        MembrePharmacopee adhesion = new MembrePharmacopee();
        adhesion.setUtilisateur(gerant);
        when(membrePharmacopeeRepository.findFirstByPharmacopeeIdAndEstProprietaireTrue(6L)).thenReturn(Optional.of(adhesion));
        gerantDe(1L, 5L, 6L);
        Abonnement gratuit = new Abonnement();
        gratuit.setId(60L);
        Abonnement semestriel = new Abonnement();
        semestriel.setId(50L);
        when(abonnementRepository.findEnVigueur(6L)).thenReturn(Optional.of(gratuit));
        when(abonnementRepository.findEnVigueur(5L)).thenReturn(Optional.of(semestriel));
        when(limiteAbonnementRepository.findByAbonnementIdAndCodeLimite(60L, IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS))
                .thenReturn(Optional.of(max(1)));
        when(limiteAbonnementRepository.findByAbonnementIdAndCodeLimite(50L, IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS))
                .thenReturn(Optional.of(max(20)));
        when(membrePharmacopeeRepository.countByPharmacopeeIdAndStatut(6L, StatutMembre.ACTIF)).thenReturn(1L);

        assertThatCode(() -> service.verifierQuota(6L, IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS)).doesNotThrowAnyException();
        assertThat(service.plafondApplicable(6L, IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS))
                .hasValueSatisfying(p -> {
                    assertThat(p.valeurMaximale()).isEqualTo(20);
                    assertThat(p.abonnementId()).isEqualTo(50L);
                });
    }
}
