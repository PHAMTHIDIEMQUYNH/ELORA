package ELORA.ELORA.controller;

import ELORA.ELORA.dto.ApiResponse;
import ELORA.ELORA.dto.request.CartRequest;
import ELORA.ELORA.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // 1. API THÊM VÀO GIỎ
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<?> addToCart(@RequestBody CartRequest request) {
        try {
            return ApiResponse.success(cartService.addToCart(request));
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 2. API CẬP NHẬT SỐ LƯỢNG (Dùng cho nút +/-)
    @PutMapping("/update-quantity/{cartId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<?> updateQuantity(@PathVariable Integer cartId, @RequestParam Integer quantity) {
        try {
            return ApiResponse.success(cartService.updateQuantity(cartId, quantity));
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 3. API LẤY GIỎ HÀNG CỦA TÔI
    @GetMapping("/my-cart/{userId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<?> getMyCart(@PathVariable Integer userId) {
        return ApiResponse.success(cartService.getMyCart(userId));
    }

    // 4. API XÓA 1 SẢN PHẨM (Nút dấu X)
    @DeleteMapping("/remove/{cartId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<?> removeFromCart(@PathVariable Integer cartId) {
        try {
            cartService.removeFromCart(cartId);
            return ApiResponse.success("Đã xóa sản phẩm khỏi giỏ hàng thành công");
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi khi xóa: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear/{userId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<?> clearCart(@PathVariable Integer userId) {
        try {
            cartService.clearCart(userId);
            return ApiResponse.success("Đã xóa sạch giỏ hàng của bạn");
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi: " + e.getMessage());
        }
    }
}