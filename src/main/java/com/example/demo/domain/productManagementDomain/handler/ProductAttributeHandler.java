package com.example.demo.domain.productManagementDomain.handler;

import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.entity.ProductAttribute;
import com.example.demo.domain.productManagementDomain.repository.ProductAttributeRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public class ProductAttributeHandler implements ProductEntityHandler<ProductAttribute> {

    private ProductAttributeRepository productAttributeRepository;

    @Override
    public JpaRepository<ProductAttribute, Long> getRepository() {
        return productAttributeRepository;
    }

    @Override
    public Collection<ProductAttribute> getCollection(Product product) {
        return product.getAttributes();
    }

    @Override
    public void addToCollection(Product product, ProductAttribute entity) {
        product.getAttributes().add(entity);
        entity.setProduct(product);
    }

    @Override
    public void removeFromCollection(Product product, ProductAttribute entity) {
        product.getAttributes().remove(entity);
        entity.removeProduct(product);
    }

    @Override
    public boolean checkUniqueById(Product product, ProductAttribute entity) {
       return entity.getProducts().contains(product) && entity.getProducts().contains(product);
    }
}
