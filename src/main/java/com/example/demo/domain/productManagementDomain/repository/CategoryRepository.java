package com.example.demo.domain.productManagementDomain.repository;

import com.example.demo.domain.productManagementDomain.dto.CategoryDTO;
import com.example.demo.domain.productManagementDomain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by its name.
     *
     * @param name The name of the category to search for.
     * @return An Optional containing the Category if found, otherwise empty.
     */
    Optional<Category> findByName(String name);

    /**
     * Fetches all root categories that do not have a parent category.
     *
     * @return A list of root categories with no parent category.
     */
    List<Category> findByParentCategoryIsNull();

    /**
     * Fetches all subcategories associated with a given parent category ID.
     *
     * @param parentId The ID of the parent category.
     * @return A list of categories that are subcategories of the given parent category.
     */
    List<Category> findByParentCategoryId(Long parentId);


    @Query("""
    SELECT new com.example.demo.productManagementDomain.dto.CategoryDTO(
        c.id,
        c.name,
        c.description,
        c.parentCategory.id
    )
    FROM Category c
    WHERE (:onlyRoots = false OR c.parentCategory IS NULL)
    AND (:ids IS NULL OR c.id IN :ids)
""")
    List<CategoryDTO> getCategoryDTOs(
            @Param("onlyRoots") boolean onlyRoots,
            @Param("ids") List<Long> ids
    );

}
