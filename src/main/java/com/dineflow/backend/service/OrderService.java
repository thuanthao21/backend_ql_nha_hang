package com.dineflow.backend.service;

import com.dineflow.backend.dto.OrderItemDTO;
import com.dineflow.backend.dto.OrderRequest;
import com.dineflow.backend.entity.*;
import com.dineflow.backend.repository.OrderItemRepository;
import com.dineflow.backend.repository.OrderRepository;
import com.dineflow.backend.repository.ProductRepository;
import com.dineflow.backend.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RestaurantTableRepository tableRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // [QUAN TRỌNG] Danh sách các trạng thái được coi là "Khách đang ăn/Chưa trả tiền"
    // Phải bao gồm cả COOKING (đang nấu) và SERVED (đã ra món)
    private static final List<String> ACTIVE_STATUSES = List.of("UNPAID", "PENDING", "COOKING", "SERVED");

    // --- HÀM TẠO ĐƠN HOẶC GỌI THÊM MÓN ---
    @Transactional
    public Order createOrder(OrderRequest request) {
        // 1. Tìm bàn
        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

        Order order;
        List<OrderItem> newItemsForKitchen = new ArrayList<>(); // Danh sách chứa món MỚI gọi để gửi bếp

        // 2. [LOGIC MỚI - TỰ ĐỘNG SỬA LỖI]
        // Tìm xem có đơn nào đang ăn dở không (Dựa trên list ACTIVE_STATUSES đầy đủ)
        Optional<Order> existingOrder = orderRepository.findByTableAndStatusIn(table, ACTIVE_STATUSES);

        // Kiểm tra logic:
        if ("OCCUPIED".equals(table.getStatus()) && existingOrder.isPresent()) {
            // TRƯỜNG HỢP 1: Cộng dồn vào đơn cũ (dù đang nấu hay đã ra món cũng cộng được)
            order = existingOrder.get();
        } else {
            // TRƯỜNG HỢP 2: Tạo đơn mới hoàn toàn (Khách mới hoặc sửa lỗi data bàn ảo)
            order = new Order();
            order.setTable(table);
            order.setCreatedAt(LocalDateTime.now());
            order.setStatus("UNPAID");
            order.setTotalAmount(BigDecimal.ZERO);
            order.setOrderItems(new ArrayList<>());

            // Cập nhật trạng thái bàn thành CÓ KHÁCH (nếu chưa phải)
            if (!"OCCUPIED".equals(table.getStatus())) {
                table.setStatus("OCCUPIED");
                tableRepository.save(table);
            }
        }

        // 3. Xử lý danh sách món ăn được gửi lên
        BigDecimal additionalAmount = BigDecimal.ZERO;

        for (OrderItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại ID: " + itemDTO.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setNote(itemDTO.getNote());
            orderItem.setPriceAtPurchase(product.getPrice());
            orderItem.setStatus("PENDING"); // Trạng thái món: Chờ bếp xác nhận

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            additionalAmount = additionalAmount.add(itemTotal);

            // Thêm vào danh sách tổng của đơn hàng
            if (order.getOrderItems() == null) {
                order.setOrderItems(new ArrayList<>());
            }
            order.getOrderItems().add(orderItem);

            // Thêm vào danh sách tạm để gửi thông báo riêng cho bếp (chỉ món mới)
            newItemsForKitchen.add(orderItem);
        }

        // 4. Cộng tiền món mới vào tổng tiền đơn hàng
        order.setTotalAmount(order.getTotalAmount().add(additionalAmount));

        Order savedOrder = orderRepository.save(order);

        // 5. Gửi WebSocket cho Bếp
        messagingTemplate.convertAndSend("/topic/kitchen", savedOrder);

        return savedOrder;
    }

    // --- HÀM THANH TOÁN (TRẢ BÀN) ---
    @Transactional
    public Order checkout(Integer tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

        // [SỬA] Tìm tất cả đơn active (kể cả đang nấu hay đã ra món) để thanh toán
        Order currentOrder = orderRepository.findByTableAndStatusIn(table, ACTIVE_STATUSES)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn cần thanh toán!"));

        currentOrder.setStatus("PAID"); // Chốt đơn
        // currentOrder.setPaymentTime(LocalDateTime.now());

        table.setStatus("EMPTY"); // Trả bàn
        tableRepository.save(table);

        return orderRepository.save(currentOrder);
    }

    // --- LẤY ĐƠN HIỆN TẠI (Để hiện hóa đơn) ---
    public Order getCurrentOrder(Integer tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

        // [SỬA] Tìm tất cả trạng thái active để hiển thị hóa đơn đúng
        return orderRepository.findByTableAndStatusIn(table, ACTIVE_STATUSES)
                .orElse(null);
    }

    @Transactional
    public void payItems(Integer orderId, List<Integer> orderItemIds) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // 1. Duyệt qua các món được chọn để thanh toán
        boolean allPaid = true;
        for (OrderItem item : order.getOrderItems()) {
            // Nếu món này nằm trong danh sách được chọn -> Đánh dấu ĐÃ TRẢ TIỀN
            if (orderItemIds.contains(item.getId())) {
                item.setStatus("PAID");
            }

            // Kiểm tra xem còn món nào chưa trả tiền không?
            if (!"PAID".equals(item.getStatus())) {
                allPaid = false;
            }
        }

        // 2. Nếu TẤT CẢ món trong đơn đã là PAID -> Đóng bàn, Hoàn tất đơn
        if (allPaid) {
            order.setStatus("COMPLETED"); // Đổi thành COMPLETED để hiện trong báo cáo
            order.getTable().setStatus("EMPTY"); // Trả bàn
            tableRepository.save(order.getTable());
        } else {
            // Nếu chưa trả hết -> Vẫn giữ bàn là OCCUPIED, đơn là UNPAID (hoặc PARTIAL_PAID tùy bạn)
            order.setStatus("UNPAID");
        }
        orderRepository.save(order);

    }
    @Autowired
    private OrderItemRepository orderItemRepository; // Nhớ Autowired Repository này

    // [MỚI] Cập nhật trạng thái từng món
    public OrderItem updateOrderItemStatus(Integer itemId, String status) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại"));

        item.setStatus(status);

        // Logic phụ: Nếu tất cả món đã xong (SERVED) -> Update luôn Order thành SERVED (tuỳ chọn)
        // ...

        OrderItem savedItem = orderItemRepository.save(item);

        // Gửi socket báo cho Bếp/Thu ngân biết là món này đã đổi trạng thái (để UI tự cập nhật)
        messagingTemplate.convertAndSend("/topic/kitchen", savedItem.getOrder());

        return savedItem;
    }
}