package ELORA.ELORA.controller;

import ELORA.ELORA.entity.Order;
import ELORA.ELORA.repository.OrderRepository;
import ELORA.ELORA.service.EmailService;
import ELORA.ELORA.service.InvoiceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    @Autowired private OrderRepository orderRepository;
    @Autowired private InvoiceService invoiceService;
    @Autowired private EmailService emailService;

    @GetMapping("/vnpay-callback")
    public String vnpayCallback(HttpServletRequest request) throws Exception {
        String status = request.getParameter("vnp_ResponseCode");
        String orderNo = request.getParameter("vnp_TxnRef");

        Order order = orderRepository.findByOrderNumber(orderNo).orElse(null);
        if (order != null) {
            if ("00".equals(status)) {
                order.setPaymentStatus("success");
                orderRepository.save(order);

                byte[] pdf = invoiceService.createInvoicePdf(order);
                emailService.sendInvoiceEmail(order.getUser().getEmail(), orderNo, pdf);

                return getSuccessHtml(order); // Trả về HTML thành công
            } else {
                order.setPaymentStatus("failed");
                orderRepository.save(order);
                return getFailedHtml(order.getOrderNumber());
            }
        }
        return getNotFoundHtml();
    }

    @GetMapping("/momo-return")
    public String momoReturn(
            @RequestParam("orderId") String orderId,
            @RequestParam("resultCode") String resultCode) throws Exception {

        System.out.println("MoMo return - orderId: " + orderId + ", resultCode: " + resultCode);

        Order order = orderRepository.findByOrderNumber(orderId).orElse(null);

        if (order != null) {
            if ("0".equals(resultCode)) { // 0 là thành công
                order.setPaymentStatus("success");
                orderRepository.save(order);

                // Gửi email hóa đơn
                try {
                    byte[] pdf = invoiceService.createInvoicePdf(order);
                    emailService.sendInvoiceEmail(order.getUser().getEmail(), orderId, pdf);
                    System.out.println("Đã gửi email hóa đơn cho đơn hàng: " + orderId);
                } catch (Exception e) {
                    System.err.println("Lỗi gửi email: " + e.getMessage());
                }

                return getSuccessHtml(order);

            } else {
                order.setPaymentStatus("failed");
                orderRepository.save(order);
                return getFailedHtml(orderId);
            }
        }

        return getNotFoundHtml();
    }

    @GetMapping("/momo-callback")
    public String momoIpn(
            @RequestParam("orderId") String orderId,
            @RequestParam("resultCode") String resultCode) {

        System.out.println("MoMo IPN - orderId: " + orderId + ", resultCode: " + resultCode);

        Order order = orderRepository.findByOrderNumber(orderId).orElse(null);
        if (order != null) {
            if ("0".equals(resultCode)) {
                order.setPaymentStatus("success");
                orderRepository.save(order);

                // Gửi email ở IPN nếu chưa gửi ở return
                try {
                    byte[] pdf = invoiceService.createInvoicePdf(order);
                    emailService.sendInvoiceEmail(order.getUser().getEmail(), orderId, pdf);
                } catch (Exception e) {
                    System.err.println("Lỗi gửi email IPN: " + e.getMessage());
                }
            } else {
                order.setPaymentStatus("failed");
                orderRepository.save(order);
            }
        }

        return "{\"status\":\"OK\"}";
    }


    private String getSuccessHtml(Order order) {
        String orderNo = order.getOrderNumber();
        String userEmail = (order.getUser() != null) ? order.getUser().getEmail() : "của bạn";

        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>ELORA | Thanh toán thành công</title>" +
                "<link href='https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,300;0,400;0,500;0,600;0,700;1,400&family=Montserrat:wght@200;300;400;500;600&family=Great+Vibes&display=swap' rel='stylesheet'>" +
                "<link href='https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap' rel='stylesheet'>" +
                "<style>" +
                "body { margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; background: #faf9f9; min-height: 100vh; display: flex; align-items: center; justify-content: center; }" +
                ".success-container { max-width: 480px; width: 90%; background: white; border-radius: 32px; padding: 45px 35px; box-shadow: 0 15px 35px rgba(248,187,208,0.1); text-align: center; animation: fadeIn 0.5s ease; border: 1px solid #fce4ec; }" +
                "@keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }" +
                ".icon-circle { width: 80px; height: 80px; background: #fdf2f8; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; }" +
                ".icon-circle span { font-size: 45px; color: #f8bbd0; }" +
                "h1 { font-family: 'Cormorant Garamond', serif; color: #2D2124; font-size: 32px; margin: 0 0 10px; font-weight: 500; }" +
                ".order-number { background: #fdf2f8; padding: 10px 20px; border-radius: 50px; display: inline-block; margin: 15px 0 10px; font-size: 14px; color: #2D2124; border: 1px solid #fce4ec; }" +
                ".order-number span { color: #f8bbd0; font-weight: 600; margin-left: 5px; }" +
                ".message { font-size: 15px; color: #5d4037; line-height: 1.6; margin: 20px 0 25px; background: #fdf2f8; padding: 15px; border-radius: 16px; }" +
                ".message span { color: #f8bbd0; font-weight: 500; }" +
                ".button-group { display: flex; gap: 15px; justify-content: center; margin-top: 25px; }" +
                ".btn-primary { background: #f8bbd0; color: #2D2124; border: none; padding: 14px 30px; border-radius: 50px; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; cursor: pointer; transition: all 0.3s; text-decoration: none; display: inline-flex; align-items: center; gap: 8px; box-shadow: 0 4px 12px rgba(248,187,208,0.2); }" +
                ".btn-primary:hover { background: #f9a8d4; transform: translateY(-2px); box-shadow: 0 8px 18px rgba(248,187,208,0.3); }" +
                ".btn-secondary { background: white; color: #2D2124; border: 1px solid #fce4ec; padding: 14px 30px; border-radius: 50px; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; cursor: pointer; transition: all 0.3s; text-decoration: none; display: inline-flex; align-items: center; gap: 8px; }" +
                ".btn-secondary:hover { background: #fdf2f8; border-color: #f8bbd0; }" +
                ".email-notice { font-size: 13px; color: #a0a0a0; margin-top: 25px; padding-top: 20px; border-top: 1px dashed #fce4ec; }" +
                ".email-notice span { color: #f8bbd0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='success-container'>" +
                "<div class='icon-circle'>" +
                "<span class='material-symbols-outlined'>check_circle</span>" +
                "</div>" +
                "<h1>Thanh toán thành công!</h1>" +
                "<div class='order-number'>Mã đơn hàng <span>" + orderNo + "</span></div>" +
                "<div class='message'>" +
                "<span>✨</span> Hóa đơn đã được gửi vào email của bạn <span>✨</span>" +
                "</div>" +
                "<div class='button-group'>" +
                "<a href='http://127.0.0.1:5501/FE_EloraBeauty-main/trang-chu/trang-chu.html' class='btn-primary'>" +
                "<span class='material-symbols-outlined' style='font-size: 18px;'>home</span> Về trang chủ" +
                "</a>" +
                "<a href='http://127.0.0.1:5501/FE_EloraBeauty-main/theo-doi-don-hang/theo-doi-don-hang.html' class='btn-secondary'>" +
                "<span class='material-symbols-outlined' style='font-size: 18px;'>receipt</span> Theo dõi đơn" +
                "</a>" +
                "</div>" +
                "<div class='email-notice'>" +
                "Kiểm tra email <span>" + userEmail + "</span> để nhận hóa đơn" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String getFailedHtml(String orderNo) {
        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>ELORA | Thanh toán thất bại</title>" +
                "<link href='https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,300;0,400;0,500;0,600;0,700;1,400&family=Montserrat:wght@200;300;400;500;600&family=Great+Vibes&display=swap' rel='stylesheet'>" +
                "<link href='https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap' rel='stylesheet'>" +
                "<style>" +
                "body { margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; background: #faf9f9; min-height: 100vh; display: flex; align-items: center; justify-content: center; }" +
                ".failed-container { max-width: 480px; width: 90%; background: white; border-radius: 32px; padding: 45px 35px; box-shadow: 0 15px 35px rgba(0,0,0,0.05); text-align: center; animation: fadeIn 0.5s ease; border: 1px solid #fee2e2; }" +
                "@keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }" +
                ".icon-circle { width: 80px; height: 80px; background: #fef2f2; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; }" +
                ".icon-circle span { font-size: 45px; color: #ef4444; }" +
                "h1 { font-family: 'Cormorant Garamond', serif; color: #2D2124; font-size: 32px; margin: 0 0 10px; font-weight: 500; }" +
                ".order-number { background: #fef2f2; padding: 10px 20px; border-radius: 50px; display: inline-block; margin: 15px 0 10px; font-size: 14px; color: #2D2124; border: 1px solid #fecaca; }" +
                ".order-number span { color: #ef4444; font-weight: 600; margin-left: 5px; }" +
                ".message { font-size: 15px; color: #5d4037; line-height: 1.6; margin: 20px 0 25px; background: #fef2f2; padding: 15px; border-radius: 16px; }" +
                ".button-group { display: flex; gap: 15px; justify-content: center; margin-top: 25px; }" +
                ".btn-primary { background: #f8bbd0; color: #2D2124; border: none; padding: 14px 30px; border-radius: 50px; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; cursor: pointer; transition: all 0.3s; text-decoration: none; display: inline-flex; align-items: center; gap: 8px; }" +
                ".btn-primary:hover { background: #f9a8d4; transform: translateY(-2px); }" +
                ".btn-secondary { background: white; color: #2D2124; border: 1px solid #fce4ec; padding: 14px 30px; border-radius: 50px; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; cursor: pointer; transition: all 0.3s; text-decoration: none; display: inline-flex; align-items: center; gap: 8px; }" +
                ".btn-secondary:hover { background: #fdf2f8; border-color: #f8bbd0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='failed-container'>" +
                "<div class='icon-circle'>" +
                "<span class='material-symbols-outlined'>error</span>" +
                "</div>" +
                "<h1>Thanh toán thất bại!</h1>" +
                "<div class='order-number'>Mã đơn hàng <span>" + orderNo + "</span></div>" +
                "<div class='message'>" +
                "Giao dịch không thành công. Vui lòng thử lại hoặc liên hệ hỗ trợ." +
                "</div>" +
                "<div class='button-group'>" +
                "<a href='http://127.0.0.1:5501/FE_EloraBeauty-main/trang-chu/trang-chu.html' class='btn-primary'>" +
                "<span class='material-symbols-outlined' style='font-size: 18px;'>home</span> Về trang chủ" +
                "</a>" +
                "<a href='http://127.0.0.1:5501/FE_EloraBeauty-main/gio-hang/gio-hang.html' class='btn-secondary'>" +
                "<span class='material-symbols-outlined' style='font-size: 18px;'>shopping_cart</span> Xem giỏ hàng" +
                "</a>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String getNotFoundHtml() {
        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>ELORA | Không tìm thấy đơn hàng</title>" +
                "<link href='https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,300;0,400;0,500;0,600;0,700;1,400&family=Montserrat:wght@200;300;400;500;600&family=Great+Vibes&display=swap' rel='stylesheet'>" +
                "<link href='https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap' rel='stylesheet'>" +
                "<style>" +
                "body { margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; background: #faf9f9; min-height: 100vh; display: flex; align-items: center; justify-content: center; }" +
                ".notfound-container { max-width: 480px; width: 90%; background: white; border-radius: 32px; padding: 45px 35px; box-shadow: 0 15px 35px rgba(0,0,0,0.05); text-align: center; animation: fadeIn 0.5s ease; border: 1px solid #fce4ec; }" +
                "@keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }" +
                ".icon-circle { width: 80px; height: 80px; background: #fdf2f8; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; }" +
                ".icon-circle span { font-size: 45px; color: #f8bbd0; }" +
                "h1 { font-family: 'Cormorant Garamond', serif; color: #2D2124; font-size: 32px; margin: 0 0 10px; font-weight: 500; }" +
                ".message { font-size: 15px; color: #5d4037; line-height: 1.6; margin: 20px 0 25px; background: #fdf2f8; padding: 15px; border-radius: 16px; }" +
                ".button-group { display: flex; gap: 15px; justify-content: center; margin-top: 25px; }" +
                ".btn-primary { background: #f8bbd0; color: #2D2124; border: none; padding: 14px 30px; border-radius: 50px; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; cursor: pointer; transition: all 0.3s; text-decoration: none; display: inline-flex; align-items: center; gap: 8px; }" +
                ".btn-primary:hover { background: #f9a8d4; transform: translateY(-2px); }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='notfound-container'>" +
                "<div class='icon-circle'>" +
                "<span class='material-symbols-outlined'>search</span>" +
                "</div>" +
                "<h1>Không tìm thấy đơn hàng!</h1>" +
                "<div class='message'>" +
                "Mã đơn hàng không tồn tại hoặc đã bị xóa." +
                "</div>" +
                "<a href='http://127.0.0.1:5501/FE_EloraBeauty-main/trang-chu/trang-chu.html' class='btn-primary'>" +
                "<span class='material-symbols-outlined' style='font-size: 18px;'>home</span> Về trang chủ" +
                "</a>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}