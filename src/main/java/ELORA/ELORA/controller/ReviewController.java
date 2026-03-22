package ELORA.ELORA.controller;

import ELORA.ELORA.dto.ApiResponse;
import ELORA.ELORA.dto.request.ReviewRequest;
import ELORA.ELORA.dto.response.ReviewResponse;
import ELORA.ELORA.entity.Product;
import ELORA.ELORA.entity.Review;
import ELORA.ELORA.entity.User;
import ELORA.ELORA.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    // 1. Lấy danh sách đánh giá của một sản phẩm (Công khai)
    // Postman: GET http://localhost:8081/api/reviews/product/7
    @GetMapping("/product/{productId}")
    public ApiResponse<List<ReviewResponse>> getProductReviews(@PathVariable Integer productId) {
        List<Review> reviews = reviewRepository.findByProductIdAndIsActiveTrueOrderByCreatedAtDesc(productId);

        // Chuyển đổi từ Entity sang Response DTO để tránh lộ thông tin User nhạy cảm
        List<ReviewResponse> response = reviews.stream()
                .map(r -> new ReviewResponse(
                        r.getId(),
                        r.getUser().getFullName(),
                        r.getRating(),
                        r.getTitle(),
                        r.getContent(),
                        r.getCreatedAt()
                )).collect(Collectors.toList());

        return ApiResponse.success(response);
    }

    // 2. Thêm đánh giá mới
    // Postman: POST http://localhost:8081/api/reviews/add
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<?> addReview(@RequestBody ReviewRequest req) {
        try {
            // Kiểm tra tính hợp lệ của rating
            if (req.getRating() < 1 || req.getRating() > 5) {
                return ApiResponse.error(400, "Số sao đánh giá phải từ 1 đến 5");
            }

            Review review = new Review();

            // Mapping dữ liệu
            User user = new User();
            user.setId(req.getUserId());
            review.setUser(user);

            Product product = new Product();
            product.setId(req.getProductId());
            review.setProduct(product);

            review.setRating(req.getRating());
            review.setTitle(req.getTitle());
            review.setContent(req.getContent());
            review.setIsActive(true); // Mặc định hiển thị luôn

            Review savedReview = reviewRepository.save(review);
            return ApiResponse.success("Cảm ơn bạn đã gửi đánh giá cho sản phẩm!");

        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi khi lưu đánh giá: " + e.getMessage());
        }
    }
}