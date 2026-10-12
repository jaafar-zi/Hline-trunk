package com.example.demo.modules.kitchen.interfaces;

import com.example.demo.common.base.controller.ApiResponse;
import com.example.demo.common.base.controller.BaseController;
import com.example.demo.modules.kitchen.application.command.KitchenConfigurationCommandService;
import com.example.demo.modules.kitchen.application.dto.request.*;
import com.example.demo.modules.kitchen.application.dto.response.*;
import com.example.demo.modules.kitchen.application.mapper.*;
import com.example.demo.modules.kitchen.application.query.KitchenConfigurationQueryService;
import com.example.demo.modules.kitchen.domain.model.*;
import com.example.demo.modules.kitchen.domain.model.vo.KitchenConfigurationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kitchen Configuration (Planner) REST Controller
 *
 * PAGE 5: /kitchen-planner
 *
 * Handles the kitchen design/configuration session.
 * Key feature: global colour change cascades to all non-overridden items.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kitchen-configurations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Kitchen Planner", description = "Kitchen configuration and planner APIs")
public class KitchenConfigurationController extends BaseController {

    private final KitchenConfigurationCommandService configCommandService;
    private final KitchenConfigurationQueryService configQueryService;
    private final KitchenConfigurationMapper configMapper;
    private final KitchenColourMapper colourMapper;

    // ====================================
    // Create Configuration
    // ====================================

    @PostMapping
    @Operation(summary = "Create kitchen configuration",
               description = "Start a new kitchen design session")
    public ResponseEntity<ApiResponse<KitchenConfigurationResponse>> createConfiguration(
            @Valid @RequestBody CreateKitchenConfigurationRequest request) {

        log.info("Creating kitchen configuration: collectionId={}",
            request.getCollectionId());
        validateAndNormalize(request);

        KitchenConfiguration config = configCommandService.createConfiguration(
            request.getCollectionId(),
            request.getName(),
            request.getCustomerId(),
            request.getSessionId()
        );

        KitchenConfiguration loaded = configQueryService.findByIdWithAll(config.getId());
        return created(buildFullConfigResponse(loaded));
    }

    // ====================================
    // Get Configuration
    // ====================================

