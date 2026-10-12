package com.example.demo.modules.kitchen.domain.model.vo;

import lombok.Getter;

/**
 * Assembly Type Enum
 *
 * This IS an enum because assembly types are fixed physical concepts.
 * A cabinet is either delivered flat-packed or pre-built (rigid).
 * Back office cannot invent new assembly types.
 */
@Getter
public enum AssemblyType {

    FLATPACK("Flatpack", "Self-assembly flat-packed cabinet", 0),
    RIGID("Rigid", "Pre-built rigid cabinet", 1);

    private final String displayName;
    private final String description;
    private final int sortOrder;

    AssemblyType(String displayName, String description, int sortOrder) {
        this.displayName = displayName;
        this.description = description;
        this.sortOrder = sortOrder;
    }

    @Override
    public String toString() {
        return displayName;
    }
}