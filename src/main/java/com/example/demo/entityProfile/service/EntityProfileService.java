package com.example.demo.entityProfile.service;

import com.example.demo.entityProfile.dto.EntityFieldDTO;
import com.example.demo.entityProfile.dto.EntityProfileDTO;
import com.example.demo.entityProfile.entity.EntityField;
import com.example.demo.entityProfile.entity.EntityProfile;
import com.example.demo.entityProfile.repository.EntityFieldRepository;
import com.example.demo.entityProfile.repository.EntityProfileRepository;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EntityProfileService {

    @Autowired
    private EntityProfileRepository entityProfileRepository;
    @Autowired
    private EntityFieldRepository entityFieldRepository;

    private void createEntityProfile(EntityProfileDTO profileDTO) {
        EntityProfile entityProfile = entityProfileRepository.findByClassName(profileDTO.getClassName())
                .orElseGet(EntityProfile::new);
        entityProfile.setClassName(profileDTO.getClassName());
        entityProfile.setFields(new ArrayList<>());

        // Map fields from DTO to entity
        for (EntityFieldDTO fieldDTO : profileDTO.getFields()) {
            EntityField entityField = new EntityField();
            entityField.setName(fieldDTO.getName());
            entityField.setType(fieldDTO.getType());
            entityField.setLabel(fieldDTO.getLabel());
            entityField.setPlaceholder(fieldDTO.getPlaceholder());
            entityField.setRequired(fieldDTO.getRequired());
            entityField.setReadonly(fieldDTO.getReadonly());
            entityField.setDisabled(fieldDTO.getDisabled());
            entityField.setDefaultValue(fieldDTO.getDefaultValue());
            entityField.setValidators(fieldDTO.getValidators());
            entityField.setEntityProfile(entityProfile);
            entityProfile.getFields().add(entityField);
        }
        entityProfileRepository.save(entityProfile);
    }

    public EntityProfileDTO getEntityProfile(Long id) {
        EntityProfile entityProfile = entityProfileRepository.findById(id).orElseThrow(() -> new RuntimeException("Entity Profile not found"));
        return mapToDTO(entityProfile);
    }


    private EntityProfileDTO mapToDTO(EntityProfile entityProfile) {
        EntityProfileDTO dto = new EntityProfileDTO();
        dto.setClassName(entityProfile.getClassName());

        List<EntityFieldDTO> fieldDTOs = entityProfile.getFields().stream()
                .map(field -> {
                    EntityFieldDTO fieldDTO = new EntityFieldDTO();
                    fieldDTO.setName(field.getName());
                    fieldDTO.setType(field.getType());
                    fieldDTO.setLabel(field.getLabel());
                    fieldDTO.setPlaceholder(field.getPlaceholder());
                    fieldDTO.setRequired(field.getRequired());
                    fieldDTO.setReadonly(field.getReadonly());
                    fieldDTO.setDisabled(field.getDisabled());
                    fieldDTO.setDefaultValue(field.getDefaultValue());
                    fieldDTO.setValidators(field.getValidators());
                    return fieldDTO;
                })
                .collect(Collectors.toList());

        dto.setFields(fieldDTOs);
        return dto;
    }

    public Class<?> resolveClassName(String className) throws ClassNotFoundException {
        Reflections reflections = new Reflections("com.example.demo"); // or multiple packages
        Set<Class<?>> all = reflections.getTypesAnnotatedWith(jakarta.persistence.Entity.class);
        return all.stream()
                .filter(clazz -> clazz.getSimpleName().equals(className))
                .findFirst()
                .orElseThrow(() -> new ClassNotFoundException("Class not found"));
    }

    public EntityProfileDTO generateEntityProfile(Class<?> entityClass) {
        EntityProfileDTO profileDTO = new EntityProfileDTO();
        profileDTO.setClassName(entityClass.getSimpleName());

        List<EntityFieldDTO> fieldDTOs = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            EntityFieldDTO fieldDTO = new EntityFieldDTO();
            fieldDTO.setName(field.getName());
            fieldDTO.setType(field.getType().getSimpleName());

            fieldDTO.setLabel(getFieldLabel(field));
            fieldDTO.setPlaceholder(getFieldPlaceholder(field));
            fieldDTO.setRequired(isFieldRequired(field));
            fieldDTO.setReadonly(isFieldReadonly(field));
            fieldDTO.setDisabled(false);
            fieldDTO.setDefaultValue(getFieldDefaultValue(field));
            fieldDTO.setValidators(getFieldValidators(field));

            fieldDTOs.add(fieldDTO);
        }

        profileDTO.setFields(fieldDTOs);
        createEntityProfile(profileDTO);
        return profileDTO;
    }


    private String getFieldLabel(Field field) {
        return field.getName();
    }

    private String getFieldPlaceholder(Field field) {
        return "Enter " + field.getName();
    }

    private Boolean isFieldRequired(Field field) {
        return false;
    }

    private Boolean isFieldReadonly(Field field) {
        return false;
    }

    private String getFieldDefaultValue(Field field) {
        return "";
    }

    private List<String> getFieldValidators(Field field) {
        return new ArrayList<>();
    }
}

