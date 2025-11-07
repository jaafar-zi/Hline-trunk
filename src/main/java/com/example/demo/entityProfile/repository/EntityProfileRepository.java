package com.example.demo.entityProfile.repository;

import com.example.demo.entityProfile.entity.EntityProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EntityProfileRepository extends JpaRepository<EntityProfile, Long> {

    Optional<EntityProfile> findByClassName(String className);

}
