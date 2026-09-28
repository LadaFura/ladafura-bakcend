package com.pharmacopee.ladafura.services;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.dto.agent.media.AgentMediaResponse;
import com.pharmacopee.ladafura.dto.agent.media.AgentMediaUpdateRequest;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.services.impl.AgentMediaServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;
import com.pharmacopee.ladafura.services.storage.IFileStorageService;

@ExtendWith(MockitoExtension.class)
class AgentMediaServiceImplTest {

    @Mock
    private CollecteRepository collecteRepository;

    @Mock
    private IFileStorageService fileStorageService;

    @Mock
    private IAgentAuthService agentAuthService;

    @InjectMocks
    private AgentMediaServiceImpl agentMediaService;

    private AgentCollecte agentConnecte;
    private Collecte collecteBrouillon;

    @BeforeEach
    void setUp() {
        agentConnecte = new AgentCollecte();
        agentConnecte.setId(10L);
        agentConnecte.setNom("Coulibaly");
        agentConnecte.setPrenom("Oumar");

        collecteBrouillon = Collecte.builder()
                .id(100L)
                .statut(StatutCollecte.BROUILLON)
                .agentCollecte(agentConnecte)
                .photoUrl(null)
                .audioUrl(null)
                .build();
    }

    @Test
    @DisplayName("Associer des URLs de médias directement à une collecte")
    void associerMediasUrls_Succes() {
        AgentMediaUpdateRequest request = AgentMediaUpdateRequest.builder()
                .photoUrl("https://storage.ladafura.ml/photos/plante.jpg")
                .audioUrl("https://storage.ladafura.ml/audios/temoignage.mp3")
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(collecteRepository.save(any(Collecte.class))).thenReturn(collecteBrouillon);

        AgentMediaResponse response = agentMediaService.associerMediasUrls(100L, request);

        assertThat(response).isNotNull();
        assertThat(collecteBrouillon.getPhotoUrl()).isEqualTo("https://storage.ladafura.ml/photos/plante.jpg");
        assertThat(collecteBrouillon.getAudioUrl()).isEqualTo("https://storage.ladafura.ml/audios/temoignage.mp3");
        verify(collecteRepository).save(collecteBrouillon);
    }

    @Test
    @DisplayName("Téléverser une photo d'échantillon avec succès")
    void uploadPhoto_Succes() {
        MockMultipartFile file = new MockMultipartFile("file", "kinkeliba.jpg", "image/jpeg", "image-content".getBytes());

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(fileStorageService.storeFile(eq(file), eq("collectes/photos"), anyList(), anyLong()))
                .thenReturn("/uploads/collectes/photos/unique_kinkeliba.jpg");
        when(collecteRepository.save(any(Collecte.class))).thenReturn(collecteBrouillon);

        AgentMediaResponse response = agentMediaService.uploadPhoto(100L, file);

        assertThat(response).isNotNull();
        assertThat(collecteBrouillon.getPhotoUrl()).isEqualTo("/uploads/collectes/photos/unique_kinkeliba.jpg");
        assertThat(response.isPhotoPresente()).isTrue();
        verify(fileStorageService).storeFile(eq(file), eq("collectes/photos"), anyList(), anyLong());
    }

    @Test
    @DisplayName("Téléverser un fichier audio de témoignage avec succès")
    void uploadAudio_Succes() {
        MockMultipartFile file = new MockMultipartFile("file", "temoignage.mp3", "audio/mpeg", "audio-content".getBytes());

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(fileStorageService.storeFile(eq(file), eq("collectes/audios"), anyList(), anyLong()))
                .thenReturn("/uploads/collectes/audios/unique_temoignage.mp3");
        when(collecteRepository.save(any(Collecte.class))).thenReturn(collecteBrouillon);

        AgentMediaResponse response = agentMediaService.uploadAudio(100L, file);

        assertThat(response).isNotNull();
        assertThat(collecteBrouillon.getAudioUrl()).isEqualTo("/uploads/collectes/audios/unique_temoignage.mp3");
        assertThat(response.isAudioPresent()).isTrue();
        verify(fileStorageService).storeFile(eq(file), eq("collectes/audios"), anyList(), anyLong());
    }

    @Test
    @DisplayName("Consulter les médias d'une fiche de collecte")
    void getMediasByCollecte_Succes() {
        collecteBrouillon.setPhotoUrl("/uploads/photo.jpg");
        collecteBrouillon.setAudioUrl("/uploads/audio.mp3");

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        AgentMediaResponse response = agentMediaService.getMediasByCollecte(100L);

        assertThat(response).isNotNull();
        assertThat(response.getPhotoUrl()).isEqualTo("/uploads/photo.jpg");
        assertThat(response.getAudioUrl()).isEqualTo("/uploads/audio.mp3");
        assertThat(response.isPhotoPresente()).isTrue();
        assertThat(response.isAudioPresent()).isTrue();
    }

    @Test
    @DisplayName("Supprimer la photo d'une collecte")
    void supprimerPhoto_Succes() {
        collecteBrouillon.setPhotoUrl("/uploads/collectes/photos/old.jpg");

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(collecteRepository.save(any(Collecte.class))).thenReturn(collecteBrouillon);

        AgentMediaResponse response = agentMediaService.supprimerPhoto(100L);

        assertThat(collecteBrouillon.getPhotoUrl()).isNull();
        verify(fileStorageService).deleteFile("/uploads/collectes/photos/old.jpg");
        verify(collecteRepository).save(collecteBrouillon);
    }

    @Test
    @DisplayName("Supprimer l'audio d'une collecte")
    void supprimerAudio_Succes() {
        collecteBrouillon.setAudioUrl("/uploads/collectes/audios/old.mp3");

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(collecteRepository.save(any(Collecte.class))).thenReturn(collecteBrouillon);

        AgentMediaResponse response = agentMediaService.supprimerAudio(100L);

        assertThat(collecteBrouillon.getAudioUrl()).isNull();
        verify(fileStorageService).deleteFile("/uploads/collectes/audios/old.mp3");
        verify(collecteRepository).save(collecteBrouillon);
    }

    @Test
    @DisplayName("Associer des médias : rejet si la collecte appartient à un autre agent (403)")
    void associerMediasUrls_CollecteNonProprietaire_ForbiddenException() {
        AgentCollecte autreAgent = new AgentCollecte();
        autreAgent.setId(99L);
        collecteBrouillon.setAgentCollecte(autreAgent);

        AgentMediaUpdateRequest request = AgentMediaUpdateRequest.builder().photoUrl("url").build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        assertThatThrownBy(() -> agentMediaService.associerMediasUrls(100L, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("cette collecte ne vous appartient pas");

        verify(collecteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Téléverser photo : rejet si la collecte est déjà validée (400)")
    void uploadPhoto_CollecteVerrouillee_BadRequestException() {
        collecteBrouillon.setStatut(StatutCollecte.VALIDEE);
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "bytes".getBytes());

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        assertThatThrownBy(() -> agentMediaService.uploadPhoto(100L, file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Impossible de modifier les médias d'une collecte avec le statut VALIDEE");

        verify(fileStorageService, never()).storeFile(any(), any(), any(), anyLong());
    }
}
