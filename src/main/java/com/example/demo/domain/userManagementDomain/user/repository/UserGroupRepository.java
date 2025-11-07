package com.example.demo.domain.userManagementDomain.user.repository;

import com.example.demo.domain.userManagementDomain.role.entity.Role;
import com.example.demo.domain.userManagementDomain.user.entity.User;
import com.example.demo.domain.userManagementDomain.user.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    /**
     * Finds a user group by its unique name.
     *
     * @param name The name of the user group.
     * @return An Optional containing the UserGroup if found.
     */
    Optional<UserGroup> findByName(String name);

    /**
     * Checks if a user group with the given name exists.
     *
     * @param name The name of the user group.
     * @return true if a user group with this name exists, false otherwise.
     */
    boolean existsByName(String name);

    /**
     * Retrieves all user groups that contain a specific role.
     *
     * @param role The role entity.
     * @return A list of user groups containing the role.
     */
    List<UserGroup> findByRolesContaining(Role role);

    /**
     * Retrieves all user groups that contain a specific user.
     *
     * @param user The user entity.
     * @return A list of user groups containing the user.
     */
    List<UserGroup> findByUsersContaining(User user);

    /**
     * Deletes a user group by name.
     *
     * @param name The name of the user group.
     */
    void deleteByName(String name);
}

