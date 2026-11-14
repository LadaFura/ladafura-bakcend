package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Praticien;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.auth.PharmacopeeAuthResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.auth.PharmacopeeItemSummaryDto;
import com.pharmacopee.ladafura.dto.pharmacopee.auth.PraticienRegisterRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.UnauthorizedException;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.AttributionRolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.LocalisationRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PraticienRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeePermissionRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeInitialisationService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PharmacopeeAuthServiceImpl implements IPharmacopeeAuthService {

    private static final String EN_TETE_PHARMACOPEE = "X-Pharmacopee-Id";
    private static final List<StatutMembre> STATUTS_ADHESION_EN_COURS = List.of(StatutMembre.ACTIF, StatutMembre.INVITE);
    /** Valeur attendue par le portail Angular pour l'offre gratuite. */
    private static final String PLAN_GRATUIT = "GRATUIT";

    private final UtilisateurRepository utilisateurRepository;
    private final PharmacopeeRepository pharmacopeeRepository;
    private final PraticienRepository praticienRepository;
    private final LocalisationRepository localisationRepository;
    private final MembrePharmacopeeRepository membreRepository;
    private final AttributionRolePharmacopeeRepository attributionRepository;
    private final RolePharmacopeePermissionRepository rolePermissionRepository;
    private final AbonnementRepository abonnementRepository;
    private final IPharmacopeeQuotaService quotaService;
    private final IPharmacopeeInitialisationService initialisationService;
    private final IFirebaseAuthService firebaseAuthService;

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

        if (!peutAccederEspacePharmacopee(utilisateur)) {
            log.warn("Rôle non autorisé pour l'utilisateur ID: {} (rôle={})", utilisateur.getId(), utilisateur.getRole());
            throw new ForbiddenException("Accès réservé aux membres d'une pharmacopée.");
        }

        return utilisateur;
    }

    private boolean peutAccederEspacePharmacopee(Utilisateur utilisateur) {
        return utilisateur.getRole() == Role.PHARMACOPEE
                || utilisateur.getRole() == Role.ADMINISTRATEUR
                || membreRepository.existsByUtilisateurIdAndStatutIn(utilisateur.getId(), STATUTS_ADHESION_EN_COURS);
    }

    @Override
    public MembrePharmacopee getMembreCourant() {
        Utilisateur utilisateur = getCurrentUtilisateur();
        Long pharmacopeeDemandee = lireEnTetePharmacopee();
        if (pharmacopeeDemandee != null) {
            return membreRepository
                    .findByUtilisateurIdAndPharmacopeeIdAndStatut(utilisateur.getId(), pharmacopeeDemandee, StatutMembre.ACTIF)
                    .orElseThrow(() -> {
                        log.warn("L'utilisateur ID {} n'est pas membre actif de la pharmacopée ID {}",
                                utilisateur.getId(), pharmacopeeDemandee);
                        return new ForbiddenException("Accès refusé : vous n'êtes pas membre actif de cette pharmacopée.");
                    });
        }
        return membreParDefaut(adhesionsActives(utilisateur))
                .orElseThrow(() -> new ForbiddenException("Vous n'êtes membre actif d'aucune pharmacopée."));
    }

    private List<MembrePharmacopee> adhesionsActives(Utilisateur utilisateur) {
        return membreRepository.findByUtilisateurIdAndStatut(utilisateur.getId(), StatutMembre.ACTIF);
    }

    private static Optional<MembrePharmacopee> membreParDefaut(List<MembrePharmacopee> adhesions) {
        return adhesions.stream().min(Comparator
                .comparing((MembrePharmacopee m) -> m.getPharmacopee().getStatut() != StatutPharmacopee.VALIDEE)
                .thenComparing(MembrePharmacopee::getId));
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

    @Override
    public List<Pharmacopee> getMyPharmacopees() {
        return adhesionsActives(getCurrentUtilisateur()).stream()
                .map(MembrePharmacopee::getPharmacopee)
                .toList();
    }

    @Override
    public Pharmacopee getCurrentPharmacopee() {
        return getMembreCourant().getPharmacopee();
    }

    @Override
    public PharmacopeeAuthResponse getMe() {
        Utilisateur user = getCurrentUtilisateur();
        List<MembrePharmacopee> adhesions = adhesionsActives(user);
        Optional<MembrePharmacopee> membreActif = membreAffiche(adhesions);

        int quotaMax = quotaService.maxPharmacopeesParGerant(user.getId());
        long possedees = quotaService.pharmacopeesPossedees(user.getId());
        // Un compte sans pharmacopée crée la sienne : il en sera le gérant.
        boolean estPrincipal = membreActif.map(m -> Boolean.TRUE.equals(m.getEstProprietaire())).orElse(true);

        PharmacopeeAuthResponse.PharmacopeeAuthResponseBuilder builder = PharmacopeeAuthResponse.builder()
                .utilisateurId(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .role(user.getRole())
                .statutUtilisateur(user.getStatut())
                .firebaseUid(user.getFirebaseUid())
                .structures(adhesions.stream().map(m -> versResume(m.getPharmacopee())).toList())
                .planAbonnement(PLAN_GRATUIT)
                .quotaMaxStructures(quotaMax)
                .quotaStructuresIllimite(quotaMax == IPharmacopeeQuotaService.PHARMACOPEES_ILLIMITEES)
                .structuresActuelles((int) possedees)
                .peutCreerStructure(estPrincipal && possedees < quotaMax)
                .estPraticienPrincipal(estPrincipal)
                .validee(false);

        membreActif.ifPresent(membre -> completerAvecPharmacopeeActive(builder, membre));
        return builder.build();
    }

    /** Contrairement aux autres appels, /me ne doit pas échouer sur un en-tête périmé : il sert à en choisir un autre. */
    private Optional<MembrePharmacopee> membreAffiche(List<MembrePharmacopee> adhesions) {
        Long demande;
        try {
            demande = lireEnTetePharmacopee();
        } catch (BadRequestException e) {
            log.debug("En-tête de pharmacopée ignoré pour /me : {}", e.getMessage());
            demande = null;
        }
        Long pharmacopeeId = demande;
        return adhesions.stream()
                .filter(m -> m.getPharmacopee().getId().equals(pharmacopeeId))
                .findFirst()
                .or(() -> membreParDefaut(adhesions));
    }

    private void completerAvecPharmacopeeActive(PharmacopeeAuthResponse.PharmacopeeAuthResponseBuilder builder, MembrePharmacopee membre) {
        Pharmacopee pharmacopee = membre.getPharmacopee();
        Optional<Abonnement> abonnement = abonnementRepository.findEnVigueur(pharmacopee.getId());
        var rolesDuMembre = attributionRepository.findByMembrePharmacopeeId(membre.getId()).stream()
                .map(a -> a.getRolePharmacopee())
                .toList();
        builder.pharmacopeeId(pharmacopee.getId())
                .nomPharmacopee(pharmacopee.getNom())
                .telephonePharmacopee(pharmacopee.getTelephone())
                .statutReferencement(pharmacopee.getStatut())
                .validee(pharmacopee.getStatut() == StatutPharmacopee.VALIDEE)
                .membreId(membre.getId())
                .roles(rolesDuMembre.stream().map(r -> r.getCode()).toList())
                .roleLibelle(String.join(", ", rolesDuMembre.stream().map(r -> r.getLibelle()).toList()))
                .permissions(rolePermissionRepository.findCodesPermissionsEffectives(membre.getUtilisateur().getId(), pharmacopee.getId()))
                .statutAbonnement(abonnement.map(a -> a.getStatut().name()).orElse(null))
                .planAbonnement(abonnement.map(a -> libellePlan(a.getCodeOffreSouscrite())).orElse(PLAN_GRATUIT));
    }

    private static String libellePlan(String codeOffre) {
        return IAbonnementCycleService.OFFRE_GRATUITE.equals(codeOffre) ? PLAN_GRATUIT : codeOffre;
    }

    private static PharmacopeeItemSummaryDto versResume(Pharmacopee p) {
        Localisation loc = p.getLocalisation();
        return PharmacopeeItemSummaryDto.builder()
                .id(p.getId())
                .nom(p.getNom())
                .telephone(p.getTelephone())
                .statut(p.getStatut())
                .validee(p.getStatut() == StatutPharmacopee.VALIDEE)
                .region(loc != null ? loc.getRegion() : null)
                .cercle(loc != null ? loc.getCercle() : null)
                .commune(loc != null ? loc.getCommune() : null)
                .localite(loc != null ? loc.getLocalite() : null)
                .build();
    }

    @Override
    public void verifyPharmacopeeOwnership(Long requestedPharmacopeeId) {
        if (requestedPharmacopeeId == null) {
            throw new ForbiddenException("Identifiant de pharmacopée manquant.");
        }

        boolean owns = getMyPharmacopees().stream().anyMatch(p -> p.getId().equals(requestedPharmacopeeId));

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

    @Override
    @Transactional
    public PharmacopeeAuthResponse registerPraticien(PraticienRegisterRequest request) {
        log.info("Inscription d'un praticien principal");

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Utilisateur", "email", request.getEmail());
        }

        String firebaseUid;
        try {
            firebaseUid = firebaseAuthService.createUser(request.getEmail(), request.getMotDePasse(),
                    request.getPrenom() + " " + request.getNom());
        } catch (Exception e) {
            log.error("Échec de création du compte Firebase : {}", e.getMessage());
            throw new BadRequestException("Échec de la création du compte dans le service d'authentification : " + e.getMessage());
        }

        try {
            return enregistrerPraticien(request, firebaseUid);
        } catch (RuntimeException e) {
            supprimerCompteFirebase(firebaseUid);
            throw e;
        }
    }

    private PharmacopeeAuthResponse enregistrerPraticien(PraticienRegisterRequest request, String firebaseUid) {
        try {
            firebaseAuthService.setRole(firebaseUid, Role.PHARMACOPEE.name());
        } catch (Exception e) {
            log.warn("Impossible d'attribuer le rôle PHARMACOPEE dans Firebase pour l'UID {}: {}", firebaseUid, e.getMessage());
        }

        Praticien saved = praticienRepository.saveAndFlush(nouveauPraticien(request, firebaseUid));

        Pharmacopee pharmacopee = pharmacopeeRepository.saveAndFlush(Pharmacopee.builder()
                .nom(request.getNomPharmacopee())
                .description(request.getDescriptionPharmacopee())
                .telephone(request.getTelephonePharmacopee())
                .statut(StatutPharmacopee.EN_ATTENTE)
                .utilisateur(saved)
                .localisation(enregistrerLocalisation(request))
                .build());
        saved.getPharmacopees().add(pharmacopee);
        initialisationService.initialiser(pharmacopee, saved);

        log.info("Praticien principal créé (ID={}) avec pharmacopée EN_ATTENTE", saved.getId());

        return PharmacopeeAuthResponse.builder()
                .utilisateurId(saved.getId())
                .nom(saved.getNom())
                .prenom(saved.getPrenom())
                .email(saved.getEmail())
                .telephone(saved.getTelephone())
                .role(saved.getRole())
                .statutUtilisateur(saved.getStatut())
                .firebaseUid(firebaseUid)
                .pharmacopeeId(pharmacopee.getId())
                .nomPharmacopee(pharmacopee.getNom())
                .telephonePharmacopee(pharmacopee.getTelephone())
                .statutReferencement(pharmacopee.getStatut())
                .validee(false)
                .estPraticienPrincipal(true)
                .build();
    }

    private static Praticien nouveauPraticien(PraticienRegisterRequest request, String firebaseUid) {
        Praticien praticien = new Praticien();
        praticien.setNom(request.getNom());
        praticien.setPrenom(request.getPrenom());
        praticien.setEmail(request.getEmail());
        praticien.setTelephone(request.getTelephone());
        praticien.setRole(Role.PHARMACOPEE);
        praticien.setStatut(StatutUtilisateur.ACTIF);
        praticien.setFirebaseUid(firebaseUid);
        praticien.setDateCreation(LocalDateTime.now());
        praticien.setNumeroAgrement(request.getNumeroAgrement());
        praticien.setSpecialite(request.getSpecialite());
        praticien.setPlanAbonnement(PLAN_GRATUIT);
        praticien.setQuotaMaxStructures(2);
        praticien.setEstPraticienPrincipal(true);
        return praticien;
    }

    private Localisation enregistrerLocalisation(PraticienRegisterRequest request) {
        if (!hasText(request.getRegion()) || !hasText(request.getCommune())) {
            return null;
        }
        return localisationRepository.save(Localisation.builder()
                .region(request.getRegion())
                .cercle(hasText(request.getCercle()) ? request.getCercle() : request.getRegion())
                .commune(request.getCommune())
                .localite(hasText(request.getLocalite()) ? request.getLocalite() : request.getCommune())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build());
    }

    private void supprimerCompteFirebase(String firebaseUid) {
        try {
            firebaseAuthService.deleteUser(firebaseUid);
        } catch (Exception e) {
            log.error("Compte Firebase orphelin après une inscription échouée (UID : {}) : {}", firebaseUid, e.getMessage());
        }
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
