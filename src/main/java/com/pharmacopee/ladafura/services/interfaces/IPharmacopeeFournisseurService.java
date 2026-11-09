package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.EnregistrerFournisseurProduitRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.EnregistrerFournisseurRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.FournisseurProduitResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.FournisseurResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.HistoriqueAchatFournisseurResponse;
import com.pharmacopee.ladafura.enums.StatutFournisseur;

/** Fournisseurs de la pharmacopée courante. Leur gestion est facultative. */
public interface IPharmacopeeFournisseurService {

    Page<FournisseurResponse> getFournisseurs(StatutFournisseur statut, Pageable pageable);

    FournisseurResponse getFournisseur(Long id);

    FournisseurResponse creer(EnregistrerFournisseurRequest request);

    FournisseurResponse modifier(Long id, EnregistrerFournisseurRequest request);

    FournisseurResponse changerStatut(Long id, StatutFournisseur statut);

    /** @throws com.pharmacopee.ladafura.exceptions.ConflictException si le fournisseur a des achats */
    void supprimer(Long id);

    Page<FournisseurProduitResponse> getProduits(Long fournisseurId, Pageable pageable);

    FournisseurProduitResponse ajouterProduit(Long fournisseurId, EnregistrerFournisseurProduitRequest request);

    FournisseurProduitResponse modifierProduit(Long fournisseurId, Long ligneId, EnregistrerFournisseurProduitRequest request);

    void retirerProduit(Long fournisseurId, Long ligneId);

    Page<HistoriqueAchatFournisseurResponse> getHistorique(Long fournisseurId, Pageable pageable);
}
