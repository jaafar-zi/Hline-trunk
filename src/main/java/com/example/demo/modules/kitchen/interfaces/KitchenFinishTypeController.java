package com.example.demo.modules.kitchen.interfaces;

import com.example.demo.common.base.controller.ApiResponse;
import com.example.demo.common.base.controller.BaseController;
import com.example.demo.modules.kitchen.application.command.KitchenFinishTypeCommandService;
import com.example.demo.modules.kitchen.application.dto.response.KitchenFinishTypeResponse;
import com.example.demo.modules.kitchen.application.query.KitchenFinishTypeQueryService;
import com.example.demo.modules.kitchen.domain.model.KitchenFinishType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/kitchen-finish-types")
@RequiredArgsConstructor
@Tag(name = "Kitchen Finish Types", description = "Kitchen finish type management APIs")
public class KitchenFinishTypeController extends BaseController {

    private final KitchenFinishTypeCommandService finishTypeCommandService;
    private final KitchenFinishTypeQueryService finishTypeQueryService;

    @GetMapping
    @Operation(summary = "Get all active finish types")
    public ResponseEntity<ApiResponse<List<KitchenFinishTypeResponse>>> getAllActive() {
        List<KitchenFinishType> types = finishTypeQueryService.findAllActive();
        return success(types.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get finish type by ID")
    public ResponseEntity<ApiResponse<KitchenFinishTypeResponse>> getById(
            @PathVariable UUID id) {
        KitchenFinishType type = finishTypeQueryService.findById(id);
        return success(toResponse(type));
    }

    @PostMapping
    @Operation(summary = "Create finish type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenFinishTypeResponse>> create(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String description) {

        log.info("Creating finish type: code={}, name={}", code, name);
        KitchenFinishType type = finishTypeCommandService.createFinishType(code, name, description);
        return created(toResponse(type));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update finish type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenFinishTypeResponse>> update(
            @PathVariable UUID id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description) {

        KitchenFinishType type = finishTypeCommandService.updateFinishType(id, name, description);
        return success(toResponse(type), "Finish type updated");
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate finish type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable UUID id) {
        finishTypeCommandService.activate(id);
        return success(null, "Finish type activated");
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate finish type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        finishTypeCommandService.deactivate(id);
        return success(null, "Finish type deactivated");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete finish type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        finishTypeCommandService.softDeleteFinishType(id, getCurrentUsername());
        return success(null, "Finish type deleted");
    }

    private KitchenFinishTypeResponse toResponse(KitchenFinishType type) {
        KitchenFinishTypeResponse response = new KitchenFinishTypeResponse();
        response.setId(type.getId());
        response.setCode(type.getCode());
        response.setName(type.getName());
        response.setDescription(type.getDescription());
        response.setActive(type.getActive());
        response.setSortOrder(type.getSortOrder());
        response.setCreatedAt(type.getCreatedAt());
        response.setUpdatedAt(type.getUpdatedAt());
        response.setVersion(type.getVersion());
        return response;
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