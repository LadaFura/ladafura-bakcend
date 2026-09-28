package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.admin.user.AdminChangeStatusRequest;
import com.pharmacopee.ladafura.dto.admin.user.AdminCreateUserRequest;
import com.pharmacopee.ladafura.dto.admin.user.AdminUpdateUserRequest;
import com.pharmacopee.ladafura.dto.admin.user.AdminUserResponse;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;

public interface IAdminUserService {

    Page<AdminUserResponse> getAllUsers(Role role, StatutUtilisateur statut, Pageable pageable);

    AdminUserResponse getUserById(Long id);

    AdminUserResponse createUser(AdminCreateUserRequest request);

    AdminUserResponse updateUser(Long id, AdminUpdateUserRequest request);

    AdminUserResponse changeUserStatus(Long id, AdminChangeStatusRequest request);

    void deleteUser(Long id);
}
