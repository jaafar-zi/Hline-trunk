package com.example.demo.modules.kitchen.interfaces;

import com.example.demo.common.base.controller.ApiResponse;
import com.example.demo.common.base.controller.BaseController;
import com.example.demo.modules.kitchen.application.command.KitchenColourCommandService;
import com.example.demo.modules.kitchen.application.dto.response.KitchenColourResponse;
import com.example.demo.modules.kitchen.application.mapper.KitchenColourMapper;
import com.example.demo.modules.kitchen.application.query.KitchenColourQueryService;
import com.example.demo.modules.kitchen.domain.model.KitchenColour;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Kitchen Colour REST Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kitchen-colours")
@RequiredArgsConstructor
@Tag(name = "Kitchen Colours", description = "Kitchen colour management APIs")
public class KitchenColourController extends BaseController {

    private final KitchenColourCommandService colourCommandService;
    private final KitchenColourQueryService colourQueryService;
    private final KitchenColourMapper colourMapper;

    @GetMapping
    @Operation(summary = "Get all active colours")
    public ResponseEntity<ApiResponse<List<KitchenColourResponse>>> getAllActive() {
        List<KitchenColour> colours = colourQueryService.findAllActive();
        return success(colourMapper.toDtoList(colours));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get colour by ID")
    public ResponseEntity<ApiResponse<KitchenColourResponse>> getById(
            @PathVariable UUID id) {
        KitchenColour colour = colourQueryService.findById(id);
        return success(colourMapper.toDto(colour));
    }

    @PostMapping
    @Operation(summary = "Create colour")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenColourResponse>> createColour(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String hexValue) {

        KitchenColour colour = colourCommandService.createColour(code, name, hexValue);
        return created(colourMapper.toDto(colour));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update colour")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenColourResponse>> updateColour(
            @PathVariable UUID id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String hexValue,
            @RequestParam(required = false) String swatchImageUrl,
            @RequestParam(required = false) String description) {

        KitchenColour colour = colourCommandService.updateColour(
            id, name, hexValue, swatchImageUrl, description);
        return success(colourMapper.toDto(colour), "Colour updated");
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate colour")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable UUID id) {
        colourCommandService.activate(id);
        return success(null, "Colour activated");
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate colour")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        colourCommandService.deactivate(id);
        return success(null, "Colour deactivated");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete colour")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        colourCommandService.softDeleteColour(id, getCurrentUsername());
        return success(null, "Colour deleted");
    }

    private String getCurrentUsername() {
        try {
            var auth = org.springframework.security.core.context
                .SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "system";
        } catch (Exception e) {
            return "system";
        }
    }
}