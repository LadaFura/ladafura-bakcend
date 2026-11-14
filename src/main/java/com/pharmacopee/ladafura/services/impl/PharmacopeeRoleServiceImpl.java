package com.pharmacopee.ladafura.services.impl;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Permission;
import com.pharmacopee.ladafura.Models.RolePharmacopee;
import com.pharmacopee.ladafura.Models.RolePharmacopeePermission;
import com.pharmacopee.ladafura.dto.pharmacopee.role.EnregistrerRoleRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.role.PermissionResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.role.RoleResponse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.PorteePermission;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.AttributionRolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PermissionRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeePermissionRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeeRepository;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService.EvenementAudit;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeRoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacopeeRoleServiceImpl implements IPharmacopeeRoleService {

    private static final String TYPE_ENTITE = "RolePharmacopee";

    private final IPharmacopeeContexteService contexteService;
    private final IJournalAuditService journalAuditService;
    private final RolePharmacopeeRepository rolePharmacopeeRepository;
    private final RolePharmacopeePermissionRepository rolePharmacopeePermissionRepository;
    private final AttributionRolePharmacopeeRepository attributionRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        ContexteMembre contexte = contexteService.verifierPermission(IPharmacopeeContexteService.MEMBRES_CONSULTER);
        List<RolePharmacopee> roles = rolePharmacopeeRepository.findDisponiblesPourPharmacopee(contexte.pharmacopee().getId());
        Map<Long, List<String>> permissions = rolePharmacopeePermissionRepository
                .findByRolePharmacopeeIdIn(roles.stream().map(RolePharmacopee::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(l -> l.getRolePharmacopee().getId(),
                        Collectors.mapping(l -> l.getPermission().getCode(), Collectors.toList())));
        return roles.stream().map(r -> versReponse(r, permissions.getOrDefault(r.getId(), List.of()))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissions() {
        contexteService.verifierPermission(IPharmacopeeContexteService.MEMBRES_CONSULTER);
        return permissionRepository.findByPorteeOrderByModuleAscCodeAsc(PorteePermission.PHARMACOPEE).stream()
                .map(p -> PermissionResponse.builder().code(p.getCode()).libelle(p.getLibelle())
                        .description(p.getDescription()).module(p.getModule()).build())
                .toList();
    }

    @Override
    public RoleResponse creer(EnregistrerRoleRequest request) {
        ContexteMembre contexte = contexteEcriture();
        Long pharmacopeeId = contexte.pharmacopee().getId();
        String code = genererCode(request.getLibelle());
        if (rolePharmacopeeRepository.findByCodeAndPharmacopeeIsNull(code).isPresent()
                || rolePharmacopeeRepository.existsByPharmacopeeIdAndCode(pharmacopeeId, code)) {
            throw new ConflictException("Un rôle portant ce nom existe déjà.");
        }
        List<Permission> permissions = resoudrePermissions(request.getPermissionCodes());
        RolePharmacopee role = rolePharmacopeeRepository.save(RolePharmacopee.builder()
                .code(code)
                .libelle(request.getLibelle().trim())
                .description(request.getDescription())
                .pharmacopee(contexte.pharmacopee())
                .systeme(false)
                .actif(true)
                .creePar(contexte.membre().getUtilisateur())
                .build());
        permissions.forEach(p -> lier(role, p));
        tracer(contexte, TypeActionAudit.ROLE_CREE, role, null, codes(permissions).toString(), "Création du rôle " + code);
        return versReponse(role, List.copyOf(codes(permissions)));
    }

    @Override
    public RoleResponse modifier(Long id, EnregistrerRoleRequest request) {
        ContexteMembre contexte = contexteEcriture();
        RolePharmacopee role = rolePersonnalise(id, contexte);
        List<Permission> permissions = resoudrePermissions(request.getPermissionCodes());
        Set<String> voulues = codes(permissions);

        List<RolePharmacopeePermission> liens = rolePharmacopeePermissionRepository.findByRolePharmacopeeId(role.getId());
        Set<String> actuelles = liens.stream().map(l -> l.getPermission().getCode()).collect(Collectors.toCollection(TreeSet::new));
        rolePharmacopeePermissionRepository.deleteAll(liens.stream().filter(l -> !voulues.contains(l.getPermission().getCode())).toList());
        permissions.stream().filter(p -> !actuelles.contains(p.getCode())).forEach(p -> lier(role, p));

        role.setLibelle(request.getLibelle().trim());
        role.setDescription(request.getDescription());
        tracer(contexte, TypeActionAudit.ROLE_MODIFIE, role, null, null, "Modification du rôle " + role.getCode());
        if (!actuelles.equals(voulues)) {
            tracer(contexte, TypeActionAudit.PERMISSIONS_ROLE_MODIFIEES, role, actuelles.toString(), voulues.toString(),
                    "Permissions du rôle " + role.getCode());
        }
        return versReponse(role, List.copyOf(voulues));
    }

    @Override
    public RoleResponse desactiver(Long id) {
        ContexteMembre contexte = contexteEcriture();
        RolePharmacopee role = rolePersonnalise(id, contexte);
        role.setActif(false);
        tracer(contexte, TypeActionAudit.ROLE_DESACTIVE, role, null, null, "Désactivation du rôle " + role.getCode());
        List<String> permissions = rolePharmacopeePermissionRepository.findByRolePharmacopeeId(role.getId()).stream()
                .map(l -> l.getPermission().getCode()).toList();
        return versReponse(role, permissions);
    }

    @Override
    public void supprimer(Long id) {
        ContexteMembre contexte = contexteEcriture();
        RolePharmacopee role = rolePersonnalise(id, contexte);
        if (attributionRepository.countByRolePharmacopeeId(role.getId()) > 0) {
            throw new ConflictException("Ce rôle est attribué à des membres : désactivez-le plutôt que de le supprimer.");
        }
        tracer(contexte, TypeActionAudit.ROLE_DESACTIVE, role, null, null, "Suppression du rôle " + role.getCode());
        rolePharmacopeePermissionRepository.deleteAll(rolePharmacopeePermissionRepository.findByRolePharmacopeeId(role.getId()));
        rolePharmacopeeRepository.delete(role);
    }

    private ContexteMembre contexteEcriture() {
        ContexteMembre contexte = contexteService.verifierPermission(IPharmacopeeContexteService.ROLES_GERER);
        contexteService.verifierEcritureAutorisee(contexte.pharmacopee().getId());
        return contexte;
    }

    private RolePharmacopee rolePersonnalise(Long id, ContexteMembre contexte) {
        RolePharmacopee role = rolePharmacopeeRepository.findDisponibleByIdPourPharmacopee(id, contexte.pharmacopee().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", "id", id));
        if (Boolean.TRUE.equals(role.getSysteme())) {
            throw new ForbiddenException("Les rôles système ne peuvent pas être modifiés.");
        }
        return role;
    }

    /** Une permission de portée PLATEFORME ne peut jamais être portée par un rôle de pharmacopée. */
    private List<Permission> resoudrePermissions(Set<String> demandes) {
        List<Permission> permissions = permissionRepository.findByCodeIn(demandes);
        if (permissions.size() != demandes.size()
                || permissions.stream().anyMatch(p -> p.getPortee() != PorteePermission.PHARMACOPEE)) {
            throw new BadRequestException("Certaines permissions sont inconnues ou non attribuables à un rôle de pharmacopée.");
        }
        return permissions;
    }

    private void lier(RolePharmacopee role, Permission permission) {
        rolePharmacopeePermissionRepository.save(RolePharmacopeePermission.builder().rolePharmacopee(role).permission(permission).build());
    }

    private static Set<String> codes(List<Permission> permissions) {
        return permissions.stream().map(Permission::getCode).collect(Collectors.toCollection(TreeSet::new));
    }

    static String genererCode(String libelle) {
        String code = Normalizer.normalize(libelle.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (code.isEmpty()) {
            throw new BadRequestException("Le libellé doit contenir au moins une lettre ou un chiffre.");
        }
        return code;
    }

    private void tracer(ContexteMembre contexte, TypeActionAudit action, RolePharmacopee role,
                        String ancienne, String nouvelle, String description) {
        journalAuditService.tracer(EvenementAudit.builder()
                .action(action)
                .origine(OrigineAction.PHARMACOPEE)
                .pharmacopee(contexte.pharmacopee())
                .utilisateur(contexte.membre().getUtilisateur())
                .typeEntite(TYPE_ENTITE)
                .entiteId(role.getId())
                .ancienneValeur(ancienne)
                .nouvelleValeur(nouvelle)
                .description(description)
                .build());
    }

    private static RoleResponse versReponse(RolePharmacopee role, List<String> permissions) {
        return RoleResponse.builder().id(role.getId()).code(role.getCode()).libelle(role.getLibelle())
                .description(role.getDescription()).systeme(role.getSysteme()).actif(role.getActif())
                .permissions(permissions).build();
    }
}
