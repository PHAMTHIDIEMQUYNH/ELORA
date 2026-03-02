package ELORA.ELORA.controller;

import ELORA.ELORA.entity.Order;
import ELORA.ELORA.repository.OrderRepository;
import ELORA.ELORA.service.EmailService;
import ELORA.ELORA.service.InvoiceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

                // TỰ ĐỘNG SINH HÓA ĐƠN VÀ GỬI EMAIL
                byte[] pdf = invoiceService.createInvoicePdf(order);
                emailService.sendInvoiceEmail(order.getUser().getEmail(), orderNo, pdf);

                return "Thanh toán thành công! Hóa đơn đã được gửi vào email của bạn.";
            } else {
                order.setPaymentStatus("failed");
                orderRepository.save(order);
                return "Thanh toán thất bại.";
            }
        }
        return "Không tìm thấy đơn hàng.";
    }
}