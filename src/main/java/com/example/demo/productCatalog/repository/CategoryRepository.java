package com.example.demo.productCatalog.repository;

import com.example.demo.productCatalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name); // Find category by name

    List<Category> findByParentCategoryIsNull(); // Get all root categories

    List<Category> findByParentCategoryId(Long parentId); // Get subcategories by parentId
}

