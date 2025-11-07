package com.example.demo.domain.productManagementDomain.handler;

import com.example.demo.domain.productManagementDomain.entity.Asset;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.repository.AssetRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public class AssetHandler implements ProductEntityHandler<Asset> {

    private AssetRepository assetRepository;

    @Override
    public JpaRepository<Asset, Long> getRepository() {
        return assetRepository;
    }

    @Override
    public Collection<Asset> getCollection(Product product) {
        return product.getAssets();
    }

    @Override
    public void addToCollection(Product product, Asset asset) {
        product.getAssets().add(asset);
        asset.setProduct(product);
    }

    @Override
    public void removeFromCollection(Product product, Asset asset) {
        product.getAssets().remove(asset);
        asset.setProduct(null);
    }


    //
    @Override
    public boolean checkUniqueById(Product product, Asset entity) {
        return false;
    }
}
