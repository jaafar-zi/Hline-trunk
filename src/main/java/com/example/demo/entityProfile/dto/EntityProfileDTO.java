package com.example.demo.entityProfile.dto;

import lombok.*;

import java.util.List;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EntityProfileDTO {
    private String className;
    private List<EntityFieldDTO> fields;

}
