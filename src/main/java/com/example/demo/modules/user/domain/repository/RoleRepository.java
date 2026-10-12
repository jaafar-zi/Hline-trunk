package com.example.demo.modules.user.domain.repository;

import com.example.demo.common.base.repository.BaseRepository;
import com.example.demo.modules.user.domain.model.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Role Repository
 *
 * @author Architecture Team
 * @version 1.0
 */
@Repository
public interface RoleRepository extends BaseRepository<Role, UUID> {

    /**
     * Find role by name
     */
    @Query("SELECT r FROM Role r WHERE r.name = :name")
    Optional<Role> findByName(@Param("name") String name);

    /**
     * Find role by name with permissions
     */
    @EntityGraph(attributePaths = {"permissions"})
    @Query("SELECT r FROM Role r WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);

    /**
     * Check if role name exists
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.name = :name")
    boolean existsByName(@Param("name") String name);

    /**
     * Find all system roles
     */
    @Query("SELECT r FROM Role r WHERE r.systemRole = true ORDER BY r.priority DESC")
    List<Role> findAllSystemRoles();

    /**
     * Find all non-system roles
     */
    @Query("SELECT r FROM Role r WHERE r.systemRole = false ORDER BY r.name")
    List<Role> findAllNonSystemRoles();

    /**
     * Find roles by priority greater than
     */
    @Query("SELECT r FROM Role r WHERE r.priority > :priority ORDER BY r.priority DESC")
    List<Role> findByPriorityGreaterThan(@Param("priority") int priority);

    /**
     * Find child roles of parent
     */
    @Query("SELECT r FROM Role r WHERE r.parent.id = :parentId")
    List<Role> findByParentId(@Param("parentId") UUID parentId);

    /**
     * Find roles by name in list
     */
    @Query("SELECT r FROM Role r WHERE r.name IN :names")
    List<Role> findByNameIn(@Param("names") List<String> names);

    /**
     * Find roles with permissions
     */
    @EntityGraph(attributePaths = {"permissions"})
    @Query("SELECT DISTINCT r FROM Role r")
    List<Role> findAllWithPermissions();

    /**
     * Count users assigned to role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countUsersByRoleId(@Param("roleId") UUID roleId);

    /**
     * Count roles by IDs
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE r.id IN :ids")
    long countByIdIn(@Param("ids") Set<UUID> ids);

    /**
     * Find roles with specific permission name
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    /**
     * Find roles with specific permission Id
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    List<Role> findByPermissionId(@Param("permissionId") UUID permissionId);
}