package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Praticien;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.auth.PharmacopeeAuthResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.auth.PraticienRegisterRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.AttributionRolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.LocalisationRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PraticienRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeePermissionRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeAuthServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeInitialisationService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

@ExtendWith(MockitoExtension.class)
class PharmacopeeAuthServiceImplTest {

    private static final Long UTILISATEUR_ID = 1L;
    private static final String EMAIL = "gerant@officine.ml";

    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private PharmacopeeRepository pharmacopeeRepository;
    @Mock private PraticienRepository praticienRepository;
    @Mock private LocalisationRepository localisationRepository;
    @Mock private MembrePharmacopeeRepository membreRepository;
    @Mock private AttributionRolePharmacopeeRepository attributionRepository;
    @Mock private RolePharmacopeePermissionRepository rolePermissionRepository;
    @Mock private AbonnementRepository abonnementRepository;
    @Mock private IPharmacopeeQuotaService quotaService;
    @Mock private IPharmacopeeInitialisationService initialisationService;
    @Mock private IFirebaseAuthService firebaseAuthService;

    @InjectMocks private PharmacopeeAuthServiceImpl service;

    private final MockHttpServletRequest requete = new MockHttpServletRequest();
    private final Utilisateur utilisateur = new Utilisateur();

    @BeforeEach
    void preparer() {
        utilisateur.setId(UTILISATEUR_ID);
        utilisateur.setEmail(EMAIL);
        utilisateur.setRole(Role.PHARMACOPEE);
        utilisateur.setStatut(StatutUtilisateur.ACTIF);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requete));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, "jeton", List.of()));
        lenient().when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utilisateur));
    }

    @AfterEach
    void nettoyer() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    private MembrePharmacopee membre(Long id, Long pharmacopeeId, StatutPharmacopee statut) {
        Pharmacopee pharmacopee = Pharmacopee.builder().id(pharmacopeeId).nom("Officine de Bamako").statut(statut).build();
        return MembrePharmacopee.builder().id(id).utilisateur(utilisateur).pharmacopee(pharmacopee)
                .statut(StatutMembre.ACTIF).estProprietaire(true).build();
    }

    @Test
    @DisplayName("l'en-tête désigne une pharmacopée dont l'utilisateur est membre actif")
    void enTeteValide() {
        requete.addHeader("X-Pharmacopee-Id", "7");
        MembrePharmacopee membre = membre(10L, 7L, StatutPharmacopee.VALIDEE);
        when(membreRepository.findByUtilisateurIdAndPharmacopeeIdAndStatut(UTILISATEUR_ID, 7L, StatutMembre.ACTIF))
                .thenReturn(Optional.of(membre));

        assertThat(service.getMembreCourant()).isSameAs(membre);
        assertThat(service.getCurrentPharmacopee().getId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("un en-tête hors appartenance donne 403, sans repli sur une autre pharmacopée")
    void enTeteHorsAppartenance() {
        requete.addHeader("X-Pharmacopee-Id", "99");
        when(membreRepository.findByUtilisateurIdAndPharmacopeeIdAndStatut(UTILISATEUR_ID, 99L, StatutMembre.ACTIF))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMembreCourant()).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("un en-tête non numérique donne 400")
    void enTeteNonNumerique() {
        requete.addHeader("X-Pharmacopee-Id", "abc");

        assertThatThrownBy(() -> service.getMembreCourant()).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("sans en-tête, la première pharmacopée VALIDEE est retenue")
    void sansEnTetePremiereValidee() {
        MembrePharmacopee enAttente = membre(1L, 1L, StatutPharmacopee.EN_ATTENTE);
        MembrePharmacopee validee = membre(2L, 2L, StatutPharmacopee.VALIDEE);
        when(membreRepository.findByUtilisateurIdAndStatut(UTILISATEUR_ID, StatutMembre.ACTIF))
                .thenReturn(List.of(enAttente, validee));

        assertThat(service.getMembreCourant()).isSameAs(validee);
    }

    @Test
    @DisplayName("sans appartenance active, l'accès est refusé")
    void sansAppartenance() {
        when(membreRepository.findByUtilisateurIdAndStatut(UTILISATEUR_ID, StatutMembre.ACTIF)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getMembreCourant()).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("un compte citoyen ayant une adhésion en cours accède à l'espace pharmacopée")
    void citoyenAvecAdhesion() {
        utilisateur.setRole(Role.POPULATION);
        when(membreRepository.existsByUtilisateurIdAndStatutIn(anyLong(), any())).thenReturn(true);

        assertThatCode(() -> service.getCurrentUtilisateur()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("un compte citoyen sans adhésion reste refusé")
    void citoyenSansAdhesion() {
        utilisateur.setRole(Role.POPULATION);
        when(membreRepository.existsByUtilisateurIdAndStatutIn(anyLong(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.getCurrentUtilisateur()).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("/me ignore un en-tête périmé et expose droits et abonnement")
    void meAvecEnTetePerime() {
        requete.addHeader("X-Pharmacopee-Id", "99");
        MembrePharmacopee membre = membre(10L, 7L, StatutPharmacopee.VALIDEE);
        when(membreRepository.findByUtilisateurIdAndStatut(UTILISATEUR_ID, StatutMembre.ACTIF)).thenReturn(List.of(membre));
        when(quotaService.maxPharmacopeesParGerant(UTILISATEUR_ID)).thenReturn(2);
        when(quotaService.pharmacopeesPossedees(UTILISATEUR_ID)).thenReturn(1L);
        com.pharmacopee.ladafura.Models.RolePharmacopee role = new com.pharmacopee.ladafura.Models.RolePharmacopee();
        role.setCode("GERANT");
        role.setLibelle("Gérant");
        com.pharmacopee.ladafura.Models.AttributionRolePharmacopee attribution = new com.pharmacopee.ladafura.Models.AttributionRolePharmacopee();
        attribution.setRolePharmacopee(role);
        when(attributionRepository.findByMembrePharmacopeeId(10L)).thenReturn(List.of(attribution));
        when(rolePermissionRepository.findCodesPermissionsEffectives(UTILISATEUR_ID, 7L)).thenReturn(Set.of("VENTES_CREER"));
        when(abonnementRepository.findEnVigueur(7L)).thenReturn(Optional.of(
                Abonnement.builder().statut(StatutAbonnement.ACTIF).codeOffreSouscrite("GRATUITE").build()));

        PharmacopeeAuthResponse me = service.getMe();

        assertThat(me.getPharmacopeeId()).isEqualTo(7L);
        assertThat(me.getMembreId()).isEqualTo(10L);
        assertThat(me.getRoles()).containsExactly("GERANT");
        assertThat(me.getRoleLibelle()).isEqualTo("Gérant");
        assertThat(me.getPermissions()).containsExactly("VENTES_CREER");
        assertThat(me.getStatutAbonnement()).isEqualTo("ACTIF");
        assertThat(me.getPlanAbonnement()).isEqualTo("GRATUIT");
        assertThat(me.isEstPraticienPrincipal()).isTrue();
        assertThat(me.isPeutCreerStructure()).isTrue();
    }

    @Test
    @DisplayName("inscription : le compte Firebase est supprimé si l'initialisation échoue")
    void inscriptionEchoueeSupprimeLeCompteFirebase() {
        PraticienRegisterRequest demande = new PraticienRegisterRequest();
        demande.setEmail("nouveau@officine.ml");
        demande.setNom("Traoré");
        demande.setPrenom("Awa");
        demande.setMotDePasse("MotDePasse1!");
        demande.setNomPharmacopee("Officine de Ségou");
        when(firebaseAuthService.createUser(anyString(), anyString(), anyString())).thenReturn("uid-1");
        when(praticienRepository.saveAndFlush(any(Praticien.class))).thenAnswer(appel -> appel.getArgument(0));
        when(pharmacopeeRepository.saveAndFlush(any(Pharmacopee.class))).thenAnswer(appel -> appel.getArgument(0));
        doThrow(new IllegalStateException("offre gratuite absente")).when(initialisationService).initialiser(any(), any());

        assertThatThrownBy(() -> service.registerPraticien(demande)).isInstanceOf(IllegalStateException.class);

        verify(firebaseAuthService).deleteUser("uid-1");
    }
}
