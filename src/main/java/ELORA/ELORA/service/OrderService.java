package ELORA.ELORA.service;

import ELORA.ELORA.dto.request.OrderRequest;
import ELORA.ELORA.dto.response.OrderResponse;
import ELORA.ELORA.entity.*;
import ELORA.ELORA.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private OrderStatusRepository orderStatusRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private VNPayService vnPayService;
    @Autowired private MomoService momoService;
    @Autowired private CouponService couponService;

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse checkout(OrderRequest request) throws Exception {
        // 1. Lấy giỏ hàng của người dùng
        List<Cart> cartItems = cartRepository.findByUserId(request.getUserId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang trống!");
        }

        // ========== 2. KIỂM TRA TỒN KHO LẦN CUỐI VỚI LOCK ==========
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Cart item : cartItems) {
            if (item.getVariant() != null) {
                // Dùng LOCK để khóa bản ghi variant
                ProductVariant variant = variantRepository.findByIdWithLock(item.getVariant().getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));

                // Kiểm tra tồn kho
                if (item.getQuantity() > variant.getStock()) {
                    throw new RuntimeException("Sản phẩm " + item.getProduct().getName() +
                            " chỉ còn " + variant.getStock() + " sản phẩm trong kho!");
                }

                // Lưu variant đã khóa để dùng sau
                item.setVariant(variant);

                BigDecimal unitPrice = variant.getPrice();
                subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));

            } else {
                // Dùng LOCK cho product (nếu không có biến thể)
                Product product = productRepository.findByIdWithLock(item.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

                if (item.getQuantity() > product.getStock()) {
                    throw new RuntimeException("Sản phẩm " + product.getName() +
                            " chỉ còn " + product.getStock() + " sản phẩm trong kho!");
                }

                item.setProduct(product);

                BigDecimal unitPrice = product.getPrice();
                subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        // 3. Xử lý Mã giảm giá (Coupon) - Giữ nguyên
        BigDecimal discountAmount = BigDecimal.ZERO;
        Coupon appliedCoupon = null;
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            appliedCoupon = couponService.validateCoupon(request.getCouponCode(), subtotal);

            if ("PERCENT".equals(appliedCoupon.getType())) {
                discountAmount = subtotal.multiply(appliedCoupon.getValue()).divide(new BigDecimal(100));
                if (appliedCoupon.getMaxDiscountValue() != null && discountAmount.compareTo(appliedCoupon.getMaxDiscountValue()) > 0) {
                    discountAmount = appliedCoupon.getMaxDiscountValue();
                }
            } else if ("FIXED".equals(appliedCoupon.getType())) {
                discountAmount = appliedCoupon.getValue();
            }
        }

        // 4. Tính toán tổng cuối cùng
        BigDecimal shippingFee = new BigDecimal(30000);
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(shippingFee);

        // 5. Tạo và lưu Đơn hàng (Order)
        Order order = new Order();
        order.setOrderNumber("ELORA" + System.currentTimeMillis());

        User user = new User(); user.setId(request.getUserId()); order.setUser(user);
        UserAddress address = new UserAddress(); address.setId(request.getAddressId()); order.setAddress(address);

        order.setStatus(orderStatusRepository.findById(1).orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy trạng thái đơn hàng mặc định")));

        order.setPaymentMethod(request.getPaymentMethod());
        order.setCoupon(appliedCoupon);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        order.setPaymentStatus("pending");
        order.setNotes(request.getNotes());

        order = orderRepository.save(order);

        // ========== 6. TRỪ KHO (SAU KHI ĐÃ KHÓA) ==========
        for (Cart item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setVariant(item.getVariant());
            detail.setProductName(item.getProduct().getName());

            String variantInfo = "";
            if (item.getVariant() != null) {
                variantInfo = item.getVariant().getVariantName() + ": " + item.getVariant().getVariantValue();
            }
            detail.setVariantInfo(variantInfo);
            detail.setQuantity(item.getQuantity());

            BigDecimal price = (item.getVariant() != null) ? item.getVariant().getPrice() : item.getProduct().getPrice();
            detail.setPrice(price);
            detail.setTotal(price.multiply(BigDecimal.valueOf(item.getQuantity())));

            orderDetailRepository.save(detail);

            // TRỪ KHO
            if (item.getVariant() != null) {
                ProductVariant v = item.getVariant();
                v.setStock(v.getStock() - item.getQuantity());
                variantRepository.save(v);

                Product p = v.getProduct();
                p.setSoldCount(p.getSoldCount() + item.getQuantity());
                productRepository.save(p);
            } else {
                Product p = item.getProduct();
                p.setStock(p.getStock() - item.getQuantity());
                p.setSoldCount(p.getSoldCount() + item.getQuantity());
                productRepository.save(p);
            }
        }

        // 7. Cập nhật số lần sử dụng của Coupon
        if (appliedCoupon != null) {
            appliedCoupon.setUsedCount(appliedCoupon.getUsedCount() + 1);
            couponRepository.save(appliedCoupon);
        }

        // 8. Xóa giỏ hàng sau khi đặt thành công
        cartRepository.deleteByUserId(request.getUserId());

        // 9. Chuẩn bị phản hồi
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setTotalAmount(order.getTotalAmount());

        // XỬ LÝ PHƯƠNG THỨC THANH TOÁN ĐỂ TRẢ VỀ LINK
        if ("VNPAY".equals(request.getPaymentMethod())) {
            String vnpayUrl = vnPayService.createPaymentUrl(order);
            response.setPaymentUrl(vnpayUrl);
        } else if ("MOMO".equals(request.getPaymentMethod())) {
            String momoUrl = momoService.createPaymentUrl(order);
            response.setPaymentUrl(momoUrl);
        }

        return response;
    }
}