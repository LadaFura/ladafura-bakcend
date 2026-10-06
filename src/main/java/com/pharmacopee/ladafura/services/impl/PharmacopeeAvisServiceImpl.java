package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.avis.PharmacopeeAvisItemResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.avis.PharmacopeeAvisSummaryResponse;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.AvisRepository;
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

    @Override
    @Transactional(readOnly = true)
    public Page<PharmacopeeAvisItemResponse> getAvis(Pageable pageable) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Consultation des avis publiés pour la pharmacopée ID {}", pId);

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

        return PharmacopeeAvisSummaryResponse.builder()
                .totalAvisPublies(total)
                .noteMoyenneGlobale(noteMoyenne)
                .total5Etoiles(s5)
                .total4Etoiles(s4)
                .total3Etoiles(s3)
                .total2Etoiles(s2)
                .total1Etoile(s1)
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
                        + " pour votre pharmacopée."));

        return mapToItemResponse(avis);
    }

    @Override
    @Transactional
    public PharmacopeeAvisItemResponse repondreAvis(Long avisId, String reponseText) {
        pharmacopeeAuthService.verifyPharmacopeeValidated();
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();

        Avis avis = avisRepository.findByIdAndPharmacopeeIdAndStatut(avisId, pId, StatutAvis.PUBLIE)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable ou non autorisé."));
        
        avis.setReponseOfficine(reponseText);
        avis.setDateReponse(LocalDateTime.now());
        avisRepository.save(avis);
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

        Long pharmacopeeId = null;
        String nomPharmacopee = "Pharmacopée inconnue";
        if (a.getPharmacopee() != null) {
            pharmacopeeId = a.getPharmacopee().getId();
            nomPharmacopee = a.getPharmacopee().getNom();
        }

        return PharmacopeeAvisItemResponse.builder()
                .id(a.getId())
                .pharmacopeeId(pharmacopeeId)
                .nomPharmacopee(nomPharmacopee)
                .note(a.getNote())
                .commentaire(a.getCommentaire())
                .dateAvis(a.getDateAvis())
                .auteurNom(auteurNom)
                .statut(a.getStatut())
                .reponseOfficine(a.getReponseOfficine())
                .dateReponse(a.getDateReponse())
                .build();
    }
}
