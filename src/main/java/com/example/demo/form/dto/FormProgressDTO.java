package com.example.demo.form.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FormProgressDTO {
    private Long id;
    private Long userId;
    private String formName;
    private String formData;
    private LocalDateTime lastUpdated;
}

