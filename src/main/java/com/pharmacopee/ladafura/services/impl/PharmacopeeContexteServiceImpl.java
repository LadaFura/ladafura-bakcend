package com.pharmacopee.ladafura.services.impl;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
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

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final RolePharmacopeePermissionRepository rolePharmacopeePermissionRepository;
    private final AbonnementRepository abonnementRepository;

    @Override
    public ContexteMembre getContexte() {
        MembrePharmacopee membre = pharmacopeeAuthService.getMembreCourant();
        Pharmacopee pharmacopee = membre.getPharmacopee();
        Set<String> permissions = rolePharmacopeePermissionRepository
                .findCodesPermissionsEffectives(membre.getUtilisateur().getId(), pharmacopee.getId());
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
    public ContexteMembre verifierPharmacopeeValidee(String codePermission) {
        ContexteMembre contexte = verifierPermission(codePermission);
        if (contexte.pharmacopee().getStatut() != StatutPharmacopee.VALIDEE) {
            throw new ForbiddenException("Opération impossible : cette pharmacopée n'est pas encore validée par l'administrateur.");
        }
        return contexte;
    }

    @Override
    public ContexteMembre verifierOperationCommerciale(String codePermission) {
        ContexteMembre contexte = verifierPharmacopeeValidee(codePermission);
        verifierEcritureAutorisee(contexte.pharmacopee().getId());
        return contexte;
    }
}
