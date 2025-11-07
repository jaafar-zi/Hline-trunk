package com.example.demo.common.base.controller;

import com.example.demo.common.base.service.BaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Generic REST controller providing common CRUD endpoints.
 * Extend it in domain controllers for quick scaffolding.
 */
public abstract class BaseController<T, ID> {

    protected final BaseService<T, ID> service;

    protected BaseController(BaseService<T, ID> service) {
        this.service = service;
    }

    @GetMapping
    public List<T> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public T findById(@PathVariable ID id) {
        return service.findByIdOrThrow(id);
    }

    @PostMapping
    public T create(@RequestBody T entity) {
        return service.save(entity);
    }

    @PutMapping("/{id}")
    public T update(@PathVariable ID id, @RequestBody T entity) {
        return service.update(id, entity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable ID id) {
        service.delete(id);
    }
}
