package com.example.demo.domain.userManagementDomain.user.repository;

import com.example.demo.domain.userManagementDomain.user.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    Optional<Token> findByToken(String token);

    void deleteByToken(String token);

}
