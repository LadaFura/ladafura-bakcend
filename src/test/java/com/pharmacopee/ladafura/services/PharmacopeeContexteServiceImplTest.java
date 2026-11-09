package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeePermissionRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeContexteServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;

@ExtendWith(MockitoExtension.class)
class PharmacopeeContexteServiceImplTest {

    private static final Long UTILISATEUR_ID = 1L;

    @Mock private IPharmacopeeAuthService pharmacopeeAuthService;
    @Mock private MembrePharmacopeeRepository membrePharmacopeeRepository;
    @Mock private RolePharmacopeePermissionRepository rolePharmacopeePermissionRepository;
    @Mock private AbonnementRepository abonnementRepository;

    @InjectMocks private PharmacopeeContexteServiceImpl service;

    private final MockHttpServletRequest requete = new MockHttpServletRequest();
    private final Utilisateur utilisateur = new Utilisateur();

    @BeforeEach
    void preparer() {
        utilisateur.setId(UTILISATEUR_ID);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requete));
    }

    @AfterEach
    void nettoyer() {
        RequestContextHolder.resetRequestAttributes();
    }

    private MembrePharmacopee membre(Long id, Long pharmacopeeId, StatutPharmacopee statut) {
        Pharmacopee pharmacopee = Pharmacopee.builder().id(pharmacopeeId).nom("Officine de Bamako").statut(statut).build();
        return MembrePharmacopee.builder().id(id).pharmacopee(pharmacopee).statut(StatutMembre.ACTIF).build();
    }

    private void utilisateurConnecte() {
        when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(utilisateur);
    }

    @Test
    @DisplayName("l'en-tête désigne une pharmacopée dont l'utilisateur est membre actif")
    void enTeteValide() {
        utilisateurConnecte();
        requete.addHeader("X-Pharmacopee-Id", "7");
        MembrePharmacopee membre = membre(10L, 7L, StatutPharmacopee.VALIDEE);
        when(membrePharmacopeeRepository.findByUtilisateurIdAndPharmacopeeIdAndStatut(UTILISATEUR_ID, 7L, StatutMembre.ACTIF))
                .thenReturn(Optional.of(membre));
        when(rolePharmacopeePermissionRepository.findCodesPermissionsEffectives(UTILISATEUR_ID, 7L))
                .thenReturn(Set.of("VENTES_CREER"));

        ContexteMembre contexte = service.getContexte();

        assertThat(contexte.membre()).isSameAs(membre);
        assertThat(contexte.permissions()).containsExactly("VENTES_CREER");
    }

    @Test
    @DisplayName("un en-tête hors appartenance donne 403, sans repli sur une autre pharmacopée")
    void enTeteHorsAppartenance() {
        utilisateurConnecte();
        requete.addHeader("X-Pharmacopee-Id", "99");
        when(membrePharmacopeeRepository.findByUtilisateurIdAndPharmacopeeIdAndStatut(UTILISATEUR_ID, 99L, StatutMembre.ACTIF))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getContexte()).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("un en-tête non numérique donne 400")
    void enTeteNonNumerique() {
        utilisateurConnecte();
        requete.addHeader("X-Pharmacopee-Id", "abc");

        assertThatThrownBy(() -> service.getContexte()).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("sans en-tête, la première pharmacopée VALIDEE est retenue")
    void sansEnTetePremiereValidee() {
        utilisateurConnecte();
        MembrePharmacopee enAttente = membre(1L, 1L, StatutPharmacopee.EN_ATTENTE);
        MembrePharmacopee validee = membre(2L, 2L, StatutPharmacopee.VALIDEE);
        when(membrePharmacopeeRepository.findByUtilisateurIdAndStatut(UTILISATEUR_ID, StatutMembre.ACTIF))
                .thenReturn(List.of(enAttente, validee));
        when(rolePharmacopeePermissionRepository.findCodesPermissionsEffectives(UTILISATEUR_ID, 2L)).thenReturn(Set.of());

        assertThat(service.getContexte().membre()).isSameAs(validee);
    }

    @Test
    @DisplayName("sans appartenance active, l'accès est refusé")
    void sansAppartenance() {
        utilisateurConnecte();
        when(membrePharmacopeeRepository.findByUtilisateurIdAndStatut(UTILISATEUR_ID, StatutMembre.ACTIF)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getContexte()).isInstanceOf(ForbiddenException.class);
    }

    private void contexteAvec(StatutPharmacopee statut, Set<String> permissions) {
        utilisateurConnecte();
        when(membrePharmacopeeRepository.findByUtilisateurIdAndStatut(UTILISATEUR_ID, StatutMembre.ACTIF))
                .thenReturn(List.of(membre(1L, 5L, statut)));
        when(rolePharmacopeePermissionRepository.findCodesPermissionsEffectives(UTILISATEUR_ID, 5L)).thenReturn(permissions);
    }

    @Test
    @DisplayName("une permission détenue est acceptée")
    void permissionDetenue() {
        contexteAvec(StatutPharmacopee.VALIDEE, Set.of("STOCK_ENTREE"));

        assertThatCode(() -> service.verifierPermission("STOCK_ENTREE")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("une permission manquante donne 403")
    void permissionManquante() {
        contexteAvec(StatutPharmacopee.VALIDEE, Set.of("STOCK_CONSULTER"));

        assertThatThrownBy(() -> service.verifierPermission("STOCK_ENTREE")).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("une pharmacopée suspendue n'applique aucune permission")
    void pharmacopeeSuspendue() {
        contexteAvec(StatutPharmacopee.SUSPENDUE, Set.of("STOCK_ENTREE"));

        assertThatThrownBy(() -> service.verifierPermission("STOCK_ENTREE")).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("un abonnement ACTIF autorise les écritures")
    void ecritureAbonnementActif() {
        when(abonnementRepository.findEnVigueur(5L))
                .thenReturn(Optional.of(Abonnement.builder().statut(StatutAbonnement.ACTIF).build()));

        assertThatCode(() -> service.verifierEcritureAutorisee(5L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("en période de grâce, les écritures sont refusées")
    void ecritureEnGrace() {
        when(abonnementRepository.findEnVigueur(5L))
                .thenReturn(Optional.of(Abonnement.builder().statut(StatutAbonnement.EN_GRACE).build()));

        assertThatThrownBy(() -> service.verifierEcritureAutorisee(5L)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("sans abonnement en vigueur, les écritures sont refusées")
    void ecritureSansAbonnement() {
        when(abonnementRepository.findEnVigueur(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifierEcritureAutorisee(5L)).isInstanceOf(ForbiddenException.class);
    }
}
