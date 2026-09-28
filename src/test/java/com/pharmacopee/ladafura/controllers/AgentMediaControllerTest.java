package com.pharmacopee.ladafura.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.pharmacopee.ladafura.controllers.agent.AgentMediaController;
import com.pharmacopee.ladafura.dto.agent.media.AgentMediaResponse;
import com.pharmacopee.ladafura.dto.agent.media.AgentMediaUpdateRequest;
import com.pharmacopee.ladafura.services.interfaces.IAgentMediaService;

@ExtendWith(MockitoExtension.class)
class AgentMediaControllerTest {

    @Mock
    private IAgentMediaService agentMediaService;

    @InjectMocks
    private AgentMediaController agentMediaController;

    @Test
    @DisplayName("POST /api/v1/agent/collectes/{collecteId}/medias/photo - 200 OK")
    void uploadPhoto_Ok() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());
        AgentMediaResponse responseDto = AgentMediaResponse.builder()
                .collecteId(100L)
                .photoUrl("/uploads/collectes/photos/unique.jpg")
                .photoPresente(true)
                .build();

        when(agentMediaService.uploadPhoto(100L, file)).thenReturn(responseDto);

        ResponseEntity<AgentMediaResponse> response = agentMediaController.uploadPhoto(100L, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPhotoUrl()).isEqualTo("/uploads/collectes/photos/unique.jpg");
    }

    @Test
    @DisplayName("POST /api/v1/agent/collectes/{collecteId}/medias/audio - 200 OK")
    void uploadAudio_Ok() {
        MockMultipartFile file = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", "content".getBytes());
        AgentMediaResponse responseDto = AgentMediaResponse.builder()
                .collecteId(100L)
                .audioUrl("/uploads/collectes/audios/unique.mp3")
                .audioPresent(true)
                .build();

        when(agentMediaService.uploadAudio(100L, file)).thenReturn(responseDto);

        ResponseEntity<AgentMediaResponse> response = agentMediaController.uploadAudio(100L, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAudioUrl()).isEqualTo("/uploads/collectes/audios/unique.mp3");
    }

    @Test
    @DisplayName("PUT /api/v1/agent/collectes/{collecteId}/medias - 200 OK")
    void associerMediasUrls_Ok() {
        AgentMediaUpdateRequest request = AgentMediaUpdateRequest.builder()
                .photoUrl("https://storage/photo.jpg")
                .build();

        AgentMediaResponse responseDto = AgentMediaResponse.builder()
                .collecteId(100L)
                .photoUrl("https://storage/photo.jpg")
                .build();

        when(agentMediaService.associerMediasUrls(100L, request)).thenReturn(responseDto);

        ResponseEntity<AgentMediaResponse> response = agentMediaController.associerMediasUrls(100L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPhotoUrl()).isEqualTo("https://storage/photo.jpg");
    }

    @Test
    @DisplayName("GET /api/v1/agent/collectes/{collecteId}/medias - 200 OK")
    void getMediasByCollecte_Ok() {
        AgentMediaResponse responseDto = AgentMediaResponse.builder()
                .collecteId(100L)
                .photoUrl("photo.jpg")
                .audioUrl("audio.mp3")
                .build();

        when(agentMediaService.getMediasByCollecte(100L)).thenReturn(responseDto);

        ResponseEntity<AgentMediaResponse> response = agentMediaController.getMediasByCollecte(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("DELETE /api/v1/agent/collectes/{collecteId}/medias/photo - 200 OK")
    void supprimerPhoto_Ok() {
        AgentMediaResponse responseDto = AgentMediaResponse.builder().collecteId(100L).photoUrl(null).build();
        when(agentMediaService.supprimerPhoto(100L)).thenReturn(responseDto);

        ResponseEntity<AgentMediaResponse> response = agentMediaController.supprimerPhoto(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(agentMediaService).supprimerPhoto(100L);
    }

    @Test
    @DisplayName("DELETE /api/v1/agent/collectes/{collecteId}/medias/audio - 200 OK")
    void supprimerAudio_Ok() {
        AgentMediaResponse responseDto = AgentMediaResponse.builder().collecteId(100L).audioUrl(null).build();
        when(agentMediaService.supprimerAudio(100L)).thenReturn(responseDto);

        ResponseEntity<AgentMediaResponse> response = agentMediaController.supprimerAudio(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(agentMediaService).supprimerAudio(100L);
    }
}
