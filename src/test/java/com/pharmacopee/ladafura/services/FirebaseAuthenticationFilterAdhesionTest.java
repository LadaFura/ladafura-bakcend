package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.firebase.auth.FirebaseToken;
import com.pharmacopee.ladafura.configs.security.FirebaseAuthenticationFilter;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;

@ExtendWith(MockitoExtension.class)
class FirebaseAuthenticationFilterAdhesionTest {

    private static final String URI_PHARMACOPEE = "/api/v1/pharmacopee/invitations";

    @Mock private IFirebaseAuthService firebaseAuthService;
    @Mock private ObjectProvider<UtilisateurRepository> utilisateurRepositoryProvider;
    @Mock private ObjectProvider<MembrePharmacopeeRepository> membreRepositoryProvider;
    @Mock private MembrePharmacopeeRepository membreRepository;

    private FirebaseAuthenticationFilter filtre;

    private final FirebaseToken jeton = mock(FirebaseToken.class);

    @BeforeEach
    void preparer() throws Exception {
        // Construction explicite : Mockito ne distingue pas les deux ObjectProvider par leur type générique.
        filtre = new FirebaseAuthenticationFilter(firebaseAuthService, utilisateurRepositoryProvider, membreRepositoryProvider);
        when(firebaseAuthService.verifyIdToken(anyString())).thenReturn(jeton);
        lenient().when(jeton.getUid()).thenReturn("uid-citoyen");
        lenient().when(jeton.getEmail()).thenReturn("citoyen@ladafura.ml");
        lenient().when(membreRepositoryProvider.getIfAvailable()).thenReturn(membreRepository);
    }

    @AfterEach
    void nettoyer() {
        SecurityContextHolder.clearContext();
    }

    private void filtrer(String uri, String roleClaim) throws Exception {
        when(jeton.getClaims()).thenReturn(Map.of("role", roleClaim));
        MockHttpServletRequest requete = new MockHttpServletRequest("GET", uri);
        requete.addHeader("Authorization", "Bearer a.b.c");
        filtre.doFilter(requete, new MockHttpServletResponse(), new MockFilterChain());
    }

    private static java.util.List<String> autorites() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
    }

    @Test
    @DisplayName("un citoyen ayant une adhésion en cours reçoit l'accès à l'espace pharmacopée et garde son rôle")
    void citoyenAvecAdhesion() throws Exception {
        when(membreRepository.existsByUtilisateur_FirebaseUidAndStatutIn(anyString(), any())).thenReturn(true);

        filtrer(URI_PHARMACOPEE, "POPULATION");

        assertThat(autorites()).containsExactlyInAnyOrder("ROLE_POPULATION", "ROLE_PHARMACOPEE");
    }

    @Test
    @DisplayName("un citoyen sans adhésion ne reçoit pas l'accès")
    void citoyenSansAdhesion() throws Exception {
        filtrer(URI_PHARMACOPEE, "POPULATION");

        assertThat(autorites()).containsExactly("ROLE_POPULATION");
    }

    @Test
    @DisplayName("hors espace pharmacopée, aucune adhésion n'est recherchée")
    void horsEspacePharmacopee() throws Exception {
        filtrer("/api/v1/population/commandes", "POPULATION");

        assertThat(autorites()).containsExactly("ROLE_POPULATION");
        verify(membreRepository, never()).existsByUtilisateur_FirebaseUidAndStatutIn(anyString(), any());
    }

    @Test
    @DisplayName("un compte déjà PHARMACOPEE n'ajoute pas de requête")
    void compteDejaPharmacopee() throws Exception {
        filtrer(URI_PHARMACOPEE, "PHARMACOPEE");

        assertThat(autorites()).containsExactly("ROLE_PHARMACOPEE");
        verify(membreRepository, never()).existsByUtilisateur_FirebaseUidAndStatutIn(anyString(), any());
    }

    @Test
    @DisplayName("une erreur de lecture des adhésions n'accorde aucun accès")
    void erreurDeLecture() throws Exception {
        when(membreRepository.existsByUtilisateur_FirebaseUidAndStatutIn(anyString(), any()))
                .thenThrow(new IllegalStateException("base indisponible"));

        filtrer(URI_PHARMACOPEE, "POPULATION");

        assertThat(autorites()).containsExactly("ROLE_POPULATION");
    }
}
