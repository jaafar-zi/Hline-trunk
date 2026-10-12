package com.example.demo.modules.kitchen.domain.model.vo;

import lombok.Getter;

/**
 * Kitchen Configuration Status
 *
 * Tracks the lifecycle of a kitchen design/project.
 * This IS an enum — these are fixed workflow states.
 */
@Getter
public enum KitchenConfigurationStatus {

    DRAFT("Draft", "Configuration is being designed"),
    QUOTED("Quoted", "Quote has been generated"),
    IN_CART("In Cart", "Added to shopping cart"),
    ORDERED("Ordered", "Order has been placed"),
    ARCHIVED("Archived", "Configuration archived");

    private final String displayName;
    private final String description;

    KitchenConfigurationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}