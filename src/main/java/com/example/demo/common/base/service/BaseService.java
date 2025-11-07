package com.example.demo.common.base.service;

import java.util.List;
import java.util.Optional;

/**
 * Generic service interface defining CRUD operations.
 * Implemented by AbstractBaseService and extended by domain services.
 */
public interface BaseService<T, ID> {

    T save(T entity);

    T update(ID id, T entity);

    T findByIdOrThrow(ID id);

    Optional<T> findById(ID id);

    List<T> findAll();

    void delete(ID id);
}
