package com.example.demo.entityProfile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@Entity
public class EntityProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String className;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntityField> fields;

    public EntityProfile() {
        this.fields = new ArrayList<>();
    }

}
