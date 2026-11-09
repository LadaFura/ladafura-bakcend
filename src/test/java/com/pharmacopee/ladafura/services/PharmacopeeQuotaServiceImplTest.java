package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.LimiteAbonnement;
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
}
