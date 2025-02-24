package com.example.demo.productCatalog.controller;

import com.example.demo.productCatalog.dto.CategoryDTO;
import com.example.demo.productCatalog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(categoryService.createCategory(dto));
    }


    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }


    @GetMapping("/roots")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @GetMapping("/subcategories/{parentId}")
    public ResponseEntity<List<CategoryDTO>> getSubcategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.getSubcategories(parentId));
    }


    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
