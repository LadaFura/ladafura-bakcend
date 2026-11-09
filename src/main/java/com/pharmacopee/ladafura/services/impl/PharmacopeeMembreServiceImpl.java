package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AttributionRolePharmacopee;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.RolePharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.CreerMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.InviterMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.MembreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.ModifierRolesMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.TransfertProprieteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.role.RoleResumeResponse;
import com.pharmacopee.ladafura.enums.MotifSuspensionMembre;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.AttributionRolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMembreService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeRoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacopeeMembreServiceImpl implements IPharmacopeeMembreService {

    static final int JOURS_VALIDITE_INVITATION = 7;
    private static final String TYPE_ENTITE = "MembrePharmacopee";

    private final IPharmacopeeContexteService contexteService;
    private final IPharmacopeeQuotaService quotaService;
    private final IJournalAuditService journalAuditService;
    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final IFirebaseAuthService firebaseAuthService;
    private final MembrePharmacopeeRepository membreRepository;
    private final AttributionRolePharmacopeeRepository attributionRepository;
    private final RolePharmacopeeRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PharmacopeeRepository pharmacopeeRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<MembreResponse> getMembres(StatutMembre statut, Pageable pageable) {
        Long pharmacopeeId = contexteService.verifierPermission(IPharmacopeeContexteService.MEMBRES_CONSULTER).pharmacopee().getId();
        Page<MembrePharmacopee> membres = statut != null
                ? membreRepository.findByPharmacopeeIdAndStatut(pharmacopeeId, statut, pageable)
                : membreRepository.findByPharmacopeeId(pharmacopeeId, pageable);
        Map<Long, List<AttributionRolePharmacopee>> attributions = attributionRepository
                .findByMembrePharmacopeeIdIn(membres.map(MembrePharmacopee::getId).getContent()).stream()
                .collect(Collectors.groupingBy(a -> a.getMembrePharmacopee().getId()));
        return membres.map(m -> versReponse(m, attributions.getOrDefault(m.getId(), List.of())));
    }

    @Override
    @Transactional(readOnly = true)
    public MembreResponse getMembre(Long id) {
        ContexteMembre contexte = contexteService.verifierPermission(IPharmacopeeContexteService.MEMBRES_CONSULTER);
        return versReponse(membreDe(contexte.pharmacopee().getId(), id));
    }

    @Override
    public MembreResponse inviter(InviterMembreRequest request) {
        ContexteMembre contexte = contexteEcriture(IPharmacopeeContexteService.MEMBRES_INVITER);
        Pharmacopee pharmacopee = contexte.pharmacopee();
        List<RolePharmacopee> roles = resoudreRoles(request.getRoleIds(), pharmacopee.getId());
        Utilisateur invite = trouverCompte(request);

        MembrePharmacopee membre = membreRepository.findByUtilisateurIdAndPharmacopeeId(invite.getId(), pharmacopee.getId())
                .map(this::preparerRetour)
                .orElseGet(() -> MembrePharmacopee.builder().utilisateur(invite).pharmacopee(pharmacopee).estProprietaire(false).build());
        membre.setStatut(StatutMembre.INVITE);
        membre.setPoste(request.getPoste());
        membre.setInvitePar(contexte.membre().getUtilisateur());
        membre.setDateInvitation(LocalDateTime.now());
        membre.setDateExpirationInvitation(LocalDateTime.now().plusDays(JOURS_VALIDITE_INVITATION));
        MembrePharmacopee enregistre = membreRepository.save(membre);

        attribuer(enregistre, roles, contexte);
        tracer(contexte, TypeActionAudit.MEMBRE_INVITE, enregistre, "Invitation d'un utilisateur existant");
        return versReponse(enregistre);
    }

    @Override
    public MembreResponse creer(CreerMembreRequest request) {
        ContexteMembre contexte = contexteEcriture(IPharmacopeeContexteService.MEMBRES_INVITER);
        Pharmacopee pharmacopee = contexte.pharmacopee();
        List<RolePharmacopee> roles = resoudreRoles(request.getRoleIds(), pharmacopee.getId());
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Utilisateur", "email", request.getEmail());
        }
        quotaService.verifierQuota(pharmacopee.getId(), IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS);

        String firebaseUid = firebaseAuthService.createUser(request.getEmail(), request.getMotDePasse(),
                request.getPrenom() + " " + request.getNom());
        try {
            firebaseAuthService.setRole(firebaseUid, Role.PHARMACOPEE.name());
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(request.getNom());
            utilisateur.setPrenom(request.getPrenom());
            utilisateur.setEmail(request.getEmail());
            utilisateur.setTelephone(request.getTelephone());
            utilisateur.setFirebaseUid(firebaseUid);
            utilisateur.setRole(Role.PHARMACOPEE);
            Utilisateur enregistre = utilisateurRepository.saveAndFlush(utilisateur);

            MembrePharmacopee membre = membreRepository.saveAndFlush(MembrePharmacopee.builder()
                    .utilisateur(enregistre)
                    .pharmacopee(pharmacopee)
                    .statut(StatutMembre.ACTIF)
                    .estProprietaire(false)
                    .poste(request.getPoste())
                    .invitePar(contexte.membre().getUtilisateur())
                    .dateAdhesion(LocalDateTime.now())
                    .build());
            attribuer(membre, roles, contexte);
            tracer(contexte, TypeActionAudit.MEMBRE_ACTIVE, membre, "Création d'un compte et ajout comme membre actif");
            return versReponse(membre);
        } catch (RuntimeException e) {
            supprimerCompteFirebase(firebaseUid);
            throw e;
        }
    }

    @Override
    public MembreResponse modifierRoles(Long id, ModifierRolesMembreRequest request) {
        ContexteMembre contexte = contexteEcriture(IPharmacopeeContexteService.MEMBRES_GERER);
        MembrePharmacopee membre = membreDe(contexte.pharmacopee().getId(), id);
        exigerNonProprietaire(membre);
        if (membre.getStatut() == StatutMembre.RETIRE) {
            throw new ConflictException("Ce membre a été retiré de la pharmacopée.");
        }
        List<RolePharmacopee> voulus = resoudreRoles(request.getRoleIds(), contexte.pharmacopee().getId());
        Set<Long> idsVoulus = voulus.stream().map(RolePharmacopee::getId).collect(Collectors.toSet());

        List<AttributionRolePharmacopee> actuelles = attributionRepository.findByMembrePharmacopeeId(membre.getId());
        Set<Long> idsActuels = actuelles.stream().map(a -> a.getRolePharmacopee().getId()).collect(Collectors.toSet());
        List<AttributionRolePharmacopee> aRetirer = actuelles.stream()
                .filter(a -> !idsVoulus.contains(a.getRolePharmacopee().getId())).toList();
        List<RolePharmacopee> aAjouter = voulus.stream().filter(r -> !idsActuels.contains(r.getId())).toList();

        attributionRepository.deleteAll(aRetirer);
        aRetirer.forEach(a -> tracer(contexte, TypeActionAudit.ROLE_RETIRE, membre, "Rôle retiré : " + a.getRolePharmacopee().getCode()));
        attribuer(membre, aAjouter, contexte);
        return versReponse(membre);
    }

    @Override
    public MembreResponse suspendre(Long id) {
        ContexteMembre contexte = contexteEcriture(IPharmacopeeContexteService.MEMBRES_GERER);
        MembrePharmacopee membre = membreDe(contexte.pharmacopee().getId(), id);
        exigerNonProprietaire(membre);
        if (membre.getStatut() != StatutMembre.ACTIF) {
            throw new ConflictException("Seul un membre actif peut être suspendu.");
        }
        membre.setStatut(StatutMembre.SUSPENDU);
        membre.setMotifSuspension(MotifSuspensionMembre.DECISION_GERANT);
        tracer(contexte, TypeActionAudit.MEMBRE_SUSPENDU, membre, "Suspension par le gérant");
        return versReponse(membre);
    }

    @Override
    public MembreResponse reactiver(Long id) {
        ContexteMembre contexte = contexteEcriture(IPharmacopeeContexteService.MEMBRES_GERER);
        MembrePharmacopee membre = membreDe(contexte.pharmacopee().getId(), id);
        if (membre.getStatut() != StatutMembre.SUSPENDU) {
            throw new ConflictException("Seul un membre suspendu peut être réactivé.");
        }
        if (membre.getMotifSuspension() == MotifSuspensionMembre.DECISION_ADMIN) {
            throw new ForbiddenException("Ce membre a été suspendu par l'administrateur LADAFURA.");
        }
        quotaService.verifierQuota(contexte.pharmacopee().getId(), IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS);
        membre.setStatut(StatutMembre.ACTIF);
        membre.setMotifSuspension(null);
        tracer(contexte, TypeActionAudit.MEMBRE_ACTIVE, membre, "Réactivation");
        return versReponse(membre);
    }

    @Override
    public MembreResponse retirer(Long id) {
        ContexteMembre contexte = contexteEcriture(IPharmacopeeContexteService.MEMBRES_GERER);
        MembrePharmacopee membre = membreDe(contexte.pharmacopee().getId(), id);
        exigerNonProprietaire(membre);
        if (membre.getStatut() == StatutMembre.RETIRE) {
            throw new ConflictException("Ce membre a déjà été retiré.");
        }
        membre.setStatut(StatutMembre.RETIRE);
        membre.setMotifSuspension(null);
        membre.setDateDepart(LocalDateTime.now());
        tracer(contexte, TypeActionAudit.MEMBRE_RETIRE, membre, "Retrait de la pharmacopée");
        return versReponse(membre);
    }

    @Override
    public MembreResponse transfererPropriete(TransfertProprieteRequest request) {
        ContexteMembre contexte = contexteEcriture(IPharmacopeeContexteService.MEMBRES_GERER);
        if (!Boolean.TRUE.equals(contexte.membre().getEstProprietaire())) {
            throw new ForbiddenException("Seul le gérant propriétaire peut transférer la pharmacopée.");
        }
        return effectuerTransfert(contexte.pharmacopee(), request.getMembreId(), contexte.membre().getUtilisateur(), OrigineAction.PHARMACOPEE);
    }

    @Override
    public MembreResponse transfererProprieteParAdmin(Long pharmacopeeId, TransfertProprieteRequest request) {
        Pharmacopee pharmacopee = pharmacopeeRepository.findById(pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacopée", "id", pharmacopeeId));
        return effectuerTransfert(pharmacopee, request.getMembreId(), pharmacopeeAuthService.getCurrentUtilisateur(), OrigineAction.ADMINISTRATION);
    }

    private MembreResponse effectuerTransfert(Pharmacopee pharmacopee, Long membreId, Utilisateur acteur, OrigineAction origine) {
        MembrePharmacopee cible = membreDe(pharmacopee.getId(), membreId);
        if (!cible.estActif()) {
            throw new ConflictException("Le nouveau gérant doit être un membre actif.");
        }
        if (Boolean.TRUE.equals(cible.getEstProprietaire())) {
            throw new ConflictException("Ce membre est déjà le gérant de la pharmacopée.");
        }
        RolePharmacopee gerant = roleSysteme(IPharmacopeeRoleService.ROLE_GERANT);
        RolePharmacopee manager = roleSysteme(IPharmacopeeRoleService.ROLE_MANAGER);

        // L'unicité du propriétaire est garantie par la base : l'ancien doit être écrit avant le nouveau.
        Optional<MembrePharmacopee> ancien = membreRepository.findFirstByPharmacopeeIdAndEstProprietaireTrue(pharmacopee.getId());
        ancien.ifPresent(a -> {
            a.setEstProprietaire(false);
            membreRepository.saveAndFlush(a);
            retirerRole(a, gerant);
            ajouterRole(a, manager, acteur);
        });
        cible.setEstProprietaire(true);
        membreRepository.saveAndFlush(cible);
        retirerRole(cible, manager);
        ajouterRole(cible, gerant, acteur);
        pharmacopee.setUtilisateur(cible.getUtilisateur());

        journalAuditService.tracer(EvenementAudit.builder()
                .action(TypeActionAudit.PROPRIETE_TRANSFEREE)
                .origine(origine)
                .pharmacopee(pharmacopee)
                .utilisateur(acteur)
                .typeEntite(TYPE_ENTITE)
                .entiteId(cible.getId())
                .ancienneValeur(ancien.map(a -> String.valueOf(a.getId())).orElse(null))
                .nouvelleValeur(String.valueOf(cible.getId()))
                .description("Transfert de la propriété de la pharmacopée")
                .build());
        return versReponse(cible);
    }

    private ContexteMembre contexteEcriture(String permission) {
        ContexteMembre contexte = contexteService.verifierPermission(permission);
        contexteService.verifierEcritureAutorisee(contexte.pharmacopee().getId());
        return contexte;
    }

    private MembrePharmacopee membreDe(Long pharmacopeeId, Long id) {
        return membreRepository.findByIdAndPharmacopeeId(id, pharmacopeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre", "id", id));
    }

    private static void exigerNonProprietaire(MembrePharmacopee membre) {
        if (Boolean.TRUE.equals(membre.getEstProprietaire())) {
            throw new ForbiddenException("Le gérant propriétaire ne peut être modifié que par un transfert de propriété.");
        }
    }

    private RolePharmacopee roleSysteme(String code) {
        return roleRepository.findByCodeAndPharmacopeeIsNull(code)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", "code", code));
    }

    /** Le rôle GERANT suit la propriété : il n'est jamais attribuable à la main. */
    private List<RolePharmacopee> resoudreRoles(List<Long> ids, Long pharmacopeeId) {
        List<RolePharmacopee> roles = new ArrayList<>();
        for (Long id : new LinkedHashSet<>(ids)) {
            RolePharmacopee role = roleRepository.findDisponibleByIdPourPharmacopee(id, pharmacopeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle", "id", id));
            if (!Boolean.TRUE.equals(role.getActif())) {
                throw new BadRequestException("Le rôle " + role.getLibelle() + " est désactivé.");
            }
            if (Boolean.TRUE.equals(role.getSysteme()) && IPharmacopeeRoleService.ROLE_GERANT.equals(role.getCode())) {
                throw new ForbiddenException("Le rôle de gérant ne s'attribue que par un transfert de propriété.");
            }
            roles.add(role);
        }
        return roles;
    }

    private Utilisateur trouverCompte(InviterMembreRequest request) {
        boolean parEmail = request.getEmail() != null && !request.getEmail().isBlank();
        boolean parTelephone = request.getTelephone() != null && !request.getTelephone().isBlank();
        if (parEmail == parTelephone) {
            throw new BadRequestException("Renseignez soit l'email, soit le téléphone du compte à inviter.");
        }
        Utilisateur compte = parEmail
                ? utilisateurRepository.findByEmail(request.getEmail().trim()).orElse(null)
                : uniqueParTelephone(request.getTelephone().trim());
        if (compte == null || compte.getStatut() != StatutUtilisateur.ACTIF) {
            throw new ResourceNotFoundException("Aucun compte actif ne correspond. Vous pouvez créer un compte pour cette personne.");
        }
        return compte;
    }

    private Utilisateur uniqueParTelephone(String telephone) {
        List<Utilisateur> comptes = utilisateurRepository.findByTelephone(telephone);
        if (comptes.size() > 1) {
            throw new ConflictException("Plusieurs comptes utilisent ce téléphone : invitez cette personne par son email.");
        }
        return comptes.isEmpty() ? null : comptes.get(0);
    }

    /** Un ancien membre retrouve sa ligne ; tout autre statut interdit une nouvelle invitation. */
    private MembrePharmacopee preparerRetour(MembrePharmacopee existant) {
        if (existant.getStatut() != StatutMembre.RETIRE) {
            throw new ConflictException("Cette personne est déjà membre ou invitée dans cette pharmacopée.");
        }
        attributionRepository.deleteAll(attributionRepository.findByMembrePharmacopeeId(existant.getId()));
        attributionRepository.flush();
        existant.setDateDepart(null);
        existant.setDateAdhesion(null);
        existant.setMotifSuspension(null);
        return existant;
    }

    private void attribuer(MembrePharmacopee membre, List<RolePharmacopee> roles, ContexteMembre contexte) {
        for (RolePharmacopee role : roles) {
            ajouterRole(membre, role, contexte.membre().getUtilisateur());
            tracer(contexte, TypeActionAudit.ROLE_ATTRIBUE, membre, "Rôle attribué : " + role.getCode());
        }
    }

    private void ajouterRole(MembrePharmacopee membre, RolePharmacopee role, Utilisateur auteur) {
        if (!attributionRepository.existsByMembrePharmacopeeIdAndRolePharmacopeeId(membre.getId(), role.getId())) {
            attributionRepository.save(AttributionRolePharmacopee.builder()
                    .membrePharmacopee(membre).rolePharmacopee(role).attribuePar(auteur).build());
        }
    }

    private void retirerRole(MembrePharmacopee membre, RolePharmacopee role) {
        attributionRepository.findByMembrePharmacopeeIdAndRolePharmacopeeId(membre.getId(), role.getId())
                .ifPresent(attributionRepository::delete);
    }

    private void supprimerCompteFirebase(String firebaseUid) {
        try {
            firebaseAuthService.deleteUser(firebaseUid);
        } catch (RuntimeException e) {
            log.error("Compte Firebase orphelin à supprimer manuellement (UID {}) : {}", firebaseUid, e.getMessage());
        }
    }

    private void tracer(ContexteMembre contexte, TypeActionAudit action, MembrePharmacopee membre, String description) {
        journalAuditService.tracer(EvenementAudit.builder()
                .action(action)
                .origine(OrigineAction.PHARMACOPEE)
                .pharmacopee(contexte.pharmacopee())
                .utilisateur(contexte.membre().getUtilisateur())
                .typeEntite(TYPE_ENTITE)
                .entiteId(membre.getId())
                .description(description)
                .build());
    }

    private MembreResponse versReponse(MembrePharmacopee membre) {
        return versReponse(membre, attributionRepository.findByMembrePharmacopeeId(membre.getId()));
    }

    private static MembreResponse versReponse(MembrePharmacopee membre, List<AttributionRolePharmacopee> attributions) {
        Utilisateur utilisateur = membre.getUtilisateur();
        MembreResponse.MembreResponseBuilder reponse = MembreResponse.builder()
                .id(membre.getId())
                .statut(membre.getStatut())
                .motifSuspension(membre.getMotifSuspension())
                .estProprietaire(membre.getEstProprietaire())
                .poste(membre.getPoste())
                .dateInvitation(membre.getDateInvitation())
                .dateExpirationInvitation(membre.getDateExpirationInvitation())
                .dateAdhesion(membre.getDateAdhesion())
                .dateDepart(membre.getDateDepart())
                .roles(attributions.stream()
                        .map(a -> RoleResumeResponse.builder()
                                .id(a.getRolePharmacopee().getId())
                                .code(a.getRolePharmacopee().getCode())
                                .libelle(a.getRolePharmacopee().getLibelle())
                                .build())
                        .toList());
        // Tant que l'invitation n'est pas acceptée, l'identité du compte reste privée.
        if (membre.getStatut() == StatutMembre.INVITE) {
            return reponse.contactMasque(masquerEmail(utilisateur.getEmail())).build();
        }
        return reponse.utilisateurId(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .telephone(utilisateur.getTelephone())
                .build();
    }

    private static String masquerEmail(String email) {
        int arobase = email.indexOf('@');
        return arobase <= 0 ? "***" : email.charAt(0) + "***" + email.substring(arobase);
    }
}