    @GetMapping("/{id}")
    @Operation(summary = "Get kitchen configuration",
               description = "Get the full kitchen planner state")
    public ResponseEntity<ApiResponse<KitchenConfigurationResponse>> getConfiguration(
            @PathVariable UUID id) {

        log.info("Getting kitchen configuration: {}", id);
        KitchenConfiguration config = configQueryService.findByIdWithAll(id);
        return success(buildFullConfigResponse(config));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get configurations by customer")
    // @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Page<KitchenConfigurationResponse>>>
            getByCustomer(
            @PathVariable UUID customerId,
            Pageable pageable) {

        Page<KitchenConfiguration> page =
            configQueryService.findByCustomer(customerId, pageable);
        return success(page.map(c -> buildFullConfigResponse(
            configQueryService.findByIdWithAll(c.getId()))));
    }

    @GetMapping("/customer/{customerId}/drafts")
    @Operation(summary = "Get draft configurations by customer")
    // @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<KitchenConfigurationResponse>>> getDraftsByCustomer(
            @PathVariable UUID customerId) {

        List<KitchenConfiguration> drafts =
            configQueryService.findDraftsByCustomer(customerId);
        return success(drafts.stream()
            .map(c -> configMapper.toDto(c))
            .collect(Collectors.toList()));
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get configuration by session (guest user)")
    public ResponseEntity<ApiResponse<KitchenConfigurationResponse>> getBySession(
            @PathVariable String sessionId) {

        return configQueryService.findActiveDraftBySession(sessionId)
            .map(c -> success(buildFullConfigResponse(
                configQueryService.findByIdWithAll(c.getId()))))
            .orElse(success(null));
    }

    // ====================================
    // Update Configuration Name
    // ====================================

    @PutMapping("/{id}/name")
    @Operation(summary = "Update configuration name")
    public ResponseEntity<ApiResponse<Void>> updateName(
            @PathVariable UUID id,
            @RequestParam String name) {

        configCommandService.updateName(id, name);
        return success(null, "Configuration name updated");
    }

    // ====================================
    // Global Colour Change (KEY FEATURE)
    // ====================================

    /**
     * The key mechanic of the kitchen planner.
     * Changes the global cabinet colour and re-resolves variants
     * for ALL non-overridden items automatically.
     */
    @PutMapping("/{id}/colour")
    @Operation(summary = "Change global cabinet colour",
               description = "Changes the cabinet colour for all non-overridden items in the configuration")
    public ResponseEntity<ApiResponse<KitchenConfigurationResponse>> changeGlobalColour(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeGlobalColourRequest request) {

        log.info("Changing global colour: configId={}, colourId={}",
            id, request.getColourId());

        configCommandService.changeGlobalColour(id, request.getColourId());

        KitchenConfiguration updated = configQueryService.findByIdWithAll(id);
        return success(
            buildFullConfigResponse(updated),
            "Cabinet colour changed for all items"
        );
    }

    // ====================================
    // Item Management
    // ====================================

    @PostMapping("/{id}/items")
    @Operation(summary = "Add item to configuration",
               description = "Add a cabinet unit to the kitchen design")
    public ResponseEntity<ApiResponse<KitchenConfigurationItemResponse>> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody AddConfigurationItemRequest request) {

        log.info("Adding item to configuration: configId={}, productId={}",
            id, request.getProductId());
        validateAndNormalize(request);

        KitchenConfigurationItem item = configCommandService.addItem(
            id,
            request.getProductId(),
            request.getVariantId(),
            request.getQuantity()
        );

        if (request.getLabel() != null) {
            configCommandService.updateItemLabel(id, item.getId(), request.getLabel());
        }

        return created(configMapper.toItemDto(item));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "Remove item from configuration")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId) {

        log.info("Removing item: configId={}, itemId={}", id, itemId);
        configCommandService.removeItem(id, itemId);
        return success(null, "Item removed");
    }

    @PutMapping("/{id}/items/{itemId}/quantity")
    @Operation(summary = "Update item quantity")
    public ResponseEntity<ApiResponse<KitchenConfigurationItemResponse>> updateItemQuantity(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestParam int quantity) {

        log.info("Updating item quantity: itemId={}, qty={}", itemId, quantity);

        KitchenConfigurationItem item =
            configCommandService.updateItemQuantity(id, itemId, quantity);

        return success(configMapper.toItemDto(item), "Quantity updated");
    }

    @PutMapping("/{id}/items/{itemId}/colour-override")
    @Operation(summary = "Override item colour",
               description = "Set a different colour for a specific cabinet (overrides global colour)")
    public ResponseEntity<ApiResponse<KitchenConfigurationItemResponse>> overrideItemColour(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestParam UUID newVariantId) {

        log.info("Overriding item colour: configId={}, itemId={}", id, itemId);

        KitchenConfigurationItem item =
            configCommandService.overrideItemColour(id, itemId, newVariantId);

        return success(configMapper.toItemDto(item), "Item colour overridden");
    }

    @PutMapping("/{id}/items/{itemId}/label")
    @Operation(summary = "Update item label")
    public ResponseEntity<ApiResponse<Void>> updateItemLabel(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestParam String label) {

        configCommandService.updateItemLabel(id, itemId, label);
        return success(null, "Label updated");
    }

    // ====================================
    // Status Management
    // ====================================

    @PostMapping("/{id}/quote")
    @Operation(summary = "Generate quote for configuration")
    public ResponseEntity<ApiResponse<KitchenConfigurationResponse>> generateQuote(
            @PathVariable UUID id) {

        log.info("Generating quote for configuration: {}", id);
        configCommandService.markAsQuoted(id);

        KitchenConfiguration config = configQueryService.findByIdWithAll(id);
        return success(buildFullConfigResponse(config), "Quote generated");
    }

    @PostMapping("/{id}/order")
    @Operation(summary = "Mark configuration as ordered")
    public ResponseEntity<ApiResponse<Void>> markAsOrdered(@PathVariable UUID id) {
        configCommandService.markAsOrdered(id);
        return success(null, "Configuration marked as ordered");
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive configuration")
    public ResponseEntity<ApiResponse<Void>> archiveConfiguration(@PathVariable UUID id) {
        configCommandService.archive(id);
        return success(null, "Configuration archived");
    }

    // ====================================
    // Admin
    // ====================================

    @GetMapping("/admin/statistics")
    @Operation(summary = "Get configuration statistics")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ConfigurationStatistics>> getStatistics() {
        return success(ConfigurationStatistics.builder()
            .totalDraft(configQueryService.countByStatus(
                KitchenConfigurationStatus.DRAFT))
            .totalQuoted(configQueryService.countByStatus(
                KitchenConfigurationStatus.QUOTED))
            .totalOrdered(configQueryService.countByStatus(
                KitchenConfigurationStatus.ORDERED))
            .build());
    }

    // ====================================
    // Build Full Response
    // ====================================

    private KitchenConfigurationResponse buildFullConfigResponse(
            KitchenConfiguration config) {

        KitchenConfigurationResponse response = configMapper.toDto(config);

        // Set selected cabinet colour
        if (config.getSelectedCabinetColour() != null) {
            response.setSelectedCabinetColour(
                colourMapper.toDto(config.getSelectedCabinetColour()));
        }

        // Set available cabinet colours from collection
        if (config.getCollection() != null
                && config.getCollection().getAvailableCabinetColours() != null) {
            response.setAvailableCabinetColours(
                config.getCollection().getAvailableCabinetColours().stream()
                    .map(colourMapper::toDto)
                    .collect(Collectors.toList())
            );
        }

        // Set items
        List<KitchenConfigurationItem> items =
            configQueryService.findItemsByConfiguration(config.getId());
        response.setItems(configMapper.toItemDtoList(items));

        // Set totals
        response.setTotalItems(items.size());
        response.setTotalUnits(configQueryService.sumQuantityByConfiguration(
            config.getId()));
        response.setSubtotal(configQueryService.sumTotalByConfiguration(config.getId()));
        response.setCurrency("GBP");

        return response;
    }

    @lombok.Data
    @lombok.Builder
    public static class ConfigurationStatistics {
        private Long totalDraft;
        private Long totalQuoted;
        private Long totalOrdered;
    }
}