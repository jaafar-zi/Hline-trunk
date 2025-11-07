package com.example.demo.domain.productManagementDomain.handler;


import com.example.demo.domain.productManagementDomain.entity.Pricing;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.repository.PricingRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public class PricingHandler implements ProductEntityHandler<Pricing> {


    private PricingRepository pricingRepository;

    @Override
    public JpaRepository<Pricing, Long> getRepository() {
        return pricingRepository;
    }

    @Override
    public Collection<Pricing> getCollection(Product product) {
        return product.getPricingList();
    }


    @Override
    public void addToCollection(Product product, Pricing entity) {
        product.getPricingList().add(entity);
        entity.setProduct(product);
    }

    @Override
    public void removeFromCollection(Product product, Pricing entity) {
        product.getPricingList().remove(entity);
        entity.removeProduct(product);
    }

    @Override
    public boolean checkUniqueById(Product product, Pricing entity) {
        return entity.getProducts().contains(product) && entity.getProducts().stream().count() == 1;
    }
}
