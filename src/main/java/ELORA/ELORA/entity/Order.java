package ELORA.ELORA.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_number")
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private UserAddress address;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private OrderStatus status;

    @Column(name = "payment_method")
    private String paymentMethod;

    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    private BigDecimal subtotal;
    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "vnp_transaction_no")
    private String vnpTransactionNo;
    @Column(name = "vnp_response_code")
    private String vnpResponseCode;
    @Column(name = "payment_status")
    private String paymentStatus;

    private String notes;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // --- QUAN TRỌNG: THÊM DÒNG NÀY ĐỂ LẤY DANH SÁCH SẢN PHẨM ---
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<OrderDetail> orderDetails;
}