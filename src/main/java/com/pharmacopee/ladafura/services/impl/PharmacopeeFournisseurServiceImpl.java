package com.pharmacopee.ladafura.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Fournisseur;
import com.pharmacopee.ladafura.Models.FournisseurProduit;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.EnregistrerFournisseurProduitRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.EnregistrerFournisseurRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.FournisseurProduitResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.FournisseurResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.HistoriqueAchatFournisseurResponse;
import com.pharmacopee.ladafura.enums.StatutFournisseur;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AchatMapper;
import com.pharmacopee.ladafura.repository.AchatRepository;
import com.pharmacopee.ladafura.repository.FournisseurProduitRepository;
import com.pharmacopee.ladafura.repository.FournisseurRepository;
import com.pharmacopee.ladafura.repository.LigneAchatRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeFournisseurService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacopeeFournisseurServiceImpl implements IPharmacopeeFournisseurService {

    private final IPharmacopeeContexteService contexteService;
    private final FournisseurRepository fournisseurRepository;
    private final FournisseurProduitRepository fournisseurProduitRepository;
    private final ProduitRepository produitRepository;
    private final AchatRepository achatRepository;
    private final LigneAchatRepository ligneAchatRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<FournisseurResponse> getFournisseurs(StatutFournisseur statut, Pageable pageable) {
        Long pharmacopeeId = pharmacopeeEnLecture(IPharmacopeeContexteService.FOURNISSEURS_CONSULTER);
        Page<Fournisseur> fournisseurs = statut != null
                ? fournisseurRepository.findByPharmacopeeIdAndStatut(pharmacopeeId, statut, pageable)
                : fournisseurRepository.findByPharmacopeeId(pharmacopeeId, pageable);
        return fournisseurs.map(AchatMapper::fournisseur);
    }

    @Override
    @Transactional(readOnly = true)
    public FournisseurResponse getFournisseur(Long id) {
        return AchatMapper.fournisseur(fournisseurDe(pharmacopeeEnLecture(IPharmacopeeContexteService.FOURNISSEURS_CONSULTER), id));
    }

    @Override
    public FournisseurResponse creer(EnregistrerFournisseurRequest request) {
        Pharmacopee pharmacopee = pharmacopeeEnEcriture();
        Fournisseur fournisseur = Fournisseur.builder().pharmacopee(pharmacopee).build();
        copier(request, fournisseur);
        return AchatMapper.fournisseur(fournisseurRepository.save(fournisseur));
    }

    @Override
    public FournisseurResponse modifier(Long id, EnregistrerFournisseurRequest request) {
        Fournisseur fournisseur = fournisseurDe(pharmacopeeEnEcriture().getId(), id);
        copier(request, fournisseur);
        return AchatMapper.fournisseur(fournisseur);
    }

    private static void copier(EnregistrerFournisseurRequest request, Fournisseur fournisseur) {
        fournisseur.setNom(request.getNom().trim());
        if (request.getTypeFournisseur() != null) {
            fournisseur.setTypeFournisseur(request.getTypeFournisseur());
        }
        fournisseur.setTelephone(request.getTelephone());
        fournisseur.setEmail(request.getEmail());
        fournisseur.setAdresse(request.getAdresse());
        fournisseur.setDescription(request.getDescription());
    }

    @Override
    public FournisseurResponse changerStatut(Long id, StatutFournisseur statut) {
        Fournisseur fournisseur = fournisseurDe(pharmacopeeEnEcriture().getId(), id);
        fournisseur.setStatut(statut);
        return AchatMapper.fournisseur(fournisseur);
    }

    @Override
    public void supprimer(Long id) {
        Fournisseur fournisseur = fournisseurDe(pharmacopeeEnEcriture().getId(), id);
        if (achatRepository.existsByFournisseurId(fournisseur.getId())) {
            throw new ConflictException("Ce fournisseur a des achats : désactivez-le plutôt que de le supprimer.");
        }
        fournisseurProduitRepository.deleteByFournisseurId(fournisseur.getId());
        fournisseurRepository.delete(fournisseur);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FournisseurProduitResponse> getProduits(Long fournisseurId, Pageable pageable) {
        Fournisseur fournisseur = fournisseurDe(pharmacopeeEnLecture(IPharmacopeeContexteService.FOURNISSEURS_CONSULTER), fournisseurId);
        return fournisseurProduitRepository.findByFournisseurId(fournisseur.getId(), pageable).map(AchatMapper::fournisseurProduit);
    }

    @Override
    public FournisseurProduitResponse ajouterProduit(Long fournisseurId, EnregistrerFournisseurProduitRequest request) {
        Fournisseur fournisseur = fournisseurDe(pharmacopeeEnEcriture().getId(), fournisseurId);
        Produit produit = produitValide(request.getProduitId());
        if (fournisseurProduitRepository.existsByFournisseurIdAndProduitId(fournisseur.getId(), produit.getId())) {
            throw new ConflictException("Ce produit est déjà enregistré pour ce fournisseur.");
        }
        FournisseurProduit ligne = FournisseurProduit.builder().fournisseur(fournisseur).produit(produit).build();
        copier(request, ligne);
        return AchatMapper.fournisseurProduit(fournisseurProduitRepository.save(ligne));
    }

    @Override
    public FournisseurProduitResponse modifierProduit(Long fournisseurId, Long ligneId, EnregistrerFournisseurProduitRequest request) {
        FournisseurProduit ligne = ligneDe(pharmacopeeEnEcriture().getId(), fournisseurId, ligneId);
        copier(request, ligne);
        return AchatMapper.fournisseurProduit(ligne);
    }

    @Override
    public void retirerProduit(Long fournisseurId, Long ligneId) {
        fournisseurProduitRepository.delete(ligneDe(pharmacopeeEnEcriture().getId(), fournisseurId, ligneId));
    }

    private static void copier(EnregistrerFournisseurProduitRequest request, FournisseurProduit ligne) {
        ligne.setReferenceFournisseur(request.getReferenceFournisseur());
        ligne.setPrixIndicatif(request.getPrixIndicatif());
        ligne.setDelaiApprovisionnement(request.getDelaiApprovisionnement());
        ligne.setDisponible(request.getDisponible());
        ligne.setObservation(request.getObservation());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoriqueAchatFournisseurResponse> getHistorique(Long fournisseurId, Pageable pageable) {
        Long pharmacopeeId = pharmacopeeEnLecture(IPharmacopeeContexteService.ACHATS_CONSULTER);
        fournisseurDe(pharmacopeeId, fournisseurId);
        return ligneAchatRepository.historiqueFournisseur(pharmacopeeId, fournisseurId, pageable).map(AchatMapper::historique);
    }

    private Long pharmacopeeEnLecture(String permission) {
        return contexteService.verifierPermission(permission).pharmacopee().getId();
    }

    private Pharmacopee pharmacopeeEnEcriture() {
        return contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.FOURNISSEURS_GERER).pharmacopee();
    }

    private Fournisseur fournisseurDe(Long pharmacopeeId, Long id) {
        return fournisseurRepository.findByIdAndPharmacopeeId(id, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", "id", id));
    }

    private FournisseurProduit ligneDe(Long pharmacopeeId, Long fournisseurId, Long ligneId) {
        FournisseurProduit ligne = fournisseurProduitRepository.findByIdAndFournisseurPharmacopeeId(ligneId, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit proposé", "id", ligneId));
        if (!ligne.getFournisseur().getId().equals(fournisseurId)) {
            throw new ResourceNotFoundException("Produit proposé", "id", ligneId);
        }
        return ligne;
    }

    private Produit produitValide(Long id) {
        Produit produit = produitRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Produit", "id", id));
        if (produit.getStatut() != StatutProduit.VALIDE) {
            throw new BadRequestException("Le produit « " + produit.getNom() + " » n'est pas validé au catalogue.");
        }
        return produit;
    }
}
