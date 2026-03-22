package ELORA.ELORA.controller;

import ELORA.ELORA.dto.ApiResponse;
import ELORA.ELORA.entity.*;
import ELORA.ELORA.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderStatusRepository orderStatusRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private UserRepository userRepository;

    // =========================================================================
    // 1. QUẢN LÝ SẢN PHẨM (PRODUCTS)
    // =========================================================================

    @GetMapping("/products/all")
    public ApiResponse<?> getAllProducts() {
        return ApiResponse.success(productRepository.findAll());
    }

    @PostMapping("/products/add")
    public ApiResponse<?> addProduct(@RequestBody Product product) {
        product.setSoldCount(0);
        product.setViewCount(0);
        return ApiResponse.success(productRepository.save(product));
    }

    @PutMapping("/products/update/{id}")
    public ApiResponse<?> updateProduct(@PathVariable Integer id, @RequestBody Product req) {
        Product p = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy SP"));
        p.setName(req.getName());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        p.setDescription(req.getDescription());
        p.setIsActive(req.getIsActive());
        return ApiResponse.success(productRepository.save(p));
    }

    @PatchMapping("/products/toggle/{id}")
    public ApiResponse<?> toggleProduct(@PathVariable Integer id) {
        Product p = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy SP"));
        p.setIsActive(!p.getIsActive());
        return ApiResponse.success(productRepository.save(p));
    }

    // =========================================================================
    // 2. QUẢN LÝ ĐƠN HÀNG (ORDERS)
    // =========================================================================

    @GetMapping("/orders/all")
    public ApiResponse<?> getAllOrders() {
        return ApiResponse.success(orderRepository.findAll());
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<?> getOrderDetail(@PathVariable Integer id) {
        return ApiResponse.success(orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng")));
    }

    @PutMapping("/orders/status/{orderId}")
    public ApiResponse<?> updateOrderStatus(@PathVariable Integer orderId, @RequestParam Integer statusId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        OrderStatus status = orderStatusRepository.findById(statusId).orElseThrow(() -> new RuntimeException("Trạng thái không hợp lệ"));
        order.setStatus(status);
        return ApiResponse.success(orderRepository.save(order));
    }

    // =========================================================================
    // 3. QUẢN LÝ MÃ GIẢM GIÁ (COUPONS)
    // =========================================================================

    @GetMapping("/coupons/all")
    public ApiResponse<?> getAllCoupons() {
        return ApiResponse.success(couponRepository.findAll());
    }

    @PostMapping("/coupons/add")
    public ApiResponse<?> addCoupon(@RequestBody Coupon coupon) {
        coupon.setUsedCount(0);
        coupon.setIsActive(true);
        return ApiResponse.success(couponRepository.save(coupon));
    }

    @PatchMapping("/coupons/toggle/{id}")
    public ApiResponse<?> toggleCoupon(@PathVariable Integer id) {
        Coupon c = couponRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy mã"));
        c.setIsActive(!c.getIsActive());
        return ApiResponse.success(couponRepository.save(c));
    }

    // =========================================================================
    // 4. QUẢN LÝ DANH MỤC (CATEGORIES)
    // =========================================================================

    @GetMapping("/categories/all")
    public ApiResponse<?> getAllCategories() {
        return ApiResponse.success(categoryRepository.findAll());
    }

    @GetMapping("/categories/active")
    public ApiResponse<?> getActiveCategories() {
        return ApiResponse.success(categoryRepository.findByIsActiveTrue());
    }

    @PostMapping("/categories/add")
    public ApiResponse<?> addCategory(@RequestBody Category category) {
        return ApiResponse.success(categoryRepository.save(category));
    }

    @PutMapping("/categories/update/{id}")
    public ApiResponse<?> updateCategory(@PathVariable Integer id, @RequestBody Category req) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        c.setName(req.getName());
        c.setDescription(req.getDescription());
        c.setIsActive(req.getIsActive());
        return ApiResponse.success(categoryRepository.save(c));
    }

    @PatchMapping("/categories/toggle/{id}")
    public ApiResponse<?> toggleCategory(@PathVariable Integer id) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        c.setIsActive(!c.getIsActive());
        return ApiResponse.success(categoryRepository.save(c));
    }

    @DeleteMapping("/categories/delete/{id}")
    public ApiResponse<?> deleteCategory(@PathVariable Integer id) {
        categoryRepository.deleteById(id);
        return ApiResponse.success("Xóa danh mục thành công");
    }

    // =========================================================================
    // 5. QUẢN LÝ THƯƠNG HIỆU (BRANDS)
    // =========================================================================

    @GetMapping("/brands/all")
    public ApiResponse<?> getAllBrands() {
        return ApiResponse.success(brandRepository.findAll());
    }

    @GetMapping("/brands/active")
    public ApiResponse<?> getActiveBrands() {
        return ApiResponse.success(brandRepository.findByIsActiveTrue());
    }

    @PostMapping("/brands/add")
    public ApiResponse<?> addBrand(@RequestBody Brand brand) {
        return ApiResponse.success(brandRepository.save(brand));
    }

    @PutMapping("/brands/update/{id}")
    public ApiResponse<?> updateBrand(@PathVariable Integer id, @RequestBody Brand req) {
        Brand b = brandRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));
        b.setName(req.getName());
        b.setLogoUrl(req.getLogoUrl());
        b.setIsActive(req.getIsActive());
        return ApiResponse.success(brandRepository.save(b));
    }

    @PatchMapping("/brands/toggle/{id}")
    public ApiResponse<?> toggleBrand(@PathVariable Integer id) {
        Brand b = brandRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));
        b.setIsActive(!b.getIsActive());
        return ApiResponse.success(brandRepository.save(b));
    }

    @DeleteMapping("/brands/delete/{id}")
    public ApiResponse<?> deleteBrand(@PathVariable Integer id) {
        brandRepository.deleteById(id);
        return ApiResponse.success("Xóa thương hiệu thành công");
    }

    // =========================================================================
    // 6. QUẢN LÝ KHÁCH HÀNG (USERS)
    // =========================================================================

    @GetMapping("/users/all")
    public ApiResponse<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("fullName", user.getFullName());
            userData.put("email", user.getEmail());
            userData.put("phone", user.getPhone());
            userData.put("role", user.getRole());
            userData.put("isActive", user.getIsActive());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("avatarUrl", user.getAvatarUrl());

            // Tính số đơn hàng và tổng chi tiêu từ bảng orders
            List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

            // Lọc đơn hàng đã thanh toán thành công (payment_status = 'success')
            List<Order> successOrders = orders.stream()
                    .filter(o -> "success".equals(o.getPaymentStatus()))
                    .collect(Collectors.toList());

            int orderCount = successOrders.size();
            BigDecimal totalSpent = successOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            userData.put("orderCount", orderCount);
            userData.put("totalSpent", totalSpent);

            result.add(userData);
        }

        return ApiResponse.success(result);
    }

    @GetMapping("/users/{id}")
    public ApiResponse<?> getUserDetail(@PathVariable Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("fullName", user.getFullName());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("role", user.getRole());
        userData.put("isActive", user.getIsActive());
        userData.put("createdAt", user.getCreatedAt());

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<Order> successOrders = orders.stream()
                .filter(o -> "success".equals(o.getPaymentStatus()))
                .collect(Collectors.toList());

        int orderCount = successOrders.size();
        BigDecimal totalSpent = successOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        userData.put("orderCount", orderCount);
        userData.put("totalSpent", totalSpent);

        return ApiResponse.success(userData);
    }

    @PutMapping("/users/update/{id}")
    public ApiResponse<?> updateUser(@PathVariable Integer id, @RequestBody User req) {
        User u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        u.setIsActive(req.getIsActive());
        return ApiResponse.success(userRepository.save(u));
    }

    @PatchMapping("/users/toggle/{id}")
    public ApiResponse<?> toggleUser(@PathVariable Integer id) {
        User u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        u.setIsActive(!u.getIsActive());
        return ApiResponse.success(userRepository.save(u));
    }

    @DeleteMapping("/users/delete/{id}")
    public ApiResponse<?> deleteUser(@PathVariable Integer id) {
        userRepository.deleteById(id);
        return ApiResponse.success("Xóa khách hàng thành công");
    }

    @PostMapping("/users/add")
    public ApiResponse<?> addUser(@RequestBody User user) {
        // Mã hóa mật khẩu
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setRole("CUSTOMER");
        user.setIsActive(true);
        return ApiResponse.success(userRepository.save(user));
    }
}