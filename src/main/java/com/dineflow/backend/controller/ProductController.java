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

    // 1. GET: Lấy danh sách
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // 2. POST: Thêm món mới
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        // Mặc định món mới là có hàng
        product.setIsAvailable(true);
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(convertToDTO(savedProduct));
    }

    // 3. PUT: Cập nhật món
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại!"));

        existingProduct.setName(productDTO.getName());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setImageUrl(productDTO.getImageUrl());
        existingProduct.setKitchenStation(productDTO.getKitchenStation());
        // Cho phép cập nhật trạng thái từ form sửa (nếu cần)
        if(productDTO.getIsAvailable() != null) {
            existingProduct.setIsAvailable(productDTO.getIsAvailable());
        }

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            existingProduct.setCategory(category);
        }

        Product savedProduct = productRepository.save(existingProduct);
        return ResponseEntity.ok(convertToDTO(savedProduct));
    }

    // [MỚI] API 3.1: Đổi nhanh trạng thái Còn hàng / Hết hàng
    @PutMapping("/{id}/availability")
    public ResponseEntity<?> toggleAvailability(@PathVariable Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Món không tồn tại"));

        // Đảo ngược trạng thái hiện tại (True -> False, False -> True)
        boolean currentStatus = product.getIsAvailable() != null ? product.getIsAvailable() : true;
        product.setIsAvailable(!currentStatus);

        productRepository.save(product);
        return ResponseEntity.ok(convertToDTO(product));
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

    // --- HELPER METHODS ---

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setKitchenStation(product.getKitchenStation());
        // [QUAN TRỌNG] Phải trả về trạng thái để Frontend biết
        dto.setIsAvailable(product.getIsAvailable());

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
        // Map trạng thái nếu có
        product.setIsAvailable(dto.getIsAvailable());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            product.setCategory(category);
        }
        return product;
    }
}