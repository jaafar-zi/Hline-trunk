package com.example.demo.domain.userManagementDomain.role.respository;

import com.example.demo.domain.userManagementDomain.role.entity.Permission;
import com.example.demo.domain.userManagementDomain.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find a role by its name
    Optional<Role> findByName(String name);

    List<Role> findByUsersId(Long userId);

    List<Role> findByPermissionsContaining(Permission permission);

     List<Role> findAll();

     Optional<Role> findById(Long id);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p = :permission")
    List<Role> findRolesByPermission(@Param("permission") Permission permission);

}

