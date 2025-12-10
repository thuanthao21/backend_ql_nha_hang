package com.dineflow.backend.service;

import com.dineflow.backend.entity.*;
import com.dineflow.backend.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DatabaseSeeder {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantTableRepository tableRepository; // Mới thêm
    private final AreaRepository areaRepository;           // Mới thêm

    public DatabaseSeeder(CategoryRepository categoryRepository,
                          ProductRepository productRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          RestaurantTableRepository tableRepository,
                          AreaRepository areaRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tableRepository = tableRepository;
        this.areaRepository = areaRepository;
    }

    @PostConstruct
    @Transactional
    public void seedDatabase() {
        seedUsers();
        seedMenu();
        seedTables(); // Chạy hàm tạo bàn
    }

    private void seedTables() {
        if (tableRepository.count() == 0) {
            // Tạo Khu vực
            Area area = new Area();
            area.setName("Tầng 1");
            areaRepository.save(area);

            // Tạo 10 cái bàn
            for (int i = 1; i <= 10; i++) {
                RestaurantTable table = new RestaurantTable();
                table.setName("Bàn " + i);
                table.setStatus("EMPTY");
                table.setArea(area);
                tableRepository.save(table);
            }
            System.out.println("-> Đã tạo 10 bàn ăn mẫu");
        }
    }

    // ... (Giữ nguyên các hàm seedUsers, seedMenu, createCategory, createProduct cũ ở dưới)
    // Nếu em lỡ xóa thì báo tôi gửi lại, nhưng tôi nghĩ em chỉ cần thêm phần seedTables vào thôi.
    // Để an toàn, tôi gửi luôn phần User bên dưới cho đủ bộ:

    private void seedUsers() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setFullName("Admin");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            User staff = new User();
            staff.setUsername("staff");
            staff.setPassword(passwordEncoder.encode("123456"));
            staff.setFullName("Staff");
            staff.setRole(Role.STAFF);
            userRepository.save(staff);
            System.out.println("-> Đã tạo User mẫu");
        }
    }

    private void seedMenu() {
        if (productRepository.count() == 0) {
            // Logic tạo menu cũ của em...
            System.out.println("-> Đã kiểm tra Menu");
        }
    }
}