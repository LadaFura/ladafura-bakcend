package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.admin.avis.AdminAvisResponse;
import com.pharmacopee.ladafura.dto.admin.avis.AdminModerateAvisRequest;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AdminAvisMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.services.impl.AdminAvisServiceImpl;

@ExtendWith(MockitoExtension.class)
class AdminAvisServiceImplTest {

    @Mock
    private AvisRepository avisRepository;

    @Spy
    private AdminAvisMapper avisMapper = new AdminAvisMapper();

    @InjectMocks
    private AdminAvisServiceImpl avisService;

    private Avis testAvis;
    private Utilisateur testUser;
    private Pharmacopee testPharmacopee;

    @BeforeEach
    void setUp() {
        testUser = new Utilisateur();
        testUser.setId(10L);
        testUser.setNom("Traoré");
        testUser.setPrenom("Fatoumata");
        testUser.setEmail("fatou@ladafura.ml");

        testPharmacopee = Pharmacopee.builder()
                .id(5L)
                .nom("Danaya Tradithérapie")
                .build();

        testAvis = Avis.builder()
                .id(1L)
                .utilisateur(testUser)
                .pharmacopee(testPharmacopee)
                .note(4)
                .commentaire("Accueil chaleureux et service rapide")
                .statut(StatutAvis.EN_ATTENTE)
                .dateAvis(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getAllAvis - Filtrage par statut")
    void getAllAvis_ByStatut() {
        Pageable pageable = PageRequest.of(0, 10);
        when(avisRepository.findByStatut(StatutAvis.EN_ATTENTE, pageable))
                .thenReturn(new PageImpl<>(List.of(testAvis)));

        Page<AdminAvisResponse> result = avisService.getAllAvis(StatutAvis.EN_ATTENTE, null, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getNomCompletUtilisateur()).isEqualTo("Fatoumata Traoré");
        assertThat(result.getContent().get(0).getNomPharmacopee()).isEqualTo("Danaya Tradithérapie");
    }

    @Test
    @DisplayName("getAllAvis - Filtrage par pharmacopée")
    void getAllAvis_ByPharmacopee() {
        Pageable pageable = PageRequest.of(0, 10);
        when(avisRepository.findByPharmacopeeId(5L, pageable))
                .thenReturn(new PageImpl<>(List.of(testAvis)));

        Page<AdminAvisResponse> result = avisService.getAllAvis(null, 5L, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getPharmacopeeId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getAllAvis - Sans filtre (tous les avis)")
    void getAllAvis_All() {
        Pageable pageable = PageRequest.of(0, 10);
        when(avisRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(testAvis)));

        Page<AdminAvisResponse> result = avisService.getAllAvis(null, null, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("moderateAvis - Succès")
    void moderateAvis_Success() {
        AdminModerateAvisRequest request = AdminModerateAvisRequest.builder()
                .action(StatutAvis.PUBLIE)
                .motif("Avis conforme")
                .build();

        when(avisRepository.findById(1L)).thenReturn(Optional.of(testAvis));
        when(avisRepository.save(any(Avis.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminAvisResponse response = avisService.moderateAvis(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatut()).isEqualTo(StatutAvis.PUBLIE);
        verify(avisRepository).save(testAvis);
    }

    @Test
    @DisplayName("moderateAvis - Introuvable lance ResourceNotFoundException")
    void moderateAvis_NotFound() {
        AdminModerateAvisRequest request = AdminModerateAvisRequest.builder()
                .action(StatutAvis.PUBLIE)
                .build();

        when(avisRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avisService.moderateAvis(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteAvis - Succès")
    void deleteAvis_Success() {
        when(avisRepository.findById(1L)).thenReturn(Optional.of(testAvis));

        avisService.deleteAvis(1L);

        verify(avisRepository).delete(testAvis);
    }

    @Test
    @DisplayName("deleteAvis - Introuvable lance ResourceNotFoundException")
    void deleteAvis_NotFound() {
        when(avisRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avisService.deleteAvis(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
