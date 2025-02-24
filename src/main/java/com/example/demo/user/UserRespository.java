package com.example.demo.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRespository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

}
