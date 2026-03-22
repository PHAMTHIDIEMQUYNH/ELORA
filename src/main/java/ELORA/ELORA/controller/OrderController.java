package ELORA.ELORA.controller;

import ELORA.ELORA.dto.ApiResponse;
import ELORA.ELORA.dto.request.OrderRequest;
import ELORA.ELORA.entity.*;
import ELORA.ELORA.repository.*;
import ELORA.ELORA.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<?> checkout(@RequestBody OrderRequest request) {
        try {
            return ApiResponse.success(orderService.checkout(request));
        } catch (Exception e) {
            return ApiResponse.error(400, "Lỗi đặt hàng: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<List<Order>> getOrdersByUser(@PathVariable Integer userId) {
        try {
            List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
            return ApiResponse.success(userOrders);
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
        }
    }

    @PutMapping("/cancel/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Transactional
    public ApiResponse<?> cancelOrder(@PathVariable Integer orderId) {
        try {
            System.out.println("========================================");
            System.out.println("🔄 BẮT ĐẦU HỦY ĐƠN HÀNG #" + orderId);
            System.out.println("========================================");

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            System.out.println("📦 Đơn hàng: " + order.getOrderNumber());
            System.out.println("📌 Trạng thái hiện tại: " + order.getStatus().getName());
            System.out.println("💳 Phương thức thanh toán: " + order.getPaymentMethod());
            System.out.println("✅ Trạng thái thanh toán: " + order.getPaymentStatus());

            if (!"Chờ xác nhận".equals(order.getStatus().getName())) {
                System.out.println("❌ Không thể hủy: Đơn hàng không ở trạng thái chờ xác nhận");
                return ApiResponse.error(400, "Chỉ có thể hủy đơn ở trạng thái chờ xác nhận");
            }

            if (("VNPAY".equals(order.getPaymentMethod()) || "MOMO".equals(order.getPaymentMethod()))
                    && "success".equals(order.getPaymentStatus())) {
                System.out.println("❌ Không thể hủy: Đơn hàng đã thanh toán thành công");
                return ApiResponse.error(400, "Đơn hàng đã thanh toán không thể hủy");
            }

            System.out.println("🔍 Đang tìm chi tiết đơn hàng...");
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);

            System.out.println("========================================");
            System.out.println("📊 KẾT QUẢ TRUY VẤN ORDER_DETAILS");
            System.out.println("Order ID: " + orderId);
            System.out.println("Số lượng orderDetails tìm thấy: " + orderDetails.size());
            System.out.println("========================================");

            if (orderDetails.isEmpty()) {
                System.out.println("⚠️ KHÔNG TÌM THẤY CHI TIẾT ĐƠN HÀNG NÀO!");
            }

            for (int i = 0; i < orderDetails.size(); i++) {
                OrderDetail detail = orderDetails.get(i);
                System.out.println("--- Chi tiết #" + (i+1) + " ---");
                System.out.println("  - Detail ID: " + detail.getId());
                System.out.println("  - variant ID: " + (detail.getVariant() != null ? detail.getVariant().getId() : "NULL"));
                System.out.println("  - product ID: " + (detail.getProduct() != null ? detail.getProduct().getId() : "NULL"));
                System.out.println("  - quantity: " + detail.getQuantity());
                System.out.println("  - price: " + detail.getPrice());
                System.out.println("  - total: " + detail.getTotal());
            }
            System.out.println("========================================");

            int totalRestored = 0;

            for (OrderDetail detail : orderDetails) {
                if (detail.getVariant() != null) {
                    ProductVariant variant = detail.getVariant();
                    int oldStock = variant.getStock();
                    int restoreQty = detail.getQuantity();

                    System.out.println("🔄 Đang hoàn kho cho variant ID: " + variant.getId());
                    System.out.println("   - Tên biến thể: " + variant.getVariantName() + ": " + variant.getVariantValue());
                    System.out.println("   - Số lượng cần hoàn: " + restoreQty);
                    System.out.println("   - Stock hiện tại: " + oldStock);

                    variant.setStock(oldStock + restoreQty);
                    variantRepository.save(variant);

                    System.out.println("   ✅ Stock mới: " + variant.getStock());
                    System.out.println("   ✅ Đã hoàn kho +" + restoreQty + " sản phẩm cho variant ID: " + variant.getId());

                    Product product = variant.getProduct();
                    if (product != null) {
                        int oldSold = product.getSoldCount();
                        product.setSoldCount(oldSold - restoreQty);
                        productRepository.save(product);
                        System.out.println("   📊 Đã giảm sold_count: " + oldSold + " → " + product.getSoldCount());
                    }
                    totalRestored += restoreQty;

                } else if (detail.getProduct() != null) {
                    Product product = detail.getProduct();
                    int oldStock = product.getStock();
                    int restoreQty = detail.getQuantity();

                    System.out.println("🔄 Đang hoàn kho cho product ID: " + product.getId());
                    System.out.println("   - Tên sản phẩm: " + product.getName());
                    System.out.println("   - Số lượng cần hoàn: " + restoreQty);
                    System.out.println("   - Stock hiện tại: " + oldStock);

                    product.setStock(oldStock + restoreQty);
                    product.setSoldCount(product.getSoldCount() - restoreQty);
                    productRepository.save(product);

                    System.out.println("   ✅ Stock mới: " + product.getStock());
                    System.out.println("   ✅ Đã hoàn kho +" + restoreQty + " sản phẩm cho product ID: " + product.getId());
                    totalRestored += restoreQty;

                } else {
                    System.out.println("❌ LỖI: Detail ID " + detail.getId() + " không có variant và không có product!");
                }
            }

            System.out.println("========================================");
            System.out.println("📦 Tổng số sản phẩm đã hoàn kho: " + totalRestored);
            System.out.println("========================================");

            OrderStatus cancelledStatus = orderStatusRepository.findByName("Đã hủy")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái 'Đã hủy' trong database"));

            String oldStatus = order.getStatus().getName();
            order.setStatus(cancelledStatus);
            orderRepository.save(order);

            System.out.println("🔄 Cập nhật trạng thái đơn hàng: " + oldStatus + " → Đã hủy");
            System.out.println("🎉 Đã hủy đơn hàng #" + orderId + " và hoàn kho " + totalRestored + " sản phẩm thành công!");
            System.out.println("========================================");

            return ApiResponse.success("Hủy đơn hàng thành công! Đã hoàn lại " + totalRestored + " sản phẩm vào kho.");

        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ LỖI KHI HỦY ĐƠN HÀNG #" + orderId);
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");
            return ApiResponse.error(500, "Lỗi khi hủy đơn: " + e.getMessage());
        }
    }
}