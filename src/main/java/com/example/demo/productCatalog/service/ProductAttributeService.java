package com.example.demo.productCatalog.service;

import com.example.demo.productCatalog.dto.ProductAttributeDTO;
import com.example.demo.productCatalog.entity.ProductAttribute;
import com.example.demo.productCatalog.repository.ProductAttributeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductAttributeService {

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    // Create a new product attribute
    public ProductAttribute createProductAttribute(ProductAttribute productAttribute) {
        return productAttributeRepository.save(productAttribute);
    }

    // Get all product attributes
    //fixed
    public List<ProductAttributeDTO> getAllProductAttributes(Long productId,String lang) {
        return productAttributeRepository.findByProductIdAndLang(productId, lang);
    }


    // Update a product attribute
    public ProductAttribute updateProductAttribute(Long id, ProductAttribute updatedProductAttribute) {
        return productAttributeRepository.findById(id)
                .map(productAttribute -> {
                    productAttribute.setAttributeName(updatedProductAttribute.getAttributeName());
                    productAttribute.setAttributeValue(updatedProductAttribute.getAttributeValue());
                    return productAttributeRepository.save(productAttribute);
                })
                .orElseThrow(() -> new RuntimeException("ProductAttribute not found with id: " + id));
    }

    // Delete a product attribute by ID
    public void deleteProductAttribute(Long id) {
        if (!productAttributeRepository.existsById(id)) {
            throw new RuntimeException("ProductAttribute not found with id: " + id);
        }
        productAttributeRepository.deleteById(id);
    }

    // Find all attributes for a specific product
    public List<ProductAttribute> getAttributesByProductId(Long productId) {
        return productAttributeRepository.findByProductId(productId);
    }

    // Find attributes by attribute name
    public List<ProductAttribute> getAttributesByAttributeName(String attributeName) {
        return productAttributeRepository.findByAttributeName(attributeName);
    }

    // Find attributes by attribute name and value
    public List<ProductAttribute> getAttributesByAttributeNameAndValue(String attributeName, String attributeValue) {
        return productAttributeRepository.findByAttributeNameAndAttributeValue(attributeName, attributeValue);
    }

    // Find attributes by product ID and attribute name
    public List<ProductAttribute> getAttributesByProductIdAndAttributeName(Long productId, String attributeName) {
        return productAttributeRepository.findByProductIdAndAttributeName(productId, attributeName);
    }

    public Map<Long, List<ProductAttributeDTO>> getPricingByProductIdsAsMap(List<Long> productIds, String lang) {
        return productAttributeRepository.getAttributesByProductIds(productIds, lang).stream()
                .collect(Collectors.groupingBy(ProductAttributeDTO::getProductId));
    }
}