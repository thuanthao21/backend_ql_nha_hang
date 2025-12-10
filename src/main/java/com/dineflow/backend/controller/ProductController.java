package com.dineflow.backend.controller;

import com.dineflow.backend.dto.ProductDTO;
import com.dineflow.backend.entity.Category;
import com.dineflow.backend.entity.Product;
import com.dineflow.backend.repository.CategoryRepository;
import com.dineflow.backend.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // 1. GET: Lấy danh sách (Trả về DTO)
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        // Convert List<Entity> -> List<DTO>
        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // 2. POST: Thêm món mới (Nhận DTO, Validate)
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(convertToDTO(savedProduct));
    }

    // 3. PUT: Cập nhật món
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại!"));

        // Cập nhật thông tin
        existingProduct.setName(productDTO.getName());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setImageUrl(productDTO.getImageUrl());
        existingProduct.setKitchenStation(productDTO.getKitchenStation());

        // Cập nhật danh mục (nếu có)
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            existingProduct.setCategory(category);
        }

        Product savedProduct = productRepository.save(existingProduct);
        return ResponseEntity.ok(convertToDTO(savedProduct));
    }

    // 4. DELETE: Xóa món
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Món ăn không tồn tại!");
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa thành công!");
    }

    // --- HELPER METHODS (Mapping thủ công) ---
    // Sau này đi làm em sẽ dùng thư viện MapStruct, còn giờ viết tay để hiểu bản chất

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setKitchenStation(product.getKitchenStation());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        return dto;
    }

    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setKitchenStation(dto.getKitchenStation());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            product.setCategory(category);
        }
        return product;
    }
}