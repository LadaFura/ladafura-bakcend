package com.pharmacopee.ladafura.services.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminCreatePharmacopeeRequest;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminModeratePharmacopeeRequest;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminPharmacopeeDetailResponse;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminUpdatePharmacopeeRequest;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AdminPharmacopeeMapper;
import com.pharmacopee.ladafura.repository.LocalisationRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminPharmacopeeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminPharmacopeeServiceImpl implements IAdminPharmacopeeService {

    private final PharmacopeeRepository pharmacopeeRepository;
    private final LocalisationRepository localisationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final com.pharmacopee.ladafura.repository.PraticienRepository praticienRepository;
    private final AdminPharmacopeeMapper pharmacopeeMapper;
    private final jakarta.persistence.EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPharmacopeeSummaryResponse> getAllPharmacopees(StatutPharmacopee statut, Pageable pageable) {
        log.info("Récupération paginée des pharmacopées (statut={})", statut);
        Page<Pharmacopee> page = (statut != null)
                ? pharmacopeeRepository.findByStatut(statut, pageable)
                : pharmacopeeRepository.findAll(pageable);

        return page.map(pharmacopeeMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPharmacopeeDetailResponse getPharmacopeeById(Long id) {
        log.info("Consultation détaillée de la pharmacopée {}", id);
        Pharmacopee pharmacopee = pharmacopeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", id));

        return pharmacopeeMapper.toDetailDto(pharmacopee);
    }

    @Override
    public AdminPharmacopeeDetailResponse createPharmacopee(AdminCreatePharmacopeeRequest request) {
        log.info("Création administrative d'une pharmacopée : {}", request.getNom());

        Localisation loc = null;
        if (request.getRegion() != null || request.getCommune() != null || request.getLocalite() != null) {
            loc = Localisation.builder()
                    .region(request.getRegion() != null ? request.getRegion().trim() : "Non précisée")
                    .cercle(request.getCercle() != null ? request.getCercle().trim() : "Non précisé")
                    .commune(request.getCommune() != null ? request.getCommune().trim() : "Non précisée")
                    .localite(request.getLocalite() != null ? request.getLocalite().trim() : "Non précisée")
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .build();
            loc = localisationRepository.save(loc);
        }

        Utilisateur proprietaire = null;
        if (request.getUtilisateurId() != null) {
            proprietaire = utilisateurRepository.findById(request.getUtilisateurId()).orElse(null);
        }

        Pharmacopee pharmacopee = Pharmacopee.builder()
                .nom(request.getNom().trim())
                .description(request.getDescription())
                .telephone(request.getTelephone() != null ? request.getTelephone().trim() : null)
                .photoUrl(request.getPhotoUrl())
                .statut(request.getStatut() != null ? request.getStatut() : StatutPharmacopee.EN_ATTENTE)
                .localisation(loc)
                .utilisateur(proprietaire)
                .build();

        Pharmacopee saved = pharmacopeeRepository.save(pharmacopee);

        // Synchronisation des praticiens affiliés
        synchroniserPraticiens(saved, request.getPraticienIds(), request.getUtilisateurId());

        return pharmacopeeMapper.toDetailDto(saved);
    }

    @Override
    public AdminPharmacopeeDetailResponse updatePharmacopee(Long id, AdminUpdatePharmacopeeRequest request) {
        log.info("Mise à jour administrative de la pharmacopée ID {}", id);

        Pharmacopee pharmacopee = pharmacopeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", id));

        if (request.getNom() != null) {
            pharmacopee.setNom(request.getNom().trim());
        }
        if (request.getDescription() != null) {
            pharmacopee.setDescription(request.getDescription());
        }
        if (request.getTelephone() != null) {
            pharmacopee.setTelephone(request.getTelephone().trim());
        }
        if (request.getPhotoUrl() != null) {
            pharmacopee.setPhotoUrl(request.getPhotoUrl().isBlank() ? null : request.getPhotoUrl());
        }
        if (request.getStatut() != null) {
            pharmacopee.setStatut(request.getStatut());
        }
        if (request.getUtilisateurId() != null) {
            Utilisateur proprietaire = utilisateurRepository.findById(request.getUtilisateurId()).orElse(null);
            pharmacopee.setUtilisateur(proprietaire);
        }

        // Localisation
        if (request.getRegion() != null || request.getCommune() != null || request.getLocalite() != null) {
            Localisation loc = pharmacopee.getLocalisation();
            if (loc == null) {
                loc = new Localisation();
            }
            if (request.getRegion() != null) loc.setRegion(request.getRegion().trim());
            if (request.getCercle() != null) loc.setCercle(request.getCercle().trim());
            if (request.getCommune() != null) loc.setCommune(request.getCommune().trim());
            if (request.getLocalite() != null) loc.setLocalite(request.getLocalite().trim());
            if (request.getLatitude() != null) loc.setLatitude(request.getLatitude());
            if (request.getLongitude() != null) loc.setLongitude(request.getLongitude());

            loc = localisationRepository.save(loc);
            pharmacopee.setLocalisation(loc);
        }

        Pharmacopee saved = pharmacopeeRepository.save(pharmacopee);

        // Synchronisation des praticiens si fournis ou si le titulaire a changé
        if (request.getPraticienIds() != null || request.getUtilisateurId() != null) {
            Long principalId = (saved.getUtilisateur() != null) ? saved.getUtilisateur().getId() : null;
            synchroniserPraticiens(saved, request.getPraticienIds(), principalId);
        }

        return pharmacopeeMapper.toDetailDto(saved);
    }

    private void synchroniserPraticiens(Pharmacopee pharmacopee, List<Long> praticienIds, Long principalId) {
        java.util.Set<Long> allIds = new java.util.LinkedHashSet<>();
        if (principalId != null) {
            allIds.add(principalId);
        }
        if (praticienIds != null) {
            allIds.addAll(praticienIds);
        }

        if (allIds.isEmpty()) {
            return;
        }

        for (Long uid : allIds) {
            boolean isPrincipal = uid.equals(principalId);
            ensurePraticienRowExists(uid, isPrincipal);
        }

        // Nettoyage et ré-insertion des liaisons
        entityManager.createNativeQuery("DELETE FROM praticiens_pharmacopees WHERE pharmacopee_id = :pharmaId")
                .setParameter("pharmaId", pharmacopee.getId())
                .executeUpdate();

        for (Long praticienId : allIds) {
            utilisateurRepository.findById(praticienId).ifPresent(u -> {
                if (u.getRole() == com.pharmacopee.ladafura.enums.Role.PHARMACOPEE) {
                    entityManager.createNativeQuery(
                            "INSERT IGNORE INTO praticiens_pharmacopees (praticien_id, pharmacopee_id) VALUES (:pratId, :pharmaId)"
                    )
                    .setParameter("pratId", praticienId)
                    .setParameter("pharmaId", pharmacopee.getId())
                    .executeUpdate();
                }
            });
        }

        entityManager.flush();
        entityManager.refresh(pharmacopee);
    }

    private void ensurePraticienRowExists(Long userId, boolean isPrincipal) {
        utilisateurRepository.findById(userId).ifPresent(user -> {
            if (user.getRole() == com.pharmacopee.ladafura.enums.Role.PHARMACOPEE) {
                entityManager.createNativeQuery(
                        "INSERT INTO praticiens (id, plan_abonnement, quota_max_structures, est_praticien_principal, specialite) " +
                        "VALUES (:id, 'GRATUIT', 1, :principal, 'Médecine traditionnelle & Pharmacopée') " +
                        "ON DUPLICATE KEY UPDATE est_praticien_principal = IF(:principal = 1, 1, est_praticien_principal)"
                )
                .setParameter("id", userId)
                .setParameter("principal", isPrincipal ? 1 : 0)
                .executeUpdate();
            }
        });
    }

    @Override
    public AdminPharmacopeeDetailResponse moderatePharmacopee(Long id, AdminModeratePharmacopeeRequest request) {
        log.info("Modération de la pharmacopée {} : nouveau statut {}", id, request.getAction());
        Pharmacopee pharmacopee = pharmacopeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", id));

        pharmacopee.setStatut(request.getAction());
        Pharmacopee saved = pharmacopeeRepository.save(pharmacopee);

        return pharmacopeeMapper.toDetailDto(saved);
    }

    @Override
    public void deletePharmacopee(Long id) {
        log.info("Suppression de la pharmacopée ID {}", id);
        Pharmacopee pharmacopee = pharmacopeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", id));

        pharmacopeeRepository.delete(pharmacopee);
    }
}
