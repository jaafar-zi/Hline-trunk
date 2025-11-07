package com.example.demo.entityProfile.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class EntityField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String type;

    private String label;

    private String placeholder;

    private Boolean required;

    private Boolean readonly;

    private Boolean disabled;

    private String defaultValue;

    @ElementCollection
    private List<String> validators;

    @ManyToOne
    private EntityProfile entityProfile;

}

