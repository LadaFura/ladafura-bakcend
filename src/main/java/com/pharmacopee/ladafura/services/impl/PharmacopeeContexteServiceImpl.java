package com.pharmacopee.ladafura.services.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeePermissionRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PharmacopeeContexteServiceImpl implements IPharmacopeeContexteService {

    private static final String EN_TETE_PHARMACOPEE = "X-Pharmacopee-Id";

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final MembrePharmacopeeRepository membrePharmacopeeRepository;
    private final RolePharmacopeePermissionRepository rolePharmacopeePermissionRepository;
    private final AbonnementRepository abonnementRepository;

    @Override
    public ContexteMembre getContexte() {
        Utilisateur utilisateur = pharmacopeeAuthService.getCurrentUtilisateur();
        MembrePharmacopee membre = resoudreMembre(utilisateur);
        Pharmacopee pharmacopee = membre.getPharmacopee();
        Set<String> permissions = rolePharmacopeePermissionRepository
                .findCodesPermissionsEffectives(utilisateur.getId(), pharmacopee.getId());
        return new ContexteMembre(membre, pharmacopee, permissions);
    }

    @Override
    public ContexteMembre verifierPermission(String codePermission) {
        ContexteMembre contexte = getContexte();
        StatutPharmacopee statut = contexte.pharmacopee().getStatut();
        if (statut == StatutPharmacopee.SUSPENDUE || statut == StatutPharmacopee.REJETEE) {
            throw new ForbiddenException("Opération impossible : cette pharmacopée est " + statut + ".");
        }
        if (!contexte.permissions().contains(codePermission)) {
            log.warn("Permission {} refusée au membre ID {}", codePermission, contexte.membre().getId());
            throw new ForbiddenException("Vous n'avez pas la permission d'effectuer cette opération.");
        }
        return contexte;
    }

    @Override
    public void verifierEcritureAutorisee(Long pharmacopeeId) {
        boolean autorise = abonnementRepository.findEnVigueur(pharmacopeeId)
                .map(Abonnement::autoriseEcriture)
                .orElse(false);
        if (!autorise) {
            throw new ForbiddenException(
                    "Opération impossible : l'abonnement de cette pharmacopée n'autorise que la consultation. Veuillez le renouveler.");
        }
    }

    @Override
    public ContexteMembre verifierOperationCommerciale(String codePermission) {
        ContexteMembre contexte = verifierPermission(codePermission);
        if (contexte.pharmacopee().getStatut() != StatutPharmacopee.VALIDEE) {
            throw new ForbiddenException("Opération impossible : cette pharmacopée n'est pas encore validée par l'administrateur.");
        }
        verifierEcritureAutorisee(contexte.pharmacopee().getId());
        return contexte;
    }

    private MembrePharmacopee resoudreMembre(Utilisateur utilisateur) {
        Long pharmacopeeDemandee = lireEnTetePharmacopee();
        if (pharmacopeeDemandee != null) {
            return membrePharmacopeeRepository
                    .findByUtilisateurIdAndPharmacopeeIdAndStatut(utilisateur.getId(), pharmacopeeDemandee, StatutMembre.ACTIF)
                    .orElseThrow(() -> {
                        log.warn("L'utilisateur ID {} n'est pas membre actif de la pharmacopée ID {}",
                                utilisateur.getId(), pharmacopeeDemandee);
                        return new ForbiddenException("Accès refusé : vous n'êtes pas membre actif de cette pharmacopée.");
                    });
        }
        List<MembrePharmacopee> appartenances = membrePharmacopeeRepository
                .findByUtilisateurIdAndStatut(utilisateur.getId(), StatutMembre.ACTIF);
        return appartenances.stream()
                .min(Comparator
                        .comparing((MembrePharmacopee m) -> m.getPharmacopee().getStatut() != StatutPharmacopee.VALIDEE)
                        .thenComparing(MembrePharmacopee::getId))
                .orElseThrow(() -> new ForbiddenException("Vous n'êtes membre actif d'aucune pharmacopée."));
    }

    private Long lireEnTetePharmacopee() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributs)) {
            return null;
        }
        String valeur = attributs.getRequest().getHeader(EN_TETE_PHARMACOPEE);
        if (valeur == null || valeur.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(valeur.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("L'en-tête " + EN_TETE_PHARMACOPEE + " doit être un identifiant numérique.");
        }
    }
}
