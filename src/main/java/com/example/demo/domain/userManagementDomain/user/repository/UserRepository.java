package com.example.demo.domain.userManagementDomain.user.repository;

import com.example.demo.domain.userManagementDomain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findById(Integer id);

    Optional<User> findByEmail(String email);

    Optional<User> findByFirstname(String firstname);

    Optional<User> findByFirstnameAndLastname(String firstname, String lastname);

    List<User> findByUserGroup_Id(Long userGroupId);

    List<User> findByRoles_Id(Long roleId);

    @Query("SELECT u FROM User u JOIN u.userGroup ug JOIN ug.roles r WHERE r.id = :roleId")
    List<User> findUsersByRoleId(@Param("roleId") Long roleId);
}
