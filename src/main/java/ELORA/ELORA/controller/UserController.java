package ELORA.ELORA.controller;

import ELORA.ELORA.dto.ApiResponse;
import ELORA.ELORA.entity.User;
import ELORA.ELORA.entity.UserAddress;
import ELORA.ELORA.repository.UserAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserAddressRepository userAddressRepository;

    // 1. Lấy danh sách địa chỉ của người dùng
    // GET http://localhost:8081/api/users/addresses/1
    @GetMapping("/addresses/{userId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<?> getAddressesByUserId(@PathVariable Integer userId) {
        try {
            List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
            return ApiResponse.success(addresses);
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi lấy danh sách địa chỉ: " + e.getMessage());
        }
    }

    // 2. Thêm địa chỉ mới
    // POST http://localhost:8081/api/users/addresses/add
    @PostMapping("/addresses/add")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<?> addAddress(@RequestBody UserAddress address) {
        try {
            // Nếu đây là địa chỉ đầu tiên, hoặc được set là mặc định
            if (address.getIsDefault() == null) {
                address.setIsDefault(false);
            }

            if (address.getIsDefault()) {
                List<UserAddress> existing = userAddressRepository.findByUserId(address.getUser().getId());
                existing.forEach(a -> a.setIsDefault(false));
                userAddressRepository.saveAll(existing);
            }

            UserAddress saved = userAddressRepository.save(address);
            return ApiResponse.success(saved);
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi khi thêm địa chỉ: " + e.getMessage());
        }
    }
}