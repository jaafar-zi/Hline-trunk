package com.example.demo.modules.user.domain.repository;

import com.example.demo.common.base.repository.BaseRepository;
import com.example.demo.modules.user.domain.model.UserGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UserGroup Repository
 *
 * @author Architecture Team
 * @version 1.0
 */
@Repository
public interface UserGroupRepository extends BaseRepository<UserGroup, UUID> {

    /**
     * Find group by name
     */
    @Query("SELECT g FROM UserGroup g WHERE g.name = :name")
    Optional<UserGroup> findByName(@Param("name") String name);

    /**
     * Find group by code
     */
    @Query("SELECT g FROM UserGroup g WHERE g.code = :code")
    Optional<UserGroup> findByCode(@Param("code") String code);

    /**
     * Find group by code with roles
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("SELECT g FROM UserGroup g WHERE g.code = :code")
    Optional<UserGroup> findByCodeWithRoles(@Param("code") String code);

    /**
     * Check if name exists
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM UserGroup g WHERE g.name = :name")
    boolean existsByName(@Param("name") String name);

    /**
     * Check if code exists
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM UserGroup g WHERE g.code = :code")
    boolean existsByCode(@Param("code") String code);

    /**
     * Find all active groups
     */
    @Query("SELECT g FROM UserGroup g WHERE g.active = true ORDER BY g.priority DESC, g.name")
    List<UserGroup> findAllActive();

    /**
     * Find all inactive groups
     */
    @Query("SELECT g FROM UserGroup g WHERE g.active = false ORDER BY g.name")
    List<UserGroup> findAllInactive();

    /**
     * Find child groups
     */
    @Query("SELECT g FROM UserGroup g WHERE g.parentGroup.id = :parentId")
    List<UserGroup> findByParentGroupId(@Param("parentId") UUID parentId);

    /**
     * Find groups without parent
     */
    @Query("SELECT g FROM UserGroup g WHERE g.parentGroup IS NULL ORDER BY g.priority DESC, g.name")
    List<UserGroup> findRootGroups();

    /**
     * Find all with roles
     */
    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT DISTINCT g FROM UserGroup g")
    List<UserGroup> findAllWithRoles();

    /**
     * Count users in group
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.userGroup.id = :groupId")
    long countUsersByGroupId(@Param("groupId") UUID groupId);

    /**
     * Count child groups by parent group id
     */
    @Query("SELECT COUNT(g) FROM UserGroup g WHERE g.parentGroup.id = :parentId")
    long countByParentGroupId(@Param("parentId") UUID parentId);

    /**
     * Count active users in group
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.userGroup.id = :groupId AND u.enabled = true")
    long countActiveUsersByGroupId(@Param("groupId") UUID groupId);
}