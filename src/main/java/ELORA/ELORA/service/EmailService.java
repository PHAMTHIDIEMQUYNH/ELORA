package ELORA.ELORA.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    // 1. Gửi mail văn bản đơn giản (Dùng cho Quên mật khẩu)
    public void sendSimpleEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    // 2. Gửi mail kèm hóa đơn PDF (Dùng cho đặt hàng thành công - Module của Quỳnh)
    public void sendInvoiceEmail(String to, String orderNo, byte[] pdfBytes) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Hóa đơn đơn hàng " + orderNo + " - ELORA");
        helper.setText("Cảm ơn bạn đã mua sắm tại ELORA. Vui lòng xem hóa đơn đính kèm.");

        helper.addAttachment("Invoice_" + orderNo + ".pdf", new ByteArrayResource(pdfBytes));
        mailSender.send(message);
    }
    // Thêm vào EmailService.java

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // 'true' nghĩa là gửi dạng HTML
        mailSender.send(message);
    }
}