package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Fournisseur;
import com.pharmacopee.ladafura.Models.FournisseurProduit;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.EnregistrerFournisseurProduitRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.EnregistrerFournisseurRequest;
import com.pharmacopee.ladafura.enums.StatutFournisseur;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.AchatRepository;
import com.pharmacopee.ladafura.repository.FournisseurProduitRepository;
import com.pharmacopee.ladafura.repository.FournisseurRepository;
import com.pharmacopee.ladafura.repository.LigneAchatRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeFournisseurServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;

@ExtendWith(MockitoExtension.class)
class PharmacopeeFournisseurServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;

    @Mock private IPharmacopeeContexteService contexteService;
    @Mock private FournisseurRepository fournisseurRepository;
    @Mock private FournisseurProduitRepository fournisseurProduitRepository;
    @Mock private ProduitRepository produitRepository;
    @Mock private AchatRepository achatRepository;
    @Mock private LigneAchatRepository ligneAchatRepository;

    @InjectMocks private PharmacopeeFournisseurServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine").build();

    @BeforeEach
    void preparer() {
        ContexteMembre contexte = new ContexteMembre(MembrePharmacopee.builder().id(1L).build(), pharmacopee, Set.of());
        lenient().when(contexteService.verifierPermission(anyString())).thenReturn(contexte);
        lenient().when(contexteService.verifierOperationCommerciale(anyString())).thenReturn(contexte);
        lenient().when(fournisseurRepository.save(any(Fournisseur.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(fournisseurProduitRepository.save(any(FournisseurProduit.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Fournisseur fournisseur(Long id) {
        Fournisseur f = Fournisseur.builder().id(id).pharmacopee(pharmacopee).nom("Coopérative").statut(StatutFournisseur.ACTIF).build();
        lenient().when(fournisseurRepository.findByIdAndPharmacopeeId(id, PHARMACOPEE_ID)).thenReturn(Optional.of(f));
        return f;
    }

    private void produit(Long id, StatutProduit statut) {
        when(produitRepository.findById(id)).thenReturn(Optional.of(Produit.builder().id(id).nom("Tisane").statut(statut).build()));
    }

    @Test
    @DisplayName("création : le fournisseur appartient à la pharmacopée du contexte")
    void creation() {
        service.creer(EnregistrerFournisseurRequest.builder().nom("  Coopérative de Sikasso ").build());

        verify(fournisseurRepository).save(org.mockito.ArgumentMatchers.argThat(f -> f.getPharmacopee() == pharmacopee && "Coopérative de Sikasso".equals(f.getNom())));
    }

    @Test
    @DisplayName("une écriture exige une opération commerciale autorisée")
    void ecritureRefusee() {
        when(contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.FOURNISSEURS_GERER)).thenThrow(new ForbiddenException("non"));

        assertThatThrownBy(() -> service.creer(EnregistrerFournisseurRequest.builder().nom("X").build())).isInstanceOf(ForbiddenException.class);
        verify(fournisseurRepository, never()).save(any());
    }

    @Test
    @DisplayName("le fournisseur d'une autre pharmacopée n'est pas trouvé")
    void autrePharmacopee() {
        when(fournisseurRepository.findByIdAndPharmacopeeId(9L, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFournisseur(9L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("désactivation d'un fournisseur")
    void desactivation() {
        Fournisseur f = fournisseur(1L);

        service.changerStatut(1L, StatutFournisseur.INACTIF);

        assertThat(f.estActif()).isFalse();
    }

    @Test
    @DisplayName("un fournisseur qui a des achats ne se supprime pas")
    void suppressionAvecAchats() {
        Fournisseur f = fournisseur(1L);
        when(achatRepository.existsByFournisseurId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.supprimer(1L)).isInstanceOf(ConflictException.class);
        verify(fournisseurRepository, never()).delete(f);
    }

    @Test
    @DisplayName("un fournisseur sans achat est supprimé avec ses produits proposés")
    void suppression() {
        Fournisseur f = fournisseur(1L);
        when(achatRepository.existsByFournisseurId(1L)).thenReturn(false);

        service.supprimer(1L);

        verify(fournisseurProduitRepository).deleteByFournisseurId(1L);
        verify(fournisseurRepository).delete(f);
    }

    private static EnregistrerFournisseurProduitRequest proposition() {
        return EnregistrerFournisseurProduitRequest.builder().produitId(7L).disponible(true).build();
    }

    @Test
    @DisplayName("seul un produit validé au catalogue peut être proposé")
    void produitNonValide() {
        fournisseur(1L);
        produit(7L, StatutProduit.EN_ATTENTE);

        assertThatThrownBy(() -> service.ajouterProduit(1L, proposition())).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("un même produit n'est proposé qu'une fois par fournisseur")
    void propositionDouble() {
        fournisseur(1L);
        produit(7L, StatutProduit.VALIDE);
        when(fournisseurProduitRepository.existsByFournisseurIdAndProduitId(1L, 7L)).thenReturn(true);

        assertThatThrownBy(() -> service.ajouterProduit(1L, proposition())).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("ajout d'un produit proposé")
    void ajoutProduit() {
        fournisseur(1L);
        produit(7L, StatutProduit.VALIDE);

        assertThat(service.ajouterProduit(1L, proposition()).getProduitNom()).isEqualTo("Tisane");
    }

    @Test
    @DisplayName("une ligne rattachée à un autre fournisseur n'est pas trouvée")
    void ligneAutreFournisseur() {
        Fournisseur autre = Fournisseur.builder().id(2L).build();
        when(fournisseurProduitRepository.findByIdAndFournisseurPharmacopeeId(30L, PHARMACOPEE_ID))
                .thenReturn(Optional.of(FournisseurProduit.builder().id(30L).fournisseur(autre).build()));

        assertThatThrownBy(() -> service.retirerProduit(1L, 30L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
