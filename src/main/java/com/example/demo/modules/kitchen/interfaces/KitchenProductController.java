package com.example.demo.modules.kitchen.interfaces;

import com.example.demo.common.base.controller.ApiResponse;
import com.example.demo.common.base.controller.BaseController;
import com.example.demo.modules.kitchen.application.command.KitchenProductCommandService;
import com.example.demo.modules.kitchen.application.dto.filter.KitchenProductFilter;
import com.example.demo.modules.kitchen.application.dto.request.*;
import com.example.demo.modules.kitchen.application.dto.response.*;
import com.example.demo.modules.kitchen.application.mapper.*;
import com.example.demo.modules.kitchen.application.query.KitchenProductQueryService;
import com.example.demo.modules.kitchen.domain.model.*;
import com.example.demo.modules.kitchen.domain.model.vo.AssemblyType;
import com.example.demo.modules.product.domain.model.vo.Money;
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
 * Kitchen Product REST Controller
 *
 * URL pattern: /kitchens/{rangeSlug}/{collectionSlug}/{productSlug}
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Kitchen Products", description = "Kitchen product APIs")
public class KitchenProductController extends BaseController {

    private final KitchenProductCommandService productCommandService;
    private final KitchenProductQueryService productQueryService;
    private final KitchenProductMapper productMapper;
    private final KitchenColourMapper colourMapper;

    // ====================================
    // Public Storefront
    // ====================================

    /**
     * PAGE 4: Product detail page
     * URL: /kitchens/{rangeSlug}/{collectionSlug}/{productSlug}
     */
    @GetMapping("/api/v1/kitchens/{rangeSlug}/{collectionSlug}/{productSlug}")
    @Operation(summary = "Get product detail page",
               description = "Full product data for the unit configuration page")
    public ResponseEntity<ApiResponse<KitchenProductResponse>> getProductPage(
            @PathVariable String rangeSlug,
            @PathVariable String collectionSlug,
            @PathVariable String productSlug) {

        log.info("Getting product page: {}/{}/{}", rangeSlug, collectionSlug, productSlug);

        KitchenProduct product = productQueryService.findByPath(
            rangeSlug, collectionSlug, productSlug);

        KitchenProductResponse response = buildFullProductResponse(product);
        return success(response);
    }

    private KitchenProductResponse buildFullProductResponse(KitchenProduct product) {
        KitchenProductResponse response = productMapper.toDto(product);

        // Set images
        response.setImages(productMapper.toImageDtoList(product.getImages()));

        // Set all variants
        List<KitchenProductVariant> activeVariants =
            productQueryService.findActiveVariantsByProduct(product.getId());
        response.setVariants(productMapper.toVariantDtoList(activeVariants));

        // Set default variant
        productQueryService.findDefaultVariant(product.getId())
            .ifPresent(v -> response.setDefaultVariant(productMapper.toVariantDto(v)));

        // Set available cabinet colours (unique from variants)
        List<KitchenColourResponse> availableColours = activeVariants.stream()
            .map(KitchenProductVariant::getCabinetColour)
            .filter(c -> c != null && Boolean.TRUE.equals(c.getActive()))
            .distinct()
            .map(colourMapper::toDto)
            .collect(Collectors.toList());
        response.setAvailableCabinetColours(availableColours);

        return response;
    }

    @GetMapping("/api/v1/kitchen-products/popular")
    @Operation(summary = "Get popular products by collection")
    public ResponseEntity<ApiResponse<List<KitchenProductSummary>>> getPopularProducts(
            @RequestParam UUID collectionId) {

        List<KitchenProduct> products =
            productQueryService.findPopularByCollection(collectionId);
        return success(productMapper.toSummaryList(products));
    }

    @GetMapping("/api/v1/kitchen-products/search")
    @Operation(summary = "Search products in a collection")
    public ResponseEntity<ApiResponse<Page<KitchenProductSummary>>> searchProducts(
            @RequestParam UUID collectionId,
            @RequestParam String keyword,
            Pageable pageable) {

        Page<KitchenProduct> page = productQueryService.searchByCollection(
            collectionId, keyword, pageable);
        return success(page.map(productMapper::toSummary));
    }

    // ====================================
    // Variant Resolution (Global Colour)
    // ====================================

    /**
     * Called when user changes the global cabinet colour.
     * Returns the correct variant for a product given the new colour.
     */
    @GetMapping("/api/v1/kitchen-products/{productId}/resolve-variant")
    @Operation(summary = "Resolve variant by colour",
               description = "Get the correct variant for a product given a cabinet colour")
    public ResponseEntity<ApiResponse<List<KitchenProductVariantResponse>>>
            resolveVariantByColour(
            @PathVariable UUID productId,
            @RequestParam UUID colourId) {

        log.info("Resolving variant: productId={}, colourId={}", productId, colourId);

        List<KitchenProductVariant> variants =
            productQueryService.resolveVariantsByColour(productId, colourId);

        return success(productMapper.toVariantDtoList(variants));
    }

    @GetMapping("/api/v1/kitchen-products/{productId}/resolve-exact-variant")
    @Operation(summary = "Resolve exact variant by colour and assembly type")
    public ResponseEntity<ApiResponse<KitchenProductVariantResponse>> resolveExactVariant(
            @PathVariable UUID productId,
            @RequestParam UUID colourId,
            @RequestParam AssemblyType assemblyType) {

        return productQueryService.resolveExactVariant(productId, colourId, assemblyType)
            .map(v -> success(productMapper.toVariantDto(v)))
            .orElse(success(null));
    }

    // ====================================
    // Admin - Product Management
    // ====================================

    @GetMapping("/api/v1/admin/kitchen-products")
    @Operation(summary = "Get all products (admin)")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Page<KitchenProductResponse>>> getAllProducts(
            @ModelAttribute KitchenProductFilter filter,
            Pageable pageable) {

        Page<KitchenProduct> page = productQueryService.findAll(pageable);
        return success(page.map(productMapper::toDto));
    }

    @GetMapping("/api/v1/admin/kitchen-products/{id}")
    @Operation(summary = "Get product by ID (admin)")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenProductResponse>> getProductById(
            @PathVariable UUID id) {

        KitchenProduct product = productQueryService.findByIdWithAll(id);
        return success(buildFullProductResponse(product));
    }

    @PostMapping("/api/v1/admin/kitchen-products")
    @Operation(summary = "Create kitchen product")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenProductResponse>> createProduct(
            @Valid @RequestBody CreateKitchenProductRequest request) {

        log.info("Creating kitchen product: {}", request.getName());
        validateAndNormalize(request);

        KitchenProduct product = productCommandService.createProduct(
            request.getCollectionId(),
            request.getUnitCategoryId(),
            request.getSku(),
            request.getName(),
            request.getWidthMm(),
            request.getHeightMm(),
            request.getDepthMm(),
            Money.of(request.getBasePriceAmount(), request.getCurrency())
        );

        return created(productMapper.toDto(product));
    }

