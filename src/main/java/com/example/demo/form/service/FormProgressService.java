package com.example.demo.form.service;

import com.example.demo.form.dto.FormProgressDTO;
import com.example.demo.form.entity.FormProgress;
import com.example.demo.form.respository.FormProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FormProgressService {

    @Autowired
    private FormProgressRepository repository;

    public FormProgress saveOrUpdate(Long userId, String formName, String formData) {
        FormProgress progress = repository
                .findByUserIdAndFormName(userId, formName)
                .orElse(new FormProgress());

        progress.setUserId(userId);
        progress.setFormName(formName);
        progress.setFormData(formData);
        progress.setLastUpdated(LocalDateTime.now());

        return repository.save(progress);
    }

    public Optional<FormProgressDTO> getProgress(Long userId, String formName) {
        return repository.findByUserIdAndFormName(userId, formName).map(this::toDTO);
    }

    public FormProgressDTO toDTO(FormProgress progress) {
        return new FormProgressDTO(
                progress.getId(),
                progress.getUserId(),
                progress.getFormName(),
                progress.getFormData(),
                progress.getLastUpdated()
        );
    }
}

