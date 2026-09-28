package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.auth.PopulationAuthResponse;
import com.pharmacopee.ladafura.dto.population.auth.PopulationRegisterRequest;
import com.pharmacopee.ladafura.dto.population.auth.PopulationSyncRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.UnauthorizedException;
import com.pharmacopee.ladafura.mappers.PopulationAuthMapper;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.impl.PopulationAuthServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;

@ExtendWith(MockitoExtension.class)
class PopulationAuthServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private IFirebaseAuthService firebaseAuthService;

    @Spy
    private PopulationAuthMapper populationAuthMapper = new PopulationAuthMapper();

    @InjectMocks
    private PopulationAuthServiceImpl populationAuthService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("register - Succès de l'inscription autonome d'un citoyen")
    void register_success() {
        PopulationRegisterRequest request = PopulationRegisterRequest.builder()
                .nom("Diarra")
                .prenom("Fatoumata")
                .email("fatoumata.diarra@gmail.com")
                .motDePasse("Mali2026!")
                .telephone("+223 70 12 34 56")
                .build();

        when(utilisateurRepository.existsByEmail("fatoumata.diarra@gmail.com")).thenReturn(false);
        when(firebaseAuthService.createUser("fatoumata.diarra@gmail.com", "Mali2026!", "Fatoumata Diarra"))
                .thenReturn("firebase-uid-fatou-123");

        Utilisateur savedUser = new Utilisateur();
        savedUser.setId(15L);
        savedUser.setNom("Diarra");
        savedUser.setPrenom("Fatoumata");
        savedUser.setEmail("fatoumata.diarra@gmail.com");
        savedUser.setTelephone("+223 70 12 34 56");
        savedUser.setRole(Role.POPULATION);
        savedUser.setStatut(StatutUtilisateur.ACTIF);
        savedUser.setFirebaseUid("firebase-uid-fatou-123");
        savedUser.setDateCreation(LocalDateTime.now());

        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(savedUser);

        PopulationAuthResponse response = populationAuthService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getEmail()).isEqualTo("fatoumata.diarra@gmail.com");
        assertThat(response.getRole()).isEqualTo(Role.POPULATION);
        assertThat(response.getStatut()).isEqualTo(StatutUtilisateur.ACTIF);
        assertThat(response.getFirebaseUid()).isEqualTo("firebase-uid-fatou-123");

        verify(firebaseAuthService).createUser("fatoumata.diarra@gmail.com", "Mali2026!", "Fatoumata Diarra");
        verify(firebaseAuthService).setRole("firebase-uid-fatou-123", "POPULATION");
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("register - Échec si l'adresse email existe déjà (ConflictException)")
    void register_emailAlreadyExists_throwsConflict() {
        PopulationRegisterRequest request = PopulationRegisterRequest.builder()
                .nom("Diarra")
                .prenom("Fatoumata")
                .email("existant@gmail.com")
                .motDePasse("Mali2026!")
                .build();

        when(utilisateurRepository.existsByEmail("existant@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> populationAuthService.register(request))
                .isInstanceOf(ConflictException.class);

        verify(firebaseAuthService, never()).createUser(any(), any(), any());
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    @DisplayName("register - Échec si Firebase rejette la création (BadRequestException)")
    void register_firebaseFails_throwsBadRequest() {
        PopulationRegisterRequest request = PopulationRegisterRequest.builder()
                .nom("Diarra")
                .prenom("Fatoumata")
                .email("fatou@gmail.com")
                .motDePasse("Mali2026!")
                .build();

        when(utilisateurRepository.existsByEmail("fatou@gmail.com")).thenReturn(false);
        when(firebaseAuthService.createUser(any(), any(), any()))
                .thenThrow(new RuntimeException("Firebase network error"));

        assertThatThrownBy(() -> populationAuthService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Firebase network error");

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCurrentPopulationUser - Succès si l'utilisateur est authentifié avec le rôle POPULATION")
    void getCurrentPopulationUser_success() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("fatoumata.diarra@gmail.com", "token", List.of(new SimpleGrantedAuthority("ROLE_POPULATION")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Utilisateur user = new Utilisateur();
        user.setId(15L);
        user.setEmail("fatoumata.diarra@gmail.com");
        user.setRole(Role.POPULATION);
        user.setStatut(StatutUtilisateur.ACTIF);

        when(utilisateurRepository.findByEmail("fatoumata.diarra@gmail.com")).thenReturn(Optional.of(user));

        Utilisateur result = populationAuthService.getCurrentPopulationUser();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(15L);
        assertThat(result.getRole()).isEqualTo(Role.POPULATION);
    }

    @Test
    @DisplayName("getCurrentPopulationUser - Erreur 403 si l'utilisateur a un rôle différent (ex: AGENT_COLLECTE)")
    void getCurrentPopulationUser_wrongRole_throwsForbidden() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("agent@ladafura.ml", "token", List.of(new SimpleGrantedAuthority("ROLE_AGENT_COLLECTE")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Utilisateur user = new Utilisateur();
        user.setId(20L);
        user.setEmail("agent@ladafura.ml");
        user.setRole(Role.AGENT_COLLECTE);
        user.setStatut(StatutUtilisateur.ACTIF);

        when(utilisateurRepository.findByEmail("agent@ladafura.ml")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> populationAuthService.getCurrentPopulationUser())
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Accès réservé exclusivement aux utilisateurs du rôle POPULATION");
    }

    @Test
    @DisplayName("getCurrentPopulationUser - Erreur 403 si le compte est inactif")
    void getCurrentPopulationUser_inactive_throwsForbidden() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("inactive@gmail.com", "token", List.of(new SimpleGrantedAuthority("ROLE_POPULATION")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Utilisateur user = new Utilisateur();
        user.setId(22L);
        user.setEmail("inactive@gmail.com");
        user.setRole(Role.POPULATION);
        user.setStatut(StatutUtilisateur.INACTIF);

        when(utilisateurRepository.findByEmail("inactive@gmail.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> populationAuthService.getCurrentPopulationUser())
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("inactif ou désactivé");
    }

    @Test
    @DisplayName("getCurrentPopulationUser - Erreur 401 si non authentifié")
    void getCurrentPopulationUser_unauthenticated_throwsUnauthorized() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> populationAuthService.getCurrentPopulationUser())
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("getMe - Retourne les informations de session")
    void getMe_success() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("fatoumata.diarra@gmail.com", "token", List.of(new SimpleGrantedAuthority("ROLE_POPULATION")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Utilisateur user = new Utilisateur();
        user.setId(15L);
        user.setNom("Diarra");
        user.setPrenom("Fatoumata");
        user.setEmail("fatoumata.diarra@gmail.com");
        user.setTelephone("+223 70 12 34 56");
        user.setRole(Role.POPULATION);
        user.setStatut(StatutUtilisateur.ACTIF);
        user.setFirebaseUid("uid-123");
        user.setDateCreation(LocalDateTime.now());

        when(utilisateurRepository.findByEmail("fatoumata.diarra@gmail.com")).thenReturn(Optional.of(user));

        PopulationAuthResponse me = populationAuthService.getMe();

        assertThat(me).isNotNull();
        assertThat(me.getId()).isEqualTo(15L);
        assertThat(me.getNom()).isEqualTo("Diarra");
        assertThat(me.getEmail()).isEqualTo("fatoumata.diarra@gmail.com");
        assertThat(me.getRole()).isEqualTo(Role.POPULATION);
    }

    @Test
    @DisplayName("syncFirebaseUser - Synchronisation d'un nouvel utilisateur direct Firebase")
    void syncFirebaseUser_newAccount() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("google_uid_999", "token", List.of(new SimpleGrantedAuthority("ROLE_POPULATION")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(utilisateurRepository.findByEmail("google_uid_999")).thenReturn(Optional.empty());
        when(utilisateurRepository.findByFirebaseUid("google_uid_999")).thenReturn(Optional.empty());

        Utilisateur saved = new Utilisateur();
        saved.setId(30L);
        saved.setNom("Sanogo");
        saved.setPrenom("Ibrahim");
        saved.setEmail("google_uid_999@firebase.ladafura.ml");
        saved.setFirebaseUid("google_uid_999");
        saved.setRole(Role.POPULATION);
        saved.setStatut(StatutUtilisateur.ACTIF);

        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(saved);

        PopulationSyncRequest request = PopulationSyncRequest.builder()
                .nom("Sanogo")
                .prenom("Ibrahim")
                .telephone("+223 66 11 22 33")
                .build();

        PopulationAuthResponse result = populationAuthService.syncFirebaseUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(30L);
        assertThat(result.getRole()).isEqualTo(Role.POPULATION);
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }
}
