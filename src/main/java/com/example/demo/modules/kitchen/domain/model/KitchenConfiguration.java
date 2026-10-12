package com.example.demo.modules.kitchen.domain.model;

import com.example.demo.common.base.entity.AuditableAggregateRoot;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.kitchen.domain.event.KitchenColourChangedEvent;
import com.example.demo.modules.kitchen.domain.model.vo.KitchenConfigurationStatus;
import com.example.demo.modules.product.domain.model.vo.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Kitchen Configuration Entity
 *
 * Represents a user's active kitchen design session.
 * NOT an order — just a saved configuration / project.
 *
 * The key mechanic: selectedCabinetColour cascades to all items.
 * When user changes the global colour, every item's variant is re-resolved.
 *
 * Business Rules:
 * - Must reference a KitchenCollection (the style being configured)
 * - Has a global selectedCabinetColour
 * - Can be for a guest (sessionId) or logged-in user (customerId)
 * - Items are resolved against the selected colour
 * - Total price is calculated from items
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "kitchen_configurations",
    indexes = {
        @Index(name = "idx_kconfig_collection", columnList = "collection_id"),
        @Index(name = "idx_kconfig_customer", columnList = "customer_id"),
        @Index(name = "idx_kconfig_session", columnList = "session_id"),
        @Index(name = "idx_kconfig_status", columnList = "status")
    }
)
public class KitchenConfiguration extends AuditableAggregateRoot {

    // ====================================
    // Core Information
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private KitchenCollection collection;

    @Column(name = "name", length = 200)
    private String name;                      // "My L-Shaped Kitchen"

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private KitchenConfigurationStatus status = KitchenConfigurationStatus.DRAFT;

    // ====================================
    // User Context
    // ====================================

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    // ====================================
    // Global Colour Selection
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_cabinet_colour_id")
    private KitchenColour selectedCabinetColour;

    // ====================================
    // Items
    // ====================================

    @OneToMany(
        mappedBy = "configuration",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("position ASC")
    @Builder.Default
    private List<KitchenConfigurationItem> items = new ArrayList<>();

    // ====================================
    // Factory Methods
    // ====================================

    public static KitchenConfiguration create(
            KitchenCollection collection,
            String name,
            UUID customerId,
            String sessionId) {

        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        if (customerId == null && (sessionId == null || sessionId.isBlank())) {
            throw new IllegalArgumentException("Either customerId or sessionId is required");
        }

        return KitchenConfiguration.builder()
            .collection(collection)
            .name(name != null ? name : "My Kitchen")
            .customerId(customerId)
            .sessionId(sessionId)
            .selectedCabinetColour(collection.getDefaultCabinetColour())
            .status(KitchenConfigurationStatus.DRAFT)
            .build();
    }

    // ====================================
    // Domain Logic - Global Colour
    // ====================================

    /**
     * Change the global cabinet colour.
     * This is the key mechanic — all items should re-resolve their variant.
     */
    public void changeGlobalCabinetColour(KitchenColour newColour) {
        if (newColour == null) {
            throw new IllegalArgumentException("Cabinet colour cannot be null");
        }

        if (!collection.getAvailableCabinetColours().contains(newColour)) {
            throw new IllegalArgumentException(
                "Colour " + newColour.getName() + " is not available for this collection"
            );
        }

        KitchenColour oldColour = this.selectedCabinetColour;
        this.selectedCabinetColour = newColour;

        registerEvent(KitchenColourChangedEvent.builder()
            .configurationId(this.getId())
            .collectionId(this.collection.getId())
            .oldColourId(oldColour != null ? oldColour.getId() : null)
            .oldColourName(oldColour != null ? oldColour.getName() : null)
            .newColourId(newColour.getId())
            .newColourName(newColour.getName())
            .affectedItemCount(this.items.size())
            .occurredAt(LocalDateTime.now())
            .build());
    }

    // ====================================
    // Domain Logic - Items
    // ====================================

    public KitchenConfigurationItem addItem(
            KitchenProduct product,
            KitchenProductVariant variant,
            int quantity) {

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (variant == null) {
            throw new IllegalArgumentException("Variant cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        int nextPosition = items.size() + 1;

        KitchenConfigurationItem item = KitchenConfigurationItem.create(
            this, product, variant, quantity, nextPosition
        );

        this.items.add(item);
        return item;
    }

    public void removeItem(UUID itemId) {
        KitchenConfigurationItem item = items.stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        this.items.remove(item);
        reindexPositions();
    }

    public void updateItemQuantity(UUID itemId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        KitchenConfigurationItem item = items.stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        item.updateQuantity(newQuantity);
    }

    /**
     * Override colour for a specific item (different from global)
     */
    public void overrideItemColour(UUID itemId, KitchenProductVariant newVariant) {
        if (newVariant == null) {
            throw new IllegalArgumentException("Variant cannot be null");
        }

        KitchenConfigurationItem item = items.stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        item.overrideVariant(newVariant);
    }

    private void reindexPositions() {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setPosition(i + 1);
        }
    }

    // ====================================
    // Domain Logic - Status
    // ====================================

    public void markAsQuoted() {
        if (this.status != KitchenConfigurationStatus.DRAFT) {
            throw new IllegalStateException("Can only quote a draft configuration");
        }
        this.status = KitchenConfigurationStatus.QUOTED;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("status", KitchenConfigurationStatus.DRAFT, KitchenConfigurationStatus.QUOTED);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    public void markAsOrdered() {
        this.status = KitchenConfigurationStatus.ORDERED;
        ChangeTracker tracker = new ChangeTracker();
        tracker.track("status", this.status, KitchenConfigurationStatus.ORDERED);
        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    public void archive() {
        this.status = KitchenConfigurationStatus.ARCHIVED;
    }

    // ====================================
    // Calculated Fields
    // ====================================

    @Transient
    public int getTotalItemCount() {
        return items.stream()
            .mapToInt(KitchenConfigurationItem::getQuantity)
            .sum();
    }

    @Transient
    public Money getSubtotal() {
        if (items.isEmpty()) {
            return Money.zero("GBP");
        }

        return items.stream()
            .map(KitchenConfigurationItem::getLineTotal)
            .reduce(Money::add)
            .orElse(Money.zero("GBP"));
    }
}