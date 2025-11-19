package com.example.demo.common.base.repository;

import com.example.demo.common.base.entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<TestEntity, Long> {}
