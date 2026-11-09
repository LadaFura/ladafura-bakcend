package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.ConfigurationLimiteOffre;
import com.pharmacopee.ladafura.Models.LimiteOffre;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.dto.admin.abonnement.DefinirQuotasOffreRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.EnregistrerOffreRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.LigneQuotaRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.OffreResponse;
import com.pharmacopee.ladafura.enums.TypeOffre;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.repository.ConfigurationLimiteOffreRepository;
import com.pharmacopee.ladafura.repository.LimiteOffreRepository;
import com.pharmacopee.ladafura.repository.OffreAbonnementRepository;
import com.pharmacopee.ladafura.services.impl.AdminOffreServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;

@ExtendWith(MockitoExtension.class)
class AdminOffreServiceImplTest {

    @Mock private OffreAbonnementRepository offreRepository;
    @Mock private LimiteOffreRepository limiteRepository;
    @Mock private ConfigurationLimiteOffreRepository configurationRepository;
    @Mock private IJournalAuditService journalAuditService;
    @Mock private IPharmacopeeAuthService pharmacopeeAuthService;

    @InjectMocks private AdminOffreServiceImpl service;

    @BeforeEach
    void preparer() {
        lenient().when(offreRepository.save(any(OffreAbonnement.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(configurationRepository.findByOffreId(any())).thenReturn(List.of());
    }

    private OffreAbonnement offre(TypeOffre type, String prix) {
        OffreAbonnement o = OffreAbonnement.builder().id(1L).code("X").typeOffre(type).prix(new BigDecimal(prix)).actif(false).build();
        when(offreRepository.findById(1L)).thenReturn(Optional.of(o));
        return o;
    }

    private static EnregistrerOffreRequest requete(TypeOffre type, String prix) {
        return EnregistrerOffreRequest.builder().code("PRO").nom("Pro").typeOffre(type).prix(new BigDecimal(prix)).joursGrace(7).build();
    }

    private void quotas(int nombre, long total) {
        List<ConfigurationLimiteOffre> configs = java.util.stream.IntStream.range(0, nombre)
                .mapToObj(i -> ConfigurationLimiteOffre.builder().limite(LimiteOffre.builder().id((long) i).build()).build()).toList();
        when(configurationRepository.findByOffreId(1L)).thenReturn(configs);
        when(limiteRepository.count()).thenReturn(total);
    }

    @Test
    @DisplayName("une offre est créée inactive, avec la durée de son type")
    void creation() {
        when(offreRepository.findByCode("PRO")).thenReturn(Optional.empty());

        OffreResponse reponse = service.creer(requete(TypeOffre.SEMESTRIELLE, "54000"));

        assertThat(reponse.getActif()).isFalse();
        assertThat(reponse.getDureeMois()).isEqualTo(6);
    }

    @Test
    @DisplayName("un code déjà utilisé est refusé")
    void codeDouble() {
        when(offreRepository.findByCode("PRO")).thenReturn(Optional.of(new OffreAbonnement()));

        assertThatThrownBy(() -> service.creer(requete(TypeOffre.MENSUELLE, "1000"))).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("une offre gratuite doit avoir un prix nul")
    void gratuitePayante() {
        when(offreRepository.findByCode("PRO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.creer(requete(TypeOffre.GRATUITE, "1000"))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("une offre payante à 0 F n'est pas activable")
    void activationPrixNul() {
        offre(TypeOffre.MENSUELLE, "0");

        assertThatThrownBy(() -> service.activer(1L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("une offre payante sans tous ses quotas n'est pas activable")
    void activationSansQuotas() {
        offre(TypeOffre.MENSUELLE, "10000");
        quotas(2, 3);

        assertThatThrownBy(() -> service.activer(1L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("une offre payante complète est activée")
    void activation() {
        OffreAbonnement o = offre(TypeOffre.MENSUELLE, "10000");
        quotas(3, 3);

        service.activer(1L);

        assertThat(o.getActif()).isTrue();
    }

    @Test
    @DisplayName("l'offre gratuite ne peut pas être désactivée")
    void desactivationGratuite() {
        offre(TypeOffre.GRATUITE, "0");

        assertThatThrownBy(() -> service.desactiver(1L)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("une limite ni chiffrée ni illimitée est refusée")
    void quotaIncomplet() {
        offre(TypeOffre.MENSUELLE, "10000");
        DefinirQuotasOffreRequest request = new DefinirQuotasOffreRequest(List.of(new LigneQuotaRequest(9L, null, false)));

        assertThatThrownBy(() -> service.definirQuotas(1L, request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("les quotas existants sont mis à jour, les nouveaux créés, les absents retirés")
    void definitionQuotas() {
        offre(TypeOffre.MENSUELLE, "10000");
        ConfigurationLimiteOffre existante = ConfigurationLimiteOffre.builder().limite(LimiteOffre.builder().id(1L).build()).valeurMaximale(5).illimite(false).build();
        ConfigurationLimiteOffre retiree = ConfigurationLimiteOffre.builder().limite(LimiteOffre.builder().id(2L).build()).build();
        when(configurationRepository.findByOffreId(1L)).thenReturn(List.of(existante, retiree));
        when(limiteRepository.findById(3L)).thenReturn(Optional.of(LimiteOffre.builder().id(3L).build()));

        service.definirQuotas(1L, new DefinirQuotasOffreRequest(List.of(new LigneQuotaRequest(1L, 10, false), new LigneQuotaRequest(3L, null, true))));

        assertThat(existante.getValeurMaximale()).isEqualTo(10);
        verify(configurationRepository).delete(retiree);
        verify(configurationRepository).save(Mockito.argThat(c -> c.getLimite().getId().equals(3L) && c.getIllimite() && c.getValeurMaximale() == null));
    }
}
