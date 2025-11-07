package com.example.demo.domain.userManagementDomain.role.respository;

import com.example.demo.domain.userManagementDomain.role.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Finds a permission by its name.
     *
     * @param name The name of the permission.
     * @return An Optional containing the Permission if found.
     */
    Optional<Permission> findByName(String name);

    /**
     * Checks if a permission exists by its name.
     *
     * @param name The name of the permission.
     * @return True if the permission exists, otherwise false.
     */
    boolean existsByName(String name);
}
