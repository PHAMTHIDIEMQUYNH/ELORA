package ELORA.ELORA.controller;

import ELORA.ELORA.dto.ApiResponse;
import ELORA.ELORA.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 1. Lấy tất cả sản phẩm đang kinh doanh (Trang chủ)
    @GetMapping("/all")
    public ApiResponse<?> getAll() {
        return ApiResponse.success(productService.getAllActiveProducts());
    }

    // 2. Xem chi tiết sản phẩm và các BIẾN THỂ (Dùng để chọn màu/size trước khi mua)
    @GetMapping("/{id}")
    public ApiResponse<?> getDetail(@PathVariable Integer id) {
        try {
            return ApiResponse.success(productService.getProductById(id));
        } catch (Exception e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }

    // 3. API Lọc, Tìm kiếm và Sắp xếp tổng hợp
    @GetMapping("/filter")
    public ApiResponse<?> filter(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String sortBy
    ) {
        return ApiResponse.success(productService.getFilteredProducts(
                keyword, categoryId, brandId, minPrice, maxPrice, minRating, sortBy
        ));
    }
}