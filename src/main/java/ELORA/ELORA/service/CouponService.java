package ELORA.ELORA.service;

import ELORA.ELORA.entity.Coupon;
import ELORA.ELORA.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {
    @Autowired private CouponRepository couponRepository;

    public List<Coupon> getAvailableCoupons(BigDecimal currentTotal) {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findAll().stream()
                .filter(c -> c.getIsActive()
                        && c.getStartDate().isBefore(now)
                        && c.getEndDate().isAfter(now)
                        && c.getUsedCount() < c.getUsageLimit())
                .collect(Collectors.toList());

    }

    public Coupon validateCoupon(String code, BigDecimal currentTotal) {
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại hoặc đã hết hạn"));

        if (currentTotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new RuntimeException("Đơn hàng phải tối thiểu " + coupon.getMinOrderValue() + " để dùng mã này");
        }

        if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Mã giảm giá này đã hết lượt sử dụng");
        }

        return coupon;
    }
}