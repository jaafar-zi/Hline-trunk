package com.example.demo.entityProfile.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EntityFieldDTO {
    private String name;
    private String type;
    private String label;
    private String placeholder;
    private Boolean required;
    private Boolean readonly;
    private Boolean disabled;
    private String defaultValue;
    private List<String> validators;
}

