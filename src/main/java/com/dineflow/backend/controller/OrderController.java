package com.dineflow.backend.controller;

import com.dineflow.backend.dto.OrderRequest;
import com.dineflow.backend.entity.Order;
import com.dineflow.backend.entity.OrderStatus;
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

    // 1. Tạo đơn (Staff gọi món)
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request) {
        try {
            Order newOrder = orderService.createOrder(request);
            return ResponseEntity.ok("Gọi món thành công! Mã đơn: " + newOrder.getId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. Thanh toán (Checkout)
    @PostMapping("/{tableId}/checkout")
    public ResponseEntity<?> checkout(@PathVariable Integer tableId) {
        try {
            Order completedOrder = orderService.checkout(tableId);
            return ResponseEntity.ok("Thanh toán thành công! Tổng: " + completedOrder.getTotalAmount());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Lấy đơn hiện tại của bàn (Tránh lỗi null)
    @GetMapping("/{tableId}/current")
    public ResponseEntity<?> getCurrentOrderByTable(@PathVariable Integer tableId) {
        try {
            Order order = orderService.getCurrentOrder(tableId);
            if (order == null) {
                // Trả về 204 (No Content) nếu bàn trống -> Frontend sẽ không báo lỗi đỏ
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Lấy danh sách cho Bếp
    @GetMapping("/kitchen")
    public ResponseEntity<List<Order>> getKitchenOrders() {
        // Lấy các đơn chưa hoàn thành để bếp làm
        return ResponseEntity.ok(orderRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(OrderStatus.UNPAID, OrderStatus.PENDING, OrderStatus.COOKING)
        ));
    }

    // 5. Cập nhật trạng thái ĐƠN HÀNG (Dùng cho Admin hoặc Bếp khi xong cả bàn)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer id, @RequestParam OrderStatus status) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
            order.setStatus(status);
            orderRepository.save(order);
            return ResponseEntity.ok("Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 6. Thanh toán từng món (Tách bill)
    @PostMapping("/{orderId}/pay-items")
    public ResponseEntity<?> payItems(@PathVariable Integer orderId, @RequestBody List<Integer> orderItemIds) {
        try {
            orderService.payItems(orderId, orderItemIds);
            return ResponseEntity.ok("Thanh toán món thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 7. Cập nhật trạng thái TỪNG MÓN (Bếp dùng cái này: PENDING -> READY)
    @PutMapping("/items/{itemId}/status")
    public ResponseEntity<?> updateItemStatus(@PathVariable Integer itemId, @RequestParam String status) {
        try {
            // Service sẽ gửi WebSocket ở đây
            return ResponseEntity.ok(orderService.updateOrderItemStatus(itemId, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 8. Chuyển bàn / Gộp bàn
    @PostMapping("/move")
    public ResponseEntity<?> moveTable(@RequestParam Integer fromTableId, @RequestParam Integer toTableId) {
        try {
            orderService.moveOrMergeTable(fromTableId, toTableId);
            return ResponseEntity.ok("Thao tác thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}