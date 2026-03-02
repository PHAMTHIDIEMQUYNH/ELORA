package ELORA.ELORA.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired private JavaMailSender mailSender;

    public void sendInvoiceEmail(String to, String orderNo, byte[] pdfBytes) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Hóa đơn đơn hàng " + orderNo + " - ELORA");
        helper.setText("Cảm ơn bạn đã mua sắm tại ELORA. Vui lòng xem hóa đơn đính kèm.");

        helper.addAttachment("Invoice_" + orderNo + ".pdf", new ByteArrayResource(pdfBytes));
        mailSender.send(message);
    }
}