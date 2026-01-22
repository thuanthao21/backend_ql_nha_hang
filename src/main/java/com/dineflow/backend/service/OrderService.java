package com.dineflow.backend.service;

import com.dineflow.backend.dto.OrderItemDTO;
import com.dineflow.backend.dto.OrderRequest;
import com.dineflow.backend.entity.*; // Đã bao gồm OrderStatus
import com.dineflow.backend.repository.OrderItemRepository;
import com.dineflow.backend.repository.OrderRepository;
import com.dineflow.backend.repository.ProductRepository;
import com.dineflow.backend.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
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
    private final OrderItemRepository orderItemRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // [QUAN TRỌNG] Đổi List<String> thành List<OrderStatus>
    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.UNPAID,
            OrderStatus.PENDING,
            OrderStatus.COOKING,
            OrderStatus.SERVED,
            OrderStatus.READY
    );

    // --- HÀM TẠO ĐƠN HOẶC GỌI THÊM MÓN ---
    @Transactional
    public Order createOrder(OrderRequest request) {
        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

        Order order;
        List<OrderItem> newItemsForKitchen = new ArrayList<>();

        Optional<Order> existingOrder = orderRepository.findByStatusInAndTable(ACTIVE_STATUSES, table);

        if ("OCCUPIED".equals(table.getStatus()) && existingOrder.isPresent()) {
            order = existingOrder.get();
        } else {
            order = new Order();
            order.setTable(table);
            order.setCreatedAt(LocalDateTime.now());
            order.setStatus(OrderStatus.UNPAID); // <--- SỬA LẠI
            order.setTotalAmount(BigDecimal.ZERO);
            order.setOrderItems(new ArrayList<>());

            if (!"OCCUPIED".equals(table.getStatus())) {
                table.setStatus("OCCUPIED");
                tableRepository.save(table);
            }
        }

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

            // Lưu ý: OrderItem vẫn dùng String status (nếu bạn chưa sửa Entity OrderItem)
            orderItem.setStatus("PENDING");

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            additionalAmount = additionalAmount.add(itemTotal);

            if (order.getOrderItems() == null) {
                order.setOrderItems(new ArrayList<>());
            }
            order.getOrderItems().add(orderItem);
            newItemsForKitchen.add(orderItem);
        }

        order.setTotalAmount(order.getTotalAmount().add(additionalAmount));
        Order savedOrder = orderRepository.save(order);

        messagingTemplate.convertAndSend("/topic/kitchen", savedOrder);

        return savedOrder;
    }

    // --- HÀM THANH TOÁN (TRẢ BÀN) ---
    @Transactional
    public Order checkout(Integer tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

        Order currentOrder = orderRepository.findByStatusInAndTable(ACTIVE_STATUSES, table)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn cần thanh toán!"));

        currentOrder.setStatus(OrderStatus.COMPLETED); // <--- SỬA LẠI
        table.setStatus("EMPTY");
        tableRepository.save(table);

        return orderRepository.save(currentOrder);
    }

    // --- LẤY ĐƠN HIỆN TẠI ---
    public Order getCurrentOrder(Integer tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));
        // Sửa lại cách gọi hàm Repository (Đảo ngược tham số để khớp với JPA nếu cần, hoặc giữ nguyên nếu Repo bạn viết đúng)
        return orderRepository.findByStatusInAndTable(ACTIVE_STATUSES, table).orElse(null);
    }

    // --- THANH TOÁN TỪNG MÓN ---
    @Transactional
    public void payItems(Integer orderId, List<Integer> orderItemIds) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        boolean allPaid = true;
        for (OrderItem item : order.getOrderItems()) {
            if (orderItemIds.contains(item.getId())) {
                item.setStatus("PAID");
            }
            if (!"PAID".equals(item.getStatus())) {
                allPaid = false;
            }
        }

        if (allPaid) {
            order.setStatus(OrderStatus.COMPLETED); // <--- SỬA LẠI
            order.getTable().setStatus("EMPTY");
            tableRepository.save(order.getTable());
        } else {
            order.setStatus(OrderStatus.UNPAID); // <--- SỬA LẠI
        }
        orderRepository.save(order);
    }

    // --- CẬP NHẬT TRẠNG THÁI MÓN (BẾP) ---
    @Transactional // 👈 1. Thêm cái này để đảm bảo giao dịch
    public OrderItem updateOrderItemStatus(Integer itemId, String status) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại"));

        // 2. Cập nhật và lưu xuống DB
        item.setStatus(status);
        OrderItem savedItem = orderItemRepository.save(item);

        // 3. Lấy Order cha đang nằm trong bộ nhớ (Memory)
        Order currentOrder = item.getOrder();

        // 4. [TUYỆT CHIÊU] Cập nhật thủ công vào danh sách trong bộ nhớ
        // Lý do: Nếu gọi orderRepository.findById() ngay lúc này, có thể DB vẫn trả về dữ liệu cũ.
        // Ta tự sửa trong list này để đảm bảo gửi qua Socket là chuẩn 100%.
        if (currentOrder.getOrderItems() != null) {
            for (OrderItem orderItem : currentOrder.getOrderItems()) {
                if (orderItem.getId().equals(itemId)) {
                    orderItem.setStatus(status); // Gán cứng status mới vào list
                    break;
                }
            }
        }

        // 5. Gửi dữ liệu đã chỉnh sửa đi (Chắc chắn có status mới)
        messagingTemplate.convertAndSend("/topic/kitchen", currentOrder);

        return savedItem;
    }

    // --- LOGIC CHUYỂN BÀN / GỘP BÀN ---
    @Transactional
    public void moveOrMergeTable(Integer fromTableId, Integer toTableId) {
        if (fromTableId.equals(toTableId)) {
            throw new RuntimeException("Không thể chuyển đến cùng một bàn!");
        }

        RestaurantTable fromTable = tableRepository.findById(fromTableId)
                .orElseThrow(() -> new RuntimeException("Bàn đi không tồn tại"));
        RestaurantTable toTable = tableRepository.findById(toTableId)
                .orElseThrow(() -> new RuntimeException("Bàn đến không tồn tại"));

        Optional<Order> fromOrderOpt = orderRepository.findByStatusInAndTable(ACTIVE_STATUSES, fromTable);
        Optional<Order> toOrderOpt = orderRepository.findByStatusInAndTable(ACTIVE_STATUSES, toTable);

        if (fromOrderOpt.isEmpty()) {
            throw new RuntimeException("Bàn gốc không có đơn hàng nào để chuyển!");
        }

        Order fromOrder = fromOrderOpt.get();

        if (toOrderOpt.isEmpty()) {
            // Chuyển bàn
            fromOrder.setTable(toTable);
            orderRepository.save(fromOrder);
            fromTable.setStatus("EMPTY");
            toTable.setStatus("OCCUPIED");
        } else {
            // Gộp bàn
            Order toOrder = toOrderOpt.get();
            List<OrderItem> itemsToMove = fromOrder.getOrderItems();
            for (OrderItem item : itemsToMove) {
                item.setOrder(toOrder);
            }
            orderItemRepository.saveAll(itemsToMove);

            toOrder.setTotalAmount(toOrder.getTotalAmount().add(fromOrder.getTotalAmount()));
            if (toOrder.getOrderItems() == null) toOrder.setOrderItems(new ArrayList<>());
            toOrder.getOrderItems().addAll(itemsToMove);
            orderRepository.save(toOrder);

            fromOrder.setTotalAmount(BigDecimal.ZERO);
            fromOrder.setStatus(OrderStatus.CANCELLED); // <--- SỬA LẠI
            fromOrder.setOrderItems(new ArrayList<>());
            orderRepository.save(fromOrder);

            fromTable.setStatus("EMPTY");
        }

        tableRepository.save(fromTable);
        tableRepository.save(toTable);
    }
}