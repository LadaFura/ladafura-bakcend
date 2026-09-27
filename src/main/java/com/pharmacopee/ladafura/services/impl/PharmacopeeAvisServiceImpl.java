package com.pharmacopee.ladafura.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.avis.PharmacopeeAvisItemResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.avis.PharmacopeeAvisSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.avis.PharmacopeeProduitAvisResponse;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAvisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacopeeAvisServiceImpl implements IPharmacopeeAvisService {

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final AvisRepository avisRepository;
    private final DisponibiliteProduitRepository disponibiliteProduitRepository;
    private final ProduitRepository produitRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PharmacopeeAvisItemResponse> getAvis(Pageable pageable) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Consultation des avis publiés pour les produits de la pharmacopée ID {}", pId);

        Page<Avis> page = avisRepository.findByPharmacopeeIdAndStatut(pId, StatutAvis.PUBLIE, pageable);
        return page.map(this::mapToItemResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacopeeAvisSummaryResponse getSummary() {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Calcul de la synthèse des avis pour la pharmacopée ID {}", pId);

        long total = avisRepository.countByPharmacopeeIdAndStatut(pId, StatutAvis.PUBLIE);
        Double moyenne = avisRepository.findAverageNoteByPharmacopeeIdAndStatut(pId, StatutAvis.PUBLIE);
        Double noteMoyenne = moyenne != null ? Math.round(moyenne * 10.0) / 10.0 : 0.0;

        long s5 = avisRepository.countByPharmacopeeIdAndStatutAndNote(pId, StatutAvis.PUBLIE, 5);
        long s4 = avisRepository.countByPharmacopeeIdAndStatutAndNote(pId, StatutAvis.PUBLIE, 4);
        long s3 = avisRepository.countByPharmacopeeIdAndStatutAndNote(pId, StatutAvis.PUBLIE, 3);
        long s2 = avisRepository.countByPharmacopeeIdAndStatutAndNote(pId, StatutAvis.PUBLIE, 2);
        long s1 = avisRepository.countByPharmacopeeIdAndStatutAndNote(pId, StatutAvis.PUBLIE, 1);

        int distinctProduits = avisRepository.countDistinctProduitsWithAvisByPharmacopeeIdAndStatut(pId, StatutAvis.PUBLIE);

        return PharmacopeeAvisSummaryResponse.builder()
                .totalAvisPublies(total)
                .noteMoyenneGlobale(noteMoyenne)
                .nombreProduitsEvalues(distinctProduits)
                .total5Etoiles(s5)
                .total4Etoiles(s4)
                .total3Etoiles(s3)
                .total2Etoiles(s2)
                .total1Etoile(s1)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacopeeProduitAvisResponse getAvisByProduit(Long produitId, Pageable pageable) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Consultation des avis du produit ID {} pour la pharmacopée ID {}", produitId, pId);

        // Vérifier que le produit est bien associé à cette officine
        boolean estAssocie = disponibiliteProduitRepository.existsByPharmacopeeIdAndProduitId(pId, produitId);
        if (!estAssocie) {
            log.warn("Tentative d'accès aux avis d'un produit non associé (Produit ID {}, Pharmacopée ID {})", produitId, pId);
            throw new ResourceNotFoundException("Le produit ID " + produitId + " n'est pas associé au catalogue de votre pharmacopée.");
        }

        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable avec l'ID " + produitId));

        Page<Avis> avisPage = avisRepository.findByProduitIdAndStatut(produitId, StatutAvis.PUBLIE, pageable);
        Double avg = avisRepository.findAverageNoteByProduitIdAndStatut(produitId, StatutAvis.PUBLIE);
        Double noteMoyenne = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
        long totalAvis = avisRepository.countByProduitIdAndStatut(produitId, StatutAvis.PUBLIE);

        return PharmacopeeProduitAvisResponse.builder()
                .produitId(produit.getId())
                .nomProduit(produit.getNom())
                .noteMoyenne(noteMoyenne)
                .totalAvis(totalAvis)
                .avis(avisPage.map(this::mapToItemResponse))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacopeeAvisItemResponse getAvisDetail(Long id) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();

        Avis avis = avisRepository.findByIdAndPharmacopeeIdAndStatut(id, pId, StatutAvis.PUBLIE)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable avec l'ID " + id
                        + " pour les produits référencés dans votre pharmacopée."));

        return mapToItemResponse(avis);
    }

    private PharmacopeeAvisItemResponse mapToItemResponse(Avis a) {
        String auteurNom = "Client anonyme";
        if (a.getUtilisateur() != null) {
            Utilisateur u = a.getUtilisateur();
            String prenom = u.getPrenom() != null ? u.getPrenom() : "";
            String initialeNom = (u.getNom() != null && !u.getNom().isBlank())
                    ? u.getNom().substring(0, 1).toUpperCase() + "."
                    : "";
            auteurNom = (prenom + " " + initialeNom).trim();
            if (auteurNom.isBlank()) {
                auteurNom = "Client";
            }
        }

        Long produitId = null;
        String nomProduit = "Produit inconnu";
        if (a.getProduit() != null) {
            produitId = a.getProduit().getId();
            nomProduit = a.getProduit().getNom();
        }

        return PharmacopeeAvisItemResponse.builder()
                .id(a.getId())
                .produitId(produitId)
                .nomProduit(nomProduit)
                .note(a.getNote())
                .commentaire(a.getCommentaire())
                .dateAvis(a.getDateAvis())
                .auteurNom(auteurNom)
                .statut(a.getStatut())
                .build();
    }
}
