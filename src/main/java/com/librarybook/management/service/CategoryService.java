package com.librarybook.management.service;

import com.librarybook.management.entity.Category;
import com.librarybook.management.exception.BusinessException;
import com.librarybook.management.exception.ResourceNotFoundException;
import com.librarybook.management.exception.DuplicateResourceException;
import com.librarybook.management.repository.CategoryRepository;
import com.librarybook.management.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public CategoryService(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    public Category createCategory(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new BusinessException("Category name is required");
        }
        category.setName(category.getName().trim());
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new DuplicateResourceException("Category name already exists");
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category category) {
        Category existing = getCategoryById(id);
        if (category.getName() != null) {
            String name = category.getName().trim();
            if (name.isBlank()) {
                throw new BusinessException("Category name is required");
            }
            if (!name.equalsIgnoreCase(existing.getName()) && categoryRepository.existsByNameIgnoreCase(name)) {
                throw new DuplicateResourceException("Category name already exists");
            }
            existing.setName(name);
        }
        if (category.getDescription() != null) existing.setDescription(category.getDescription());
        return categoryRepository.save(existing);
    }

    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        if (bookRepository.existsByCategoryId(id)) {
            throw new BusinessException("Cannot delete a category that still has books");
        }
        categoryRepository.delete(category);
    }
}
