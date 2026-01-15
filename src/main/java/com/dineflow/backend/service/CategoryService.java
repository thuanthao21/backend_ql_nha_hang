package com.dineflow.backend.service;

import com.dineflow.backend.entity.Category;
import com.dineflow.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 1. Lấy tất cả danh mục (Dùng cho Dropdown)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 2. Tạo danh mục mới (Có kiểm tra trùng tên)
    public Category createCategory(Category category) {
        // Kiểm tra xem tên đã tồn tại chưa
        Category existingCategory = categoryRepository.findByName(category.getName());
        if (existingCategory != null) {
            throw new RuntimeException("Tên danh mục '" + category.getName() + "' đã tồn tại!");
        }
        return categoryRepository.save(category);
    }

    // 3. Xóa danh mục (Tùy chọn thêm)
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Danh mục không tồn tại!");
        }
        categoryRepository.deleteById(id);
    }

    // Thêm hàm update
    public Category updateCategory(Integer id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        category.setName(categoryDetails.getName());
        // Nếu muốn update cả parent thì set ở đây
        // category.setParentCategory(categoryDetails.getParentCategory());

        return categoryRepository.save(category);
    }
}