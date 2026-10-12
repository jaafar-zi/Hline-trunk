package com.example.demo.modules.user.domain.repository;

import com.example.demo.common.base.repository.BaseRepository;
import com.example.demo.modules.user.domain.model.Permission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Permission Repository
 *
 * @author Architecture Team
 * @version 1.0
 */
@Repository
public interface PermissionRepository extends BaseRepository<Permission, UUID> {

    /**
     * Find permission by name
     */
    @Query("SELECT p FROM Permission p WHERE p.name = :name")
    Optional<Permission> findByName(@Param("name") String name);

    /**
     * Find permission by resource and action
     */
    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action")
    Optional<Permission> findByResourceAndAction(@Param("resource") String resource, @Param("action") String action);

    /**
     * Check if permission exists by name
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p WHERE p.name = :name")
    boolean existsByName(@Param("name") String name);

    /**
     * Find all system permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.systemPermission = true ORDER BY p.category, p.displayOrder")
    List<Permission> findAllSystemPermissions();

    /**
     * Find all non-system permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.systemPermission = false ORDER BY p.category, p.name")
    List<Permission> findAllNonSystemPermissions();

    /**
     * Find permissions by resource
     */
    @Query("SELECT p FROM Permission p WHERE p.resource = :resource ORDER BY p.action")
    List<Permission> findByResource(@Param("resource") String resource);

    /**
     * Find permissions by action
     */
    @Query("SELECT p FROM Permission p WHERE p.action = :action ORDER BY p.resource")
    List<Permission> findByAction(@Param("action") String action);

    /**
     * Find permissions by category
     */
    @Query("SELECT p FROM Permission p WHERE p.category = :category ORDER BY p.displayOrder, p.name")
    List<Permission> findByCategory(@Param("category") String category);

    /**
     * Find permissions by name in list
     */
    @Query("SELECT p FROM Permission p WHERE p.name IN :names")
    List<Permission> findByNameIn(@Param("names") List<String> names);

    /**
     * Get all distinct resources
     */
    @Query("SELECT DISTINCT p.resource FROM Permission p ORDER BY p.resource")
    List<String> findAllResources();

    /**
     * Get all distinct actions
     */
    @Query("SELECT DISTINCT p.action FROM Permission p ORDER BY p.action")
    List<String> findAllActions();

    /**
     * Get all distinct categories
     */
    @Query("SELECT DISTINCT p.category FROM Permission p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findAllCategories();

    /**
     * Count roles using permission
     */
    @Query("SELECT COUNT(r) FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    long countRolesByPermissionId(@Param("permissionId") UUID permissionId);
}