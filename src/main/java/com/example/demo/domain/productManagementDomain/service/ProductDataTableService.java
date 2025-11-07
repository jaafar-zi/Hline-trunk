package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.domain.productManagementDomain.dto.AssetDTO;
import com.example.demo.domain.productManagementDomain.dto.InventoryDTO;
import com.example.demo.domain.productManagementDomain.dto.ProductTableDTO;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.repository.AssetRepository;
import com.example.demo.domain.productManagementDomain.repository.InventoryRepository;
import com.example.demo.domain.productManagementDomain.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
@NoArgsConstructor
public class ProductDataTableService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private CategoryService categoryService;

    public Page<ProductTableDTO> getPagedProductTable(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);

        return products.map(product -> {
            InventoryDTO inventory = inventoryRepository.findByProductId(product.getId()).orElse(null);

            return ProductTableDTO.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .assetId(loadFirstAssetId(product.getId()))
                    .status(product.isPublished() ? "Published" : "Draft")
                    .stockQuantity(inventory.getQuantityInStock())
                    .reorderQuantity(inventory.getReorderQuantity())
                    .reservedQuantity(inventory.getReservedQuantity())
                    .productCategory(categoryService.mapToDTO(product.getCategory()))
                    .build();
        });
    }

    private long loadFirstAssetId(long productId){
        return assetRepository.findAssetsByProductId(productId)
                .stream()
                .map(AssetDTO::getId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(Long.MIN_VALUE);
    }


}

