package com.pharmacopee.ladafura.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Permission;
import com.pharmacopee.ladafura.enums.PorteePermission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    List<Permission> findByCodeIn(Collection<String> codes);

    List<Permission> findByPorteeOrderByModuleAscCodeAsc(PorteePermission portee);
}
