package com.example.demo.entityProfile.entity;

import lombok.Getter;

@Getter
public enum FieldType {
    TEXT("text"),
    EMAIL("email"),
    NUMBER("number"),
    PASSWORD("password"),
    DATE("date"),
    SELECT("select"),
    AUTOCOMPLETE("autocomplete"),
    READONLY("readonly"),
    CUSTOM("custom");

    private final String value;

    FieldType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
