package ELORA.ELORA.service;

import ELORA.ELORA.config.JwtUtils;
import ELORA.ELORA.dto.request.LoginRequest;
import ELORA.ELORA.dto.request.RegisterRequest;
import ELORA.ELORA.dto.response.LoginResponse;
import ELORA.ELORA.entity.User;
import ELORA.ELORA.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtils jwtUtils;

    @Autowired private EmailService emailService;

    public User register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole("CUSTOMER");
        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String token = jwtUtils.generateToken(user.getUsername(), user.getRole());
            return new LoginResponse(token, user.getUsername(), user.getRole(), user.getId());
        }
        throw new RuntimeException("Mật khẩu không chính xác");
    }

    public String forgotPassword(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));

        String subject = "[ELORA COSMETIC] - ĐẶT LẠI MẬT KHẨU";

        String resetLink = "http://localhost:8081/api/auth/reset-page?email=" + email;

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #f0f0f0; padding: 40px; text-align: center;'>"
                + "<h2 style='color: #2D2124; letter-spacing: 2px;'>ELORA COSMETIC</h2>"
                + "<p style='color: #666; font-size: 16px;'>Chào bạn,</p>"
                + "<p style='color: #666;'>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Vui lòng nhấn vào nút bên dưới để tiến hành:</p>"
                + "<div style='margin: 40px 0;'>"
                // ĐỔI MÀU background sang #2D2124 và border-radius sang 50px để bo tròn
                + "<a href='" + resetLink + "' style='background-color: #2D2124; color: #ffffff; padding: 15px 35px; text-decoration: none; border-radius: 50px; font-weight: bold; font-size: 12px; letter-spacing: 1px; display: inline-block; text-transform: uppercase;'>ĐẶT LẠI MẬT KHẨU</a>"
                + "</div>"
                + "<p style='color: #999; font-size: 13px;'>Nếu bạn không yêu cầu điều này, vui lòng bỏ qua email.</p>"
                + "<hr style='border: 0; border-top: 1px solid #eee; margin: 30px 0;'>"
                + "<p style='font-size: 11px; color: #bbb; text-transform: uppercase;'>Nâng niu vẻ đẹp tự nhiên của bạn</p>"
                + "</div>";

        try {
            emailService.sendHtmlEmail(email, "[ELORA] - Khôi phục mật khẩu tài khoản", htmlContent);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage());
        }

        return "Email hướng dẫn đã được gửi tới: " + email;
    }

    public String resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không hợp lệ!"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Mật khẩu đã được thay đổi thành công!";
    }
}