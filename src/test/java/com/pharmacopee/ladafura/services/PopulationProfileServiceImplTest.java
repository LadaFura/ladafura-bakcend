package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.profil.PopulationProfileResponse;
import com.pharmacopee.ladafura.dto.population.profil.PopulationUpdateProfileRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.mappers.PopulationProfileMapper;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.FavoriRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.impl.PopulationProfileServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

@ExtendWith(MockitoExtension.class)
class PopulationProfileServiceImplTest {

    @Mock
    private IPopulationAuthService populationAuthService;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private FavoriRepository favoriRepository;

    @Spy
    private PopulationProfileMapper populationProfileMapper = new PopulationProfileMapper();

    @InjectMocks
    private PopulationProfileServiceImpl populationProfileService;

    private Utilisateur currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new Utilisateur();
        currentUser.setId(15L);
        currentUser.setNom("Diarra");
        currentUser.setPrenom("Fatoumata");
        currentUser.setEmail("fatoumata.diarra@gmail.com");
        currentUser.setTelephone("+223 70 12 34 56");
        currentUser.setRole(Role.POPULATION);
        currentUser.setStatut(StatutUtilisateur.ACTIF);
        currentUser.setFirebaseUid("uid-firebase-fatou");
        currentUser.setDateCreation(LocalDateTime.now());
    }

    @Test
    @DisplayName("getProfile - Succès de la consultation du profil et des compteurs d'activité")
    void getProfile_success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.countByUtilisateurId(15L)).thenReturn(4L);
        when(favoriRepository.countByUtilisateurId(15L)).thenReturn(6L);

        PopulationProfileResponse response = populationProfileService.getProfile();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getNom()).isEqualTo("Diarra");
        assertThat(response.getPrenom()).isEqualTo("Fatoumata");
        assertThat(response.getEmail()).isEqualTo("fatoumata.diarra@gmail.com");
        assertThat(response.getRole()).isEqualTo(Role.POPULATION);
        assertThat(response.getStatut()).isEqualTo(StatutUtilisateur.ACTIF);
        assertThat(response.getNombreTotalCommandes()).isEqualTo(4L);
        assertThat(response.getNombreTotalFavoris()).isEqualTo(6L);

        verify(populationAuthService).getCurrentPopulationUser();
        verify(commandeRepository).countByUtilisateurId(15L);
        verify(favoriRepository).countByUtilisateurId(15L);
    }

    @Test
    @DisplayName("updateProfile - Mise à jour réussie des coordonnées sans altération du rôle ou du statut")
    void updateProfile_success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);

        PopulationUpdateProfileRequest request = PopulationUpdateProfileRequest.builder()
                .nom("Traoré")
                .prenom("Awa")
                .telephone("+223 75 99 88 77")
                .build();

        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commandeRepository.countByUtilisateurId(15L)).thenReturn(4L);
        when(favoriRepository.countByUtilisateurId(15L)).thenReturn(6L);

        PopulationProfileResponse response = populationProfileService.updateProfile(request);

        assertThat(response).isNotNull();
        assertThat(response.getNom()).isEqualTo("Traoré");
        assertThat(response.getPrenom()).isEqualTo("Awa");
        assertThat(response.getTelephone()).isEqualTo("+223 75 99 88 77");
        // Vérification de la stricte inviolabilité du rôle et du statut
        assertThat(response.getRole()).isEqualTo(Role.POPULATION);
        assertThat(response.getStatut()).isEqualTo(StatutUtilisateur.ACTIF);
        assertThat(response.getEmail()).isEqualTo("fatoumata.diarra@gmail.com");

        verify(utilisateurRepository).save(currentUser);
    }
}
