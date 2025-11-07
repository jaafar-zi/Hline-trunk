package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.domain.productManagementDomain.dto.ProductAttributeDTO;
import com.example.demo.domain.productManagementDomain.entity.ProductAttribute;
import com.example.demo.domain.productManagementDomain.repository.ProductAttributeRepository;
import com.example.demo.domain.productManagementDomain.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProductAttributeService {

    private final ProductAttributeRepository productAttributeRepository;
    private final ProductRepository productRepository;

    // ───────────────────────────────────────────────
    //  CREATE
    // ───────────────────────────────────────────────

    public ProductAttributeDTO createProductAttribute(ProductAttributeDTO dto) {
        ProductAttribute saved = productAttributeRepository.save(toEntity(dto));
        return toDto(saved);
    }

    // ───────────────────────────────────────────────
    //  READ
    // ───────────────────────────────────────────────

    public Page<ProductAttributeDTO> getAllProductAttributes(Pageable pageable) {
        return productAttributeRepository.findAll(pageable).map(this::toDto);
    }

    public List<ProductAttributeDTO> getAllProductAttributes(Long productId, String lang) {
        return productAttributeRepository.findProductAttributesByProductIdAndLang(productId, lang);
    }

    public ProductAttributeDTO getById(Long id) {
        ProductAttribute attribute = productAttributeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductAttribute not found with id: " + id));
        return toDto(attribute);
    }

    public List<ProductAttributeDTO> getAttributesByProductId(Long productId) {
        List<ProductAttributeDTO> collect = productAttributeRepository.findByProductId(productId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return collect;
    }

    public Page<ProductAttributeDTO> getAttributesNotAssignedToProduct(Long productId, Pageable pageable) {
        return productAttributeRepository.findAttributesNotAssignedToProduct(productId, pageable).map(this::toDto);
    }

    public List<ProductAttributeDTO> getAttributesByAttributeName(String attributeName) {
        return productAttributeRepository.findByAttributeName(attributeName)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProductAttributeDTO> getAttributesByAttributeNameAndValue(String attributeName, String attributeValue) {
        return productAttributeRepository.findByAttributeNameAndAttributeValue(attributeName, attributeValue)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProductAttributeDTO> getAttributesByProductIdAndAttributeName(Long productId, String attributeName) {
        return productAttributeRepository.findByProductIdAndAttributeName(productId, attributeName)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Map<Long, List<ProductAttributeDTO>> getPricingByProductIdsAsMap(List<Long> productIds, String lang) {
        return productAttributeRepository.getAttributesByProductIds(productIds, lang)
                .stream()
                .collect(Collectors.groupingBy(ProductAttributeDTO::getProductId));
    }

    // ───────────────────────────────────────────────
    //  UPDATE
    // ───────────────────────────────────────────────

    @Transactional
    public ProductAttributeDTO updateProductAttribute(Long id, ProductAttributeDTO dto) {
        productAttributeRepository.updateProductAttribute(
                id,
                dto.getAttributeName(),
                dto.getAttributeValue()
        );
        return getById(id); // fetch and return updated entity
    }

    // ───────────────────────────────────────────────
    //  DELETE
    // ───────────────────────────────────────────────

    public void deleteProductAttribute(Long id) {
        if (!productAttributeRepository.existsById(id)) {
            throw new RuntimeException("ProductAttribute not found with id: " + id);
        }
        productAttributeRepository.deleteById(id);
    }

    // ───────────────────────────────────────────────
    //  MAPPING
    // ───────────────────────────────────────────────

    public ProductAttribute toEntity(ProductAttributeDTO dto) {
        ProductAttribute entity = new ProductAttribute();
        entity.setId(dto.getId());
        entity.setAttributeName(dto.getAttributeName());
        entity.setAttributeValue(dto.getAttributeValue());
        entity.setLang(dto.getLang());

        if (dto.getProductId() != null) {
            productRepository.findById(dto.getProductId()).ifPresent(entity::setProduct);
        }

        return entity;
    }

    public ProductAttributeDTO toDto(ProductAttribute entity) {
        Long productId = entity.getProducts().isEmpty() ? null : entity.getProducts().get(0).getId();

        ProductAttributeDTO dto = new ProductAttributeDTO();
        dto.setId(entity.getId());
        dto.setProductId(productId);
        dto.setAttributeName(entity.getAttributeName());
        dto.setAttributeValue(entity.getAttributeValue());
        dto.setLang(entity.getLang());

        return dto;
    }
}
