package ELORA.ELORA.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;
    private String name;
    private String description;
    private String type;
    private BigDecimal value;

    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;
    @Column(name = "max_discount_value")
    private BigDecimal maxDiscountValue;

    @Column(name = "usage_limit")
    private Integer usageLimit;
    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "start_date")
    private LocalDateTime startDate;
    @Column(name = "end_date")
    private LocalDateTime endDate;
    @Column(name = "is_active")
    private Boolean isActive = true;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}