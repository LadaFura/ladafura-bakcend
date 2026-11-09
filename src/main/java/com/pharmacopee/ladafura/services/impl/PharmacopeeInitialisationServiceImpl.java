package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AttributionRolePharmacopee;
import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.RolePharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.AttributionRolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.CaisseRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeeRepository;
import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeInitialisationService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeRoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PharmacopeeInitialisationServiceImpl implements IPharmacopeeInitialisationService {

    private final MembrePharmacopeeRepository membreRepository;
    private final AttributionRolePharmacopeeRepository attributionRepository;
    private final RolePharmacopeeRepository roleRepository;
    private final CaisseRepository caisseRepository;
    private final IAbonnementCycleService abonnementCycleService;

    @Override
    public void initialiser(Pharmacopee pharmacopee, Utilisateur proprietaire) {
        if (proprietaire != null) {
            definirProprietaire(pharmacopee, proprietaire);
        }
        abonnementCycleService.attribuerGratuit(pharmacopee, proprietaire);
        creerCompte(pharmacopee, "Caisse principale", TypeCaisse.PHYSIQUE);
        creerCompte(pharmacopee, "Mobile Money", TypeCaisse.MOBILE_MONEY);
        creerCompte(pharmacopee, "Banque", TypeCaisse.BANQUE);
    }

    @Override
    public void definirProprietaire(Pharmacopee pharmacopee, Utilisateur proprietaire) {
        RolePharmacopee gerant = roleSysteme(IPharmacopeeRoleService.ROLE_GERANT);
        membreRepository.findFirstByPharmacopeeIdAndEstProprietaireTrue(pharmacopee.getId())
                .filter(ancien -> !ancien.getUtilisateur().getId().equals(proprietaire.getId()))
                .ifPresent(ancien -> retrograder(ancien, gerant));

        MembrePharmacopee membre = adhesionActive(pharmacopee, proprietaire);
        membre.setEstProprietaire(true);
        membreRepository.saveAndFlush(membre);
        attribuer(membre, gerant);
    }

    @Override
    public void synchroniserPraticiens(Pharmacopee pharmacopee, List<Utilisateur> rattaches, Collection<Long> detaches) {
        RolePharmacopee manager = roleSysteme(IPharmacopeeRoleService.ROLE_MANAGER);
        for (Utilisateur utilisateur : rattaches) {
            MembrePharmacopee membre = adhesionActive(pharmacopee, utilisateur);
            membreRepository.saveAndFlush(membre);
            if (!Boolean.TRUE.equals(membre.getEstProprietaire())) {
                attribuer(membre, manager);
            }
        }
        for (Long utilisateurId : detaches) {
            membreRepository.findByUtilisateurIdAndPharmacopeeId(utilisateurId, pharmacopee.getId())
                    .filter(membre -> !Boolean.TRUE.equals(membre.getEstProprietaire()))
                    .ifPresent(membre -> membre.setStatut(StatutMembre.RETIRE));
        }
    }

    private MembrePharmacopee adhesionActive(Pharmacopee pharmacopee, Utilisateur utilisateur) {
        MembrePharmacopee membre = membreRepository.findByUtilisateurIdAndPharmacopeeId(utilisateur.getId(), pharmacopee.getId())
                .orElseGet(() -> MembrePharmacopee.builder()
                        .utilisateur(utilisateur)
                        .pharmacopee(pharmacopee)
                        .estProprietaire(false)
                        .build());
        if (membre.getStatut() != StatutMembre.ACTIF) {
            membre.setStatut(StatutMembre.ACTIF);
            membre.setDateAdhesion(LocalDateTime.now());
        }
        return membre;
    }

    /** L'unicité du propriétaire est garantie par la base : l'ancien doit être écrit avant le nouveau. */
    private void retrograder(MembrePharmacopee ancien, RolePharmacopee gerant) {
        ancien.setEstProprietaire(false);
        membreRepository.saveAndFlush(ancien);
        attributionRepository.findByMembrePharmacopeeIdAndRolePharmacopeeId(ancien.getId(), gerant.getId())
                .ifPresent(attributionRepository::delete);
        attribuer(ancien, roleSysteme(IPharmacopeeRoleService.ROLE_MANAGER));
    }

    private void attribuer(MembrePharmacopee membre, RolePharmacopee role) {
        if (!attributionRepository.existsByMembrePharmacopeeIdAndRolePharmacopeeId(membre.getId(), role.getId())) {
            attributionRepository.save(AttributionRolePharmacopee.builder().membrePharmacopee(membre).rolePharmacopee(role).build());
        }
    }

    private RolePharmacopee roleSysteme(String code) {
        return roleRepository.findByCodeAndPharmacopeeIsNull(code)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", "code", code));
    }

    private void creerCompte(Pharmacopee pharmacopee, String nom, TypeCaisse type) {
        caisseRepository.save(Caisse.builder()
                .pharmacopee(pharmacopee)
                .nom(nom)
                .type(type)
                .description("Compte créé automatiquement")
                .actif(true)
                .build());
    }
}
