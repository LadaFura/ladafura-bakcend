package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.AttributionRolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeMembreServiceImpl;

@ExtendWith(MockitoExtension.class)
class PharmacopeeMembreAdminLectureTest {

    private static final Pageable PAGE = PageRequest.of(0, 20);

    @Mock private PharmacopeeRepository pharmacopeeRepository;
    @Mock private MembrePharmacopeeRepository membreRepository;
    @Mock private AttributionRolePharmacopeeRepository attributionRepository;

    @InjectMocks private PharmacopeeMembreServiceImpl service;

    @Test
    @DisplayName("GET /admin/pharmacopees/{id}/membres - 200 OK")
    void membresDeLaPharmacopee() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(3L);
        utilisateur.setNom("Diarra");
        MembrePharmacopee membre = MembrePharmacopee.builder().id(9L).utilisateur(utilisateur).statut(StatutMembre.ACTIF).estProprietaire(true).build();
        when(pharmacopeeRepository.existsById(1L)).thenReturn(true);
        when(membreRepository.findByPharmacopeeId(1L, PAGE)).thenReturn(new PageImpl<>(List.of(membre)));
        when(attributionRepository.findByMembrePharmacopeeIdIn(any())).thenReturn(List.of());

        assertThat(service.getMembresParAdmin(1L, PAGE).getContent()).singleElement()
                .satisfies(m -> assertThat(m.getId()).isEqualTo(9L));
    }

    @Test
    @DisplayName("GET /admin/pharmacopees/{id}/membres - 404 si la pharmacopée n'existe pas")
    void pharmacopeeInconnue() {
        when(pharmacopeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.getMembresParAdmin(99L, PAGE)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(membreRepository);
    }
}