    @PutMapping("/api/v1/admin/kitchen-products/{id}")
    @Operation(summary = "Update kitchen product")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenProductResponse>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody CreateKitchenProductRequest request) {

        validateAndNormalize(request);

        KitchenProduct product = productCommandService.updateBasicInfo(
            id,
            request.getName(),
            request.getShortDescription(),
            request.getDescription()
        );

        if (request.getWidthMm() != null || request.getHeightMm() != null
                || request.getDepthMm() != null) {
            productCommandService.updateDimensions(
                id,
                request.getWidthMm(),
                request.getHeightMm(),
                request.getDepthMm()
            );
        }

        return success(productMapper.toDto(product), "Product updated");
    }

    @PostMapping("/api/v1/admin/kitchen-products/{id}/activate")
    @Operation(summary = "Activate product")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> activateProduct(@PathVariable UUID id) {
        productCommandService.activateProduct(id);
        return success(null, "Product activated");
    }

    @PostMapping("/api/v1/admin/kitchen-products/{id}/deactivate")
    @Operation(summary = "Deactivate product")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(@PathVariable UUID id) {
        productCommandService.deactivateProduct(id);
        return success(null, "Product deactivated");
    }

    @PostMapping("/api/v1/admin/kitchen-products/{id}/mark-popular")
    @Operation(summary = "Mark product as popular")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> markAsPopular(@PathVariable UUID id) {
        productCommandService.markAsPopular(id);
        return success(null, "Product marked as popular");
    }

    @PostMapping("/api/v1/admin/kitchen-products/{id}/unmark-popular")
    @Operation(summary = "Unmark product as popular")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> unmarkAsPopular(@PathVariable UUID id) {
        productCommandService.unmarkAsPopular(id);
        return success(null, "Product unmarked as popular");
    }

    @DeleteMapping("/api/v1/admin/kitchen-products/{id}")
    @Operation(summary = "Delete product")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        productCommandService.softDeleteProduct(id, getCurrentUsername());
        return success(null, "Product deleted");
    }

    // ====================================
    // Admin - Image Management
    // ====================================

    @PostMapping("/api/v1/admin/kitchen-products/{id}/images")
    @Operation(summary = "Add product image")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> addImage(
            @PathVariable UUID id,
            @RequestParam String url,
            @RequestParam(required = false) String altText,
            @RequestParam(defaultValue = "false") boolean isPrimary) {

        productCommandService.addImage(id, url, altText, isPrimary);
        return success(null, "Image added");
    }

    @DeleteMapping("/api/v1/admin/kitchen-products/{id}/images/{imageId}")
    @Operation(summary = "Remove product image")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> removeImage(
            @PathVariable UUID id,
            @PathVariable UUID imageId) {

        productCommandService.removeImage(id, imageId);
        return success(null, "Image removed");
    }

    // ====================================
    // Admin - Variant Management
    // ====================================

    @PostMapping("/api/v1/admin/kitchen-products/{productId}/variants")
    @Operation(summary = "Add product variant")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenProductVariantResponse>> addVariant(
            @PathVariable UUID productId,
            @Valid @RequestBody AddKitchenProductVariantRequest request) {

        log.info("Adding variant to product: {}", productId);
        validateAndNormalize(request);

        KitchenProductVariant variant = productCommandService.addVariant(
            productId,
            request.getSku(),
            request.getCabinetColourId(),
            request.getAssemblyType(),
            Money.of(request.getPriceAmount(), request.getCurrency())
        );

        return created(productMapper.toVariantDto(variant));
    }

    @PutMapping("/api/v1/admin/kitchen-variants/{variantId}/price")
    @Operation(summary = "Update variant price")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<KitchenProductVariantResponse>> updateVariantPrice(
            @PathVariable UUID variantId,
            @RequestParam java.math.BigDecimal priceAmount,
            @RequestParam String currency) {

        KitchenProductVariant variant = productCommandService.updateVariantPrice(
            variantId, Money.of(priceAmount, currency));
        return success(productMapper.toVariantDto(variant), "Price updated");
    }

    @PutMapping("/api/v1/admin/kitchen-variants/{variantId}/stock")
    @Operation(summary = "Update variant stock")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateVariantStock(
            @PathVariable UUID variantId,
            @RequestParam boolean inStock,
            @RequestParam(required = false) Integer quantity) {

        productCommandService.updateVariantStock(variantId, inStock, quantity);
        return success(null, "Stock updated");
    }

    @PostMapping("/api/v1/admin/kitchen-variants/{variantId}/activate")
    @Operation(summary = "Activate variant")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> activateVariant(
            @PathVariable UUID variantId) {

        productCommandService.activateVariant(variantId);
        return success(null, "Variant activated");
    }

    @PostMapping("/api/v1/admin/kitchen-variants/{variantId}/deactivate")
    @Operation(summary = "Deactivate variant")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deactivateVariant(
            @PathVariable UUID variantId) {

        productCommandService.deactivateVariant(variantId);
        return success(null, "Variant deactivated");
    }

    @PostMapping("/api/v1/admin/kitchen-variants/{variantId}/set-default")
    @Operation(summary = "Set variant as default")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('KITCHEN_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> setDefaultVariant(
            @PathVariable UUID variantId) {

        productCommandService.markVariantAsDefault(variantId);
        return success(null, "Default variant set");
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