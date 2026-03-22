package ELORA.ELORA.controller;

import ELORA.ELORA.dto.ApiResponse;
import ELORA.ELORA.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    // Lấy danh sách tất cả các mã giảm giá đang hoạt động
    @GetMapping("/available")
    public ApiResponse<?> getAvailableCoupons(@RequestParam BigDecimal totalAmount) {
        return ApiResponse.success(couponService.getAvailableCoupons(totalAmount));
    }

    // Kiểm tra thử một mã giảm giá có hợp lệ không
    @GetMapping("/validate")
    public ApiResponse<?> validateCoupon(@RequestParam String code, @RequestParam BigDecimal totalAmount) {
        try {
            return ApiResponse.success(couponService.validateCoupon(code, totalAmount));
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}