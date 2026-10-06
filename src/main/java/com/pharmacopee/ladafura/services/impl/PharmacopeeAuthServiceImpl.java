package com.pharmacopee.ladafura.services.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Praticien;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.auth.PharmacopeeAuthResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.auth.PharmacopeeItemSummaryDto;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.exceptions.UnauthorizedException;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PraticienRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PharmacopeeAuthServiceImpl implements IPharmacopeeAuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PharmacopeeRepository pharmacopeeRepository;
    private final PraticienRepository praticienRepository;

    @Override
    public Utilisateur getCurrentUtilisateur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Tentative d'accès non authentifié détectée");
            throw new UnauthorizedException("Authentification requise pour effectuer cette opération.");
        }

        String identifier = authentication.getName();

        // Recherche par email puis par firebaseUid
        Utilisateur utilisateur = utilisateurRepository.findByEmail(identifier)
                .or(() -> utilisateurRepository.findByFirebaseUid(identifier))
                .orElseThrow(() -> new UnauthorizedException("Aucun compte utilisateur trouvé pour l'identifiant : " + identifier));

        if (utilisateur.getStatut() != StatutUtilisateur.ACTIF) {
            log.warn("Compte inactif pour l'utilisateur ID: {}", utilisateur.getId());
            throw new ForbiddenException("Votre compte utilisateur est inactif ou désactivé. Veuillez contacter un administrateur.");
        }

        if (utilisateur.getRole() != Role.PHARMACOPEE && utilisateur.getRole() != Role.ADMINISTRATEUR) {
            log.warn("Rôle non autorisé pour l'utilisateur ID: {} (rôle={})", utilisateur.getId(), utilisateur.getRole());
            throw new ForbiddenException("Accès réservé exclusivement aux acteurs de type PHARMACOPEE.");
        }

        return utilisateur;
    }

    @Override
    public List<Pharmacopee> getMyPharmacopees() {
        Utilisateur user = getCurrentUtilisateur();
        Map<Long, Pharmacopee> map = new LinkedHashMap<>();

        // 1. Structures dont l'utilisateur est le créateur / propriétaire direct
        List<Pharmacopee> directes = pharmacopeeRepository.findAllByUtilisateurId(user.getId());
        for (Pharmacopee p : directes) {
            map.put(p.getId(), p);
        }

        // 2. Structures associées via la table d'association Praticien (N:N)
        Optional<Praticien> praticienOpt = praticienRepository.findById(user.getId());
        if (praticienOpt.isPresent()) {
            Praticien praticien = praticienOpt.get();
            if (praticien.getPharmacopees() != null) {
                for (Pharmacopee p : praticien.getPharmacopees()) {
                    map.put(p.getId(), p);
                }
            }
        }

        return new ArrayList<>(map.values());
    }

    @Override
    public Pharmacopee getCurrentPharmacopee() {
        List<Pharmacopee> structures = getMyPharmacopees();
        Utilisateur user = getCurrentUtilisateur();

        Optional<Praticien> praticienOpt = praticienRepository.findById(user.getId());
        boolean estPrincipal = praticienOpt.map(p -> p.getEstPraticienPrincipal() == null || p.getEstPraticienPrincipal()).orElse(true);

        Long targetId = getHeaderTargetPharmacopeeId();
        if (estPrincipal || user.getRole() == Role.ADMINISTRATEUR) {
            if (targetId != null) {
                if (user.getRole() == Role.ADMINISTRATEUR) {
                    return pharmacopeeRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", targetId));
                }
                for (Pharmacopee p : structures) {
                    if (p.getId().equals(targetId)) {
                        return p;
                    }
                }
                log.warn("L'établissement ID {} spécifié par X-Pharmacopee-Id n'appartient pas à l'utilisateur ID {}", targetId, user.getId());
            }
        }

        if (structures.isEmpty()) {
            throw new ResourceNotFoundException("Pharmacopée", "utilisateurId", user.getId());
        }

        // Par défaut, retourner la première validée ou la première existante
        return structures.stream()
                .filter(p -> p.getStatut() == StatutPharmacopee.VALIDEE)
                .findFirst()
                .orElse(structures.get(0));
    }

    @Override
    public PharmacopeeAuthResponse getMe() {
        Utilisateur user = getCurrentUtilisateur();
        List<Pharmacopee> structures = getMyPharmacopees();

        Optional<Praticien> praticienOpt = praticienRepository.findById(user.getId());
        String planAbonnement = praticienOpt.map(Praticien::getPlanAbonnement).orElse("GRATUIT");
        int quotaMax = praticienOpt.map(p -> p.getQuotaMaxStructures() != null ? p.getQuotaMaxStructures() : 1).orElse(1);
        boolean estPrincipal = praticienOpt.map(p -> p.getEstPraticienPrincipal() == null || p.getEstPraticienPrincipal()).orElse(true);

        Pharmacopee activePharmacopee = null;
        if (!structures.isEmpty()) {
            try {
                activePharmacopee = getCurrentPharmacopee();
            } catch (Exception ignored) {
                activePharmacopee = structures.get(0);
            }
        }

        List<PharmacopeeItemSummaryDto> dtoList = new ArrayList<>();
        for (Pharmacopee p : structures) {
            Localisation loc = p.getLocalisation();
            dtoList.add(PharmacopeeItemSummaryDto.builder()
                    .id(p.getId())
                    .nom(p.getNom())
                    .telephone(p.getTelephone())
                    .statut(p.getStatut())
                    .validee(p.getStatut() == StatutPharmacopee.VALIDEE)
                    .region(loc != null ? loc.getRegion() : null)
                    .cercle(loc != null ? loc.getCercle() : null)
                    .commune(loc != null ? loc.getCommune() : null)
                    .localite(loc != null ? loc.getLocalite() : null)
                    .build());
        }

        PharmacopeeAuthResponse.PharmacopeeAuthResponseBuilder builder = PharmacopeeAuthResponse.builder()
                .utilisateurId(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .role(user.getRole())
                .statutUtilisateur(user.getStatut())
                .firebaseUid(user.getFirebaseUid())
                .structures(dtoList)
                .planAbonnement(planAbonnement)
                .quotaMaxStructures(quotaMax)
                .structuresActuelles(structures.size())
                .peutCreerStructure(estPrincipal && (structures.size() < quotaMax))
                .estPraticienPrincipal(estPrincipal)
                .titrePraticien(estPrincipal ? "Praticien Principal" : "Praticien Collaborateur");

        if (activePharmacopee != null) {
            builder.pharmacopeeId(activePharmacopee.getId())
                   .nomPharmacopee(activePharmacopee.getNom())
                   .telephonePharmacopee(activePharmacopee.getTelephone())
                   .statutReferencement(activePharmacopee.getStatut())
                   .validee(activePharmacopee.getStatut() == StatutPharmacopee.VALIDEE);
        } else {
            builder.validee(false);
        }

        return builder.build();
    }

    @Override
    public void verifyPharmacopeeOwnership(Long requestedPharmacopeeId) {
        if (requestedPharmacopeeId == null) {
            throw new ForbiddenException("Identifiant de pharmacopée manquant.");
        }

        List<Pharmacopee> structures = getMyPharmacopees();
        boolean owns = structures.stream().anyMatch(p -> p.getId().equals(requestedPharmacopeeId));

        if (!owns) {
            log.warn("Violation d'accès : l'utilisateur a tenté d'accéder aux ressources de la pharmacopée non autorisée ID {}", requestedPharmacopeeId);
            throw new ForbiddenException("Accès refusé : vous n'êtes pas autorisé à accéder aux ressources de cet établissement.");
        }
    }

    @Override
    public void verifyPharmacopeeValidated() {
        Pharmacopee currentPharmacopee = getCurrentPharmacopee();

        if (currentPharmacopee.getStatut() != StatutPharmacopee.VALIDEE) {
            log.warn("Tentative d'opération sur pharmacopée non validée ID: {}, statut: {}",
                    currentPharmacopee.getId(), currentPharmacopee.getStatut());
            throw new ForbiddenException("Opération impossible : votre structure n'est pas encore validée par l'administrateur (Statut : "
                    + currentPharmacopee.getStatut() + ").");
        }
    }

    private Long getHeaderTargetPharmacopeeId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String headerVal = request.getHeader("X-Pharmacopee-Id");
            if (headerVal != null && !headerVal.isBlank()) {
                try {
                    return Long.parseLong(headerVal.trim());
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }
    @Override
    public void verifyPraticienPrincipal() {
        // Implementation mock
    }
}

