package com.pharmacopee.ladafura.services.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Favori;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriCheckResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriCountResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriItemResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriRequest;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.TypeFavori;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationFavoriMapper;
import com.pharmacopee.ladafura.repository.FavoriRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationFavoriService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PopulationFavoriServiceImpl implements IPopulationFavoriService {

    private final FavoriRepository favoriRepository;
    private final PlanteRepository planteRepository;
    private final ProduitRepository produitRepository;
    private final PharmacopeeRepository pharmacopeeRepository;
    private final IPopulationAuthService populationAuthService;
    private final PopulationFavoriMapper mapper;

    @Override
    public PopulationToggleFavoriResponse toggleFavori(PopulationToggleFavoriRequest request) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Toggle favori utilisateur ID: {}, type: {}, cibleId: {}", user.getId(), request.getType(), request.getCibleId());

        return switch (request.getType()) {
            case PLANTE -> togglePlante(user, request.getCibleId());
            case PRODUIT -> toggleProduit(user, request.getCibleId());
            case PHARMACOPEE -> togglePharmacopee(user, request.getCibleId());
        };
    }

    private PopulationToggleFavoriResponse togglePlante(Utilisateur user, Long planteId) {
        Plante plante = planteRepository.findByIdAndStatut(planteId, StatutPlante.VALIDE)
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", planteId));

        Optional<Favori> optFavori = favoriRepository.findByUtilisateurIdAndPlanteId(user.getId(), planteId);
        if (optFavori.isPresent()) {
            favoriRepository.delete(optFavori.get());
            log.info("Plante ID: {} retirée des favoris pour l'utilisateur ID: {}", planteId, user.getId());
            return mapper.toToggleResponse(TypeFavori.PLANTE, planteId, false, null, "Plante retirée de vos favoris.");
        } else {
            Favori favori = Favori.builder()
                    .utilisateur(user)
                    .plante(plante)
                    .build();
            Favori saved = favoriRepository.save(favori);
            log.info("Plante ID: {} ajoutée aux favoris (Favori ID: {}) pour l'utilisateur ID: {}", planteId, saved.getId(), user.getId());
            return mapper.toToggleResponse(TypeFavori.PLANTE, planteId, true, saved.getId(), "Plante ajoutée à vos favoris avec succès.");
        }
    }

    private PopulationToggleFavoriResponse toggleProduit(Utilisateur user, Long produitId) {
        Produit produit = produitRepository.findByIdAndStatut(produitId, StatutProduit.VALIDE)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", produitId));

        Optional<Favori> optFavori = favoriRepository.findByUtilisateurIdAndProduitId(user.getId(), produitId);
        if (optFavori.isPresent()) {
            favoriRepository.delete(optFavori.get());
            log.info("Produit ID: {} retiré des favoris pour l'utilisateur ID: {}", produitId, user.getId());
            return mapper.toToggleResponse(TypeFavori.PRODUIT, produitId, false, null, "Produit retiré de vos favoris.");
        } else {
            Favori favori = Favori.builder()
                    .utilisateur(user)
                    .produit(produit)
                    .build();
            Favori saved = favoriRepository.save(favori);
            log.info("Produit ID: {} ajouté aux favoris (Favori ID: {}) pour l'utilisateur ID: {}", produitId, saved.getId(), user.getId());
            return mapper.toToggleResponse(TypeFavori.PRODUIT, produitId, true, saved.getId(), "Produit ajouté à vos favoris avec succès.");
        }
    }

    private PopulationToggleFavoriResponse togglePharmacopee(Utilisateur user, Long pharmacopeeId) {
        Pharmacopee pharmacopee = pharmacopeeRepository.findByIdAndStatut(pharmacopeeId, StatutPharmacopee.VALIDEE)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", pharmacopeeId));

        Optional<Favori> optFavori = favoriRepository.findByUtilisateurIdAndPharmacopeeId(user.getId(), pharmacopeeId);
        if (optFavori.isPresent()) {
            favoriRepository.delete(optFavori.get());
            log.info("Pharmacopée ID: {} retirée des favoris pour l'utilisateur ID: {}", pharmacopeeId, user.getId());
            return mapper.toToggleResponse(TypeFavori.PHARMACOPEE, pharmacopeeId, false, null, "Pharmacopée retirée de vos favoris.");
        } else {
            Favori favori = Favori.builder()
                    .utilisateur(user)
                    .pharmacopee(pharmacopee)
                    .build();
            Favori saved = favoriRepository.save(favori);
            log.info("Pharmacopée ID: {} ajoutée aux favoris (Favori ID: {}) pour l'utilisateur ID: {}", pharmacopeeId, saved.getId(), user.getId());
            return mapper.toToggleResponse(TypeFavori.PHARMACOPEE, pharmacopeeId, true, saved.getId(), "Pharmacopée ajoutée à vos favoris avec succès.");
        }
    }

    @Override
    public PopulationToggleFavoriResponse ajouterPlanteFavori(Long planteId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        Optional<Favori> existant = favoriRepository.findByUtilisateurIdAndPlanteId(user.getId(), planteId);
        if (existant.isPresent()) {
            return mapper.toToggleResponse(TypeFavori.PLANTE, planteId, true, existant.get().getId(), "Cette plante figure déjà dans vos favoris.");
        }
        return togglePlante(user, planteId);
    }

    @Override
    public PopulationToggleFavoriResponse supprimerPlanteFavori(Long planteId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        Optional<Favori> existant = favoriRepository.findByUtilisateurIdAndPlanteId(user.getId(), planteId);
        if (existant.isPresent()) {
            favoriRepository.delete(existant.get());
        }
        return mapper.toToggleResponse(TypeFavori.PLANTE, planteId, false, null, "Plante retirée de vos favoris.");
    }

    @Override
    public PopulationToggleFavoriResponse ajouterProduitFavori(Long produitId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        Optional<Favori> existant = favoriRepository.findByUtilisateurIdAndProduitId(user.getId(), produitId);
        if (existant.isPresent()) {
            return mapper.toToggleResponse(TypeFavori.PRODUIT, produitId, true, existant.get().getId(), "Ce produit figure déjà dans vos favoris.");
        }
        return toggleProduit(user, produitId);
    }

    @Override
    public PopulationToggleFavoriResponse supprimerProduitFavori(Long produitId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        Optional<Favori> existant = favoriRepository.findByUtilisateurIdAndProduitId(user.getId(), produitId);
        if (existant.isPresent()) {
            favoriRepository.delete(existant.get());
        }
        return mapper.toToggleResponse(TypeFavori.PRODUIT, produitId, false, null, "Produit retiré de vos favoris.");
    }

    @Override
    public PopulationToggleFavoriResponse ajouterPharmacopeeFavori(Long pharmacopeeId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        Optional<Favori> existant = favoriRepository.findByUtilisateurIdAndPharmacopeeId(user.getId(), pharmacopeeId);
        if (existant.isPresent()) {
            return mapper.toToggleResponse(TypeFavori.PHARMACOPEE, pharmacopeeId, true, existant.get().getId(), "Cette pharmacopée figure déjà dans vos favoris.");
        }
        return togglePharmacopee(user, pharmacopeeId);
    }

    @Override
    public PopulationToggleFavoriResponse supprimerPharmacopeeFavori(Long pharmacopeeId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        Optional<Favori> existant = favoriRepository.findByUtilisateurIdAndPharmacopeeId(user.getId(), pharmacopeeId);
        if (existant.isPresent()) {
            favoriRepository.delete(existant.get());
        }
        return mapper.toToggleResponse(TypeFavori.PHARMACOPEE, pharmacopeeId, false, null, "Pharmacopée retirée de vos favoris.");
    }

    @Override
    public void supprimerFavoriById(Long favoriId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        Favori favori = favoriRepository.findByIdAndUtilisateurId(favoriId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Favori introuvable avec l'ID: " + favoriId + " pour cet utilisateur."));

        favoriRepository.delete(favori);
        log.info("Favori ID: {} supprimé avec succès pour l'utilisateur ID: {}", favoriId, user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PopulationFavoriCheckResponse checkFavori(TypeFavori type, Long cibleId) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        return switch (type) {
            case PLANTE -> {
                Optional<Favori> opt = favoriRepository.findByUtilisateurIdAndPlanteId(user.getId(), cibleId);
                yield mapper.toCheckResponse(type, cibleId, opt.isPresent(), opt.map(Favori::getId).orElse(null));
            }
            case PRODUIT -> {
                Optional<Favori> opt = favoriRepository.findByUtilisateurIdAndProduitId(user.getId(), cibleId);
                yield mapper.toCheckResponse(type, cibleId, opt.isPresent(), opt.map(Favori::getId).orElse(null));
            }
            case PHARMACOPEE -> {
                Optional<Favori> opt = favoriRepository.findByUtilisateurIdAndPharmacopeeId(user.getId(), cibleId);
                yield mapper.toCheckResponse(type, cibleId, opt.isPresent(), opt.map(Favori::getId).orElse(null));
            }
        };
    }

    @Override
    @Transactional(readOnly = true)
    public PopulationFavoriCountResponse getFavoriCounts() {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        long total = favoriRepository.countByUtilisateurId(user.getId());
        long plantes = favoriRepository.countByUtilisateurIdAndPlanteIsNotNull(user.getId());
        long produits = favoriRepository.countByUtilisateurIdAndProduitIsNotNull(user.getId());
        long pharmacopees = favoriRepository.countByUtilisateurIdAndPharmacopeeIsNotNull(user.getId());

        return mapper.toCountResponse(total, plantes, produits, pharmacopees);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PopulationFavoriItemResponse> getMesFavoris(TypeFavori type, Pageable pageable) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation paginée des favoris utilisateur ID: {} (filtre type={})", user.getId(), type);

        Page<Favori> page;
        if (type == null) {
            page = favoriRepository.findByUtilisateurId(user.getId(), pageable);
        } else {
            page = switch (type) {
                case PLANTE -> favoriRepository.findByUtilisateurIdAndPlanteIsNotNull(user.getId(), pageable);
                case PRODUIT -> favoriRepository.findByUtilisateurIdAndProduitIsNotNull(user.getId(), pageable);
                case PHARMACOPEE -> favoriRepository.findByUtilisateurIdAndPharmacopeeIsNotNull(user.getId(), pageable);
            };
        }

        return page.map(mapper::toItemResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PopulationPlanteSummaryResponse> getMesPlantesFavorites(Pageable pageable) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation des plantes favorites pour l'utilisateur ID: {}", user.getId());
        Page<Favori> page = favoriRepository.findByUtilisateurIdAndPlanteIsNotNull(user.getId(), pageable);
        return page.map(f -> mapper.mapPlanteSummary(f.getPlante()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PopulationProduitSummaryResponse> getMesProduitsFavoris(Pageable pageable) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation des produits favoris pour l'utilisateur ID: {}", user.getId());
        Page<Favori> page = favoriRepository.findByUtilisateurIdAndProduitIsNotNull(user.getId(), pageable);
        return page.map(f -> mapper.mapProduitSummary(f.getProduit()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PopulationPharmacopeeSummaryResponse> getMesPharmacopeesFavorites(Pageable pageable) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation des pharmacopées favorites pour l'utilisateur ID: {}", user.getId());
        Page<Favori> page = favoriRepository.findByUtilisateurIdAndPharmacopeeIsNotNull(user.getId(), pageable);
        return page.map(f -> mapper.mapPharmacopeeSummary(f.getPharmacopee()));
    }
}
