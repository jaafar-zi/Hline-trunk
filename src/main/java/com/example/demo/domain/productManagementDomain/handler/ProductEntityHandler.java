package com.example.demo.domain.productManagementDomain.handler;

import com.example.demo.domain.productManagementDomain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ProductEntityHandler<T> {

    JpaRepository<T, Long> getRepository();

    Collection<T> getCollection(Product product);

    void addToCollection(Product product, T entity);

    void removeFromCollection(Product product, T entity);

    boolean checkUniqueById(Product product, T entity);
}
