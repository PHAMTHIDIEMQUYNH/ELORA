package ELORA.ELORA.controller;

import ELORA.ELORA.dto.ApiResponse;
import ELORA.ELORA.dto.request.LoginRequest;
import ELORA.ELORA.dto.request.RegisterRequest;
import ELORA.ELORA.dto.response.LoginResponse;
import ELORA.ELORA.dto.request.ForgotPasswordRequest;
import ELORA.ELORA.dto.request.ResetPasswordRequest;
import ELORA.ELORA.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private UserService userService;

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            return ApiResponse.success(userService.forgotPassword(request.getEmail()));
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ApiResponse<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            return ApiResponse.success(userService.resetPassword(request.getEmail(), request.getNewPassword()));
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/reset-page")
    public String showResetPage(@RequestParam String email) {
        // Trang đặt lại mật khẩu với màu hồng nhạt
        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>ELORA | Đặt Lại Mật Khẩu</title>" +
                "<link href='https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,600;1,400&family=Montserrat:wght@300;400;500;600;700&display=swap' rel='stylesheet'>" +
                "<link href='https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap' rel='stylesheet'>" +
                "<style>" +
                "body { margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; background: #faf9f9; min-height: 100vh; display: flex; align-items: center; justify-content: center; }" +
                ".reset-container { max-width: 450px; width: 90%; background: white; border-radius: 40px; padding: 40px; box-shadow: 0 10px 30px rgba(0,0,0,0.05); text-align: center; animation: fadeIn 0.5s ease; }" +
                "@keyframes fadeIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }" +
                ".logo { margin-bottom: 20px; }" +
                ".logo span { color: #f9a8d4; font-size: 48px; }" +
                ".logo h1 { font-family: 'Cormorant Garamond', serif; letter-spacing: 0.2em; color: #2D2124; margin: 5px 0 0; font-size: 28px; }" +
                ".logo p { font-size: 8px; letter-spacing: 0.4em; color: #f9a8d4; font-weight: 700; text-transform: uppercase; margin-top: -5px; }" +
                "h2 { font-family: 'Cormorant Garamond', serif; font-style: italic; color: #2D2124; font-size: 28px; margin: 20px 0 10px; }" +
                ".email-info { background: #fdf2f8; padding: 12px; border-radius: 50px; color: #2D2124; font-weight: 500; margin: 25px 0; font-size: 14px; border: 1px solid #fbcfe8; }" +
                ".input-group { text-align: left; margin-bottom: 25px; }" +
                "label { display: block; font-size: 11px; text-transform: uppercase; letter-spacing: 0.1em; color: #888; margin-bottom: 8px; font-weight: 600; }" +
                "input { width: 100%; padding: 15px 20px; border: 1px solid #fbcfe8; border-radius: 50px; font-size: 14px; box-sizing: border-box; transition: all 0.3s; outline: none; background: #fafafa; }" +
                "input:focus { border-color: #f9a8d4; background: white; box-shadow: 0 0 0 3px rgba(249,168,212,0.1); }" +
                "button { background: #fbcfe8; color: #2D2124; border: none; padding: 16px 30px; border-radius: 50px; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.2em; cursor: pointer; width: 100%; transition: all 0.3s; box-shadow: 0 5px 15px rgba(251,207,232,0.3); }" +
                "button:hover { background: #f9a8d4; transform: translateY(-2px); box-shadow: 0 10px 20px rgba(249,168,212,0.4); }" +
                ".back-link { margin-top: 25px; font-size: 11px; }" +
                ".back-link a { color: #f9a8d4; font-weight: 700; text-decoration: none; text-transform: uppercase; letter-spacing: 0.1em; }" +
                ".back-link a:hover { color: #f472b6; text-decoration: underline; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='reset-container'>" +
                "<div class='logo'>" +
                "<span class='material-symbols-outlined'>local_florist</span>" +
                "<h1>ELORA</h1>" +
                "<p>Botanic Beauty</p>" +
                "</div>" +
                "<h2>Đặt lại mật khẩu</h2>" +
                "<div class='email-info'>📧 " + email + "</div>" +
                "<form action='/api/auth/reset-password-web' method='POST'>" +
                "<input type='hidden' name='email' value='" + email + "'>" +
                "<div class='input-group'>" +
                "<label>Mật khẩu mới</label>" +
                "<input type='password' name='newPassword' placeholder='••••••••' required>" +
                "</div>" +
                "<button type='submit'>XÁC NHẬN ĐỔI</button>" +
                "</form>" +
                "<div class='back-link'>" +
                "<a href='http://127.0.0.1:5500/FE_EloraBeauty-main/auth/dang-nhap.html'>← QUAY LẠI ĐĂNG NHẬP</a>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    @PostMapping("/reset-password-web")
    public String resetPasswordWeb(@RequestParam String email, @RequestParam String newPassword) {
        try {
            userService.resetPassword(email, newPassword);
            return "<!DOCTYPE html>" +
                    "<html lang='vi'>" +
                    "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "<title>ELORA | Thành Công</title>" +
                    "<link href='https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,600;1,400&family=Montserrat:wght@300;400;500;600;700&display=swap' rel='stylesheet'>" +
                    "<link href='https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap' rel='stylesheet'>" +
                    "<style>" +
                    "body { margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; background: #faf9f9; min-height: 100vh; display: flex; align-items: center; justify-content: center; }" +
                    ".success-container { max-width: 450px; width: 90%; background: white; border-radius: 40px; padding: 50px 40px; box-shadow: 0 10px 30px rgba(0,0,0,0.05); text-align: center; animation: fadeIn 0.5s ease; }" +
                    "@keyframes fadeIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }" +
                    ".logo span { color: #f9a8d4; font-size: 64px; }" +
                    "h2 { font-family: 'Cormorant Garamond', serif; color: #2D2124; font-size: 32px; margin: 20px 0 15px; }" +
                    "p { color: #666; line-height: 1.8; margin-bottom: 30px; }" +
                    "a { display: inline-block; background: #fbcfe8; color: #2D2124; padding: 16px 40px; border-radius: 50px; text-decoration: none; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.2em; transition: all 0.3s; box-shadow: 0 5px 15px rgba(251,207,232,0.3); }" +
                    "a:hover { background: #f9a8d4; transform: translateY(-2px); box-shadow: 0 10px 20px rgba(249,168,212,0.4); }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='success-container'>" +
                    "<div class='logo'>" +
                    "<span class='material-symbols-outlined'>check_circle</span>" +
                    "</div>" +
                    "<h2>Thành công!</h2>" +
                    "<p>Mật khẩu của bạn đã được thay đổi.<br>Vui lòng đăng nhập lại với mật khẩu mới.</p>" +
                    "<a href='http://127.0.0.1:5500/FE_EloraBeauty-main/auth/dang-nhap.html'>ĐĂNG NHẬP NGAY</a>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
        } catch (Exception e) {
            return "<!DOCTYPE html>" +
                    "<html lang='vi'>" +
                    "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<title>ELORA | Lỗi</title>" +
                    "<link href='https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600&display=swap' rel='stylesheet'>" +
                    "<style>" +
                    "body { margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; background: #faf9f9; min-height: 100vh; display: flex; align-items: center; justify-content: center; }" +
                    ".error-container { max-width: 450px; width: 90%; background: white; border-radius: 40px; padding: 50px 40px; box-shadow: 0 10px 30px rgba(0,0,0,0.05); text-align: center; }" +
                    ".error-icon { color: #f9a8d4; font-size: 64px; margin-bottom: 20px; }" +
                    "h2 { color: #2D2124; font-size: 28px; margin: 20px 0 15px; }" +
                    "p { color: #666; line-height: 1.8; margin-bottom: 30px; }" +
                    "a { display: inline-block; background: #fbcfe8; color: #2D2124; padding: 16px 40px; border-radius: 50px; text-decoration: none; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.2em; }" +
                    "a:hover { background: #f9a8d4; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='error-container'>" +
                    "<div class='error-icon'>⚠️</div>" +
                    "<h2>Đã xảy ra lỗi!</h2>" +
                    "<p>" + e.getMessage() + "</p>" +
                    "<a href='http://127.0.0.1:5500/FE_EloraBeauty-main/auth/dang-nhap.html'>QUAY LẠI</a>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
        }
    }
}