package com.dineflow.backend.controller;

import com.dineflow.backend.dto.OrderRequest;
import com.dineflow.backend.entity.Order;
import com.dineflow.backend.repository.OrderRepository;
import com.dineflow.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    // 1. Tạo đơn hàng hoặc gọi thêm món (Staff)
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request) {
        try {
            Order newOrder = orderService.createOrder(request);
            return ResponseEntity.ok("Gọi món thành công! Mã đơn: " + newOrder.getId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. Thanh toán (Staff)
    @PostMapping("/{tableId}/checkout")
    public ResponseEntity<?> checkout(@PathVariable Integer tableId) {
        try {
            Order completedOrder = orderService.checkout(tableId);
            return ResponseEntity.ok("Thanh toán thành công! Tổng tiền: " + completedOrder.getTotalAmount());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // [MỚI] 3. Lấy chi tiết đơn hàng hiện tại của một bàn (Dùng cho tab Hoá đơn/Thanh toán)


    @GetMapping("/{tableId}/current")
    public ResponseEntity<?> getCurrentOrderByTable(@PathVariable Integer tableId) {
        Order order = orderService.getCurrentOrder(tableId);

        // Nếu không có đơn, trả về 404 (Not Found) thay vì để code chạy tiếp hoặc báo lỗi 400
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(order);
    }

    // 4. Lấy danh sách đơn cho Bếp (Kitchen)
    // Logic: Lấy các đơn đang ăn (UNPAID)
    @GetMapping("/kitchen")
    public ResponseEntity<List<Order>> getKitchenOrders() {
        // Lưu ý: Kitchen có thể cần lọc sâu hơn vào trạng thái món ăn (OrderItem status)
        // Nhưng tạm thời lấy tất cả đơn chưa thanh toán
        return ResponseEntity.ok(orderRepository.findByStatusIn(List.of("UNPAID", "PENDING")));
    }

    // 5. Cập nhật trạng thái đơn (Kitchen/Admin)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer id, @RequestParam String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        order.setStatus(status);
        orderRepository.save(order);

        return ResponseEntity.ok("Cập nhật trạng thái thành công!");
    }
}