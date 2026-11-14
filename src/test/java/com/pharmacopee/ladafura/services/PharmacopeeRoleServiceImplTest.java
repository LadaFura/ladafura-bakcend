package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Permission;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.RolePharmacopee;
import com.pharmacopee.ladafura.Models.RolePharmacopeePermission;
import com.pharmacopee.ladafura.dto.pharmacopee.role.EnregistrerRoleRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.role.RoleResponse;
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
import com.pharmacopee.ladafura.services.impl.PharmacopeeRoleServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;

@ExtendWith(MockitoExtension.class)
class PharmacopeeRoleServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;
    private static final Long ROLE_ID = 30L;

    @Mock private IPharmacopeeContexteService contexteService;
    @Mock private IJournalAuditService journalAuditService;
    @Mock private RolePharmacopeeRepository roleRepository;
    @Mock private RolePharmacopeePermissionRepository lienRepository;
    @Mock private AttributionRolePharmacopeeRepository attributionRepository;
    @Mock private PermissionRepository permissionRepository;

    @InjectMocks private PharmacopeeRoleServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine").build();
    private final Permission ventes = permission("VENTES_CREER", PorteePermission.PHARMACOPEE);
    private final Permission stock = permission("STOCK_CONSULTER", PorteePermission.PHARMACOPEE);

    @BeforeEach
    void preparer() {
        ContexteMembre contexte = new ContexteMembre(MembrePharmacopee.builder().id(1L).build(), pharmacopee, Set.of());
        lenient().when(contexteService.verifierPermission(anyString())).thenReturn(contexte);
        lenient().when(roleRepository.save(any(RolePharmacopee.class))).thenAnswer(i -> i.getArgument(0));
    }

    private static Permission permission(String code, PorteePermission portee) {
        return Permission.builder().code(code).portee(portee).build();
    }

    private static EnregistrerRoleRequest requete(String libelle, String... codes) {
        return EnregistrerRoleRequest.builder().libelle(libelle).permissionCodes(Set.of(codes)).build();
    }

    private RolePharmacopee rolePersonnalise() {
        RolePharmacopee role = RolePharmacopee.builder().id(ROLE_ID).code("VENDEUR_DE_NUIT").libelle("Vendeur de nuit")
                .pharmacopee(pharmacopee).systeme(false).actif(true).build();
        when(roleRepository.findDisponibleByIdPourPharmacopee(ROLE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(role));
        return role;
    }

    @Test
    @DisplayName("création : le code est dérivé du libellé, sans accents")
    void creation() {
        when(permissionRepository.findByCodeIn(Set.of("VENTES_CREER"))).thenReturn(List.of(ventes));

        RoleResponse reponse = service.creer(requete("  Préparateur de nuit ", "VENTES_CREER"));

        assertThat(reponse.getCode()).isEqualTo("PREPARATEUR_DE_NUIT");
        assertThat(reponse.getSysteme()).isFalse();
        assertThat(reponse.getPermissions()).containsExactly("VENTES_CREER");
        verify(lienRepository).save(any(RolePharmacopeePermission.class));
    }

    @Test
    @DisplayName("un libellé reprenant un code système est refusé")
    void codeSysteme() {
        when(roleRepository.findByCodeAndPharmacopeeIsNull("MANAGER")).thenReturn(Optional.of(new RolePharmacopee()));

        assertThatThrownBy(() -> service.creer(requete("Manager", "VENTES_CREER"))).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("un rôle du même nom dans la pharmacopée est refusé")
    void codeDejaPris() {
        when(roleRepository.findByCodeAndPharmacopeeIsNull("STAGIAIRE")).thenReturn(Optional.empty());
        when(roleRepository.existsByPharmacopeeIdAndCode(PHARMACOPEE_ID, "STAGIAIRE")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(requete("Stagiaire", "VENTES_CREER"))).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("une permission de portée PLATEFORME est refusée")
    void permissionPlateforme() {
        when(permissionRepository.findByCodeIn(Set.of("PLATEFORME_ROLES_GERER")))
                .thenReturn(List.of(permission("PLATEFORME_ROLES_GERER", PorteePermission.PLATEFORME)));

        assertThatThrownBy(() -> service.creer(requete("Super vendeur", "PLATEFORME_ROLES_GERER"))).isInstanceOf(BadRequestException.class);
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("une permission inconnue est refusée")
    void permissionInconnue() {
        when(permissionRepository.findByCodeIn(Set.of("INVENTEE"))).thenReturn(List.of());

        assertThatThrownBy(() -> service.creer(requete("Vendeur", "INVENTEE"))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("un libellé sans lettre ni chiffre est refusé")
    void libelleVide() {
        assertThatThrownBy(() -> service.creer(requete("---", "VENTES_CREER"))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("modification : seule la différence de permissions est écrite, et tracée")
    void modification() {
        RolePharmacopee role = rolePersonnalise();
        RolePharmacopeePermission lienStock = RolePharmacopeePermission.builder().rolePharmacopee(role).permission(stock).build();
        when(permissionRepository.findByCodeIn(Set.of("VENTES_CREER"))).thenReturn(List.of(ventes));
        when(lienRepository.findByRolePharmacopeeId(ROLE_ID)).thenReturn(List.of(lienStock));

        RoleResponse reponse = service.modifier(ROLE_ID, requete("Vendeur du soir", "VENTES_CREER"));

        verify(lienRepository).deleteAll(List.of(lienStock));
        verify(lienRepository).save(Mockito.argThat(l -> l.getPermission() == ventes));
        assertThat(reponse.getLibelle()).isEqualTo("Vendeur du soir");
        assertThat(reponse.getCode()).isEqualTo("VENDEUR_DE_NUIT");
        verify(journalAuditService).tracer(Mockito.argThat(e -> e.action() == TypeActionAudit.PERMISSIONS_ROLE_MODIFIEES));
    }

    @Test
    @DisplayName("les rôles système ne sont pas modifiables")
    void roleSysteme() {
        when(roleRepository.findDisponibleByIdPourPharmacopee(1L, PHARMACOPEE_ID))
                .thenReturn(Optional.of(RolePharmacopee.builder().id(1L).code("COMPTABLE").systeme(true).build()));

        assertThatThrownBy(() -> service.modifier(1L, requete("X", "VENTES_CREER"))).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.desactiver(1L)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.supprimer(1L)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("le rôle d'une autre pharmacopée n'est pas trouvé")
    void autrePharmacopee() {
        when(roleRepository.findDisponibleByIdPourPharmacopee(ROLE_ID, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desactiver(ROLE_ID)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("désactivation d'un rôle personnalisé")
    void desactivation() {
        RolePharmacopee role = rolePersonnalise();

        assertThat(service.desactiver(ROLE_ID).getActif()).isFalse();
        assertThat(role.getActif()).isFalse();
    }

    @Test
    @DisplayName("un rôle attribué ne se supprime pas")
    void suppressionRoleAttribue() {
        rolePersonnalise();
        when(attributionRepository.countByRolePharmacopeeId(ROLE_ID)).thenReturn(2L);

        assertThatThrownBy(() -> service.supprimer(ROLE_ID)).isInstanceOf(ConflictException.class);
        verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("un rôle non attribué est supprimé avec ses permissions")
    void suppression() {
        RolePharmacopee role = rolePersonnalise();
        when(attributionRepository.countByRolePharmacopeeId(ROLE_ID)).thenReturn(0L);
        when(lienRepository.findByRolePharmacopeeId(ROLE_ID)).thenReturn(List.of());

        service.supprimer(ROLE_ID);

        verify(roleRepository).delete(role);
    }
}
