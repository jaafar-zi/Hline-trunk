package com.example.demo.form.respository;

import com.example.demo.form.entity.FormProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FormProgressRepository extends JpaRepository<FormProgress, Long> {
    Optional<FormProgress> findByUserIdAndFormName(Long userId, String formName);
}
