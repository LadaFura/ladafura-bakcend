package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import com.pharmacopee.ladafura.dto.pharmacopee.role.EnregistrerRoleRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.role.PermissionResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.role.RoleResponse;

/** Rôles de la pharmacopée courante. Les rôles système sont en lecture seule. */
public interface IPharmacopeeRoleService {

    String ROLE_GERANT = "GERANT";
    String ROLE_MANAGER = "MANAGER";

    List<RoleResponse> getRoles();

    List<PermissionResponse> getPermissions();

    RoleResponse creer(EnregistrerRoleRequest request);

    RoleResponse modifier(Long id, EnregistrerRoleRequest request);

    RoleResponse desactiver(Long id);

    /** @throws com.pharmacopee.ladafura.exceptions.ConflictException si le rôle est attribué */
    void supprimer(Long id);
}
