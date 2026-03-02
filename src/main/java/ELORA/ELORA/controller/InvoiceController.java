package ELORA.ELORA.controller;

import ELORA.ELORA.entity.Order;
import ELORA.ELORA.repository.OrderRepository;
import ELORA.ELORA.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/download/{orderId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Integer orderId) {
        try {
            // 1. Tìm đơn hàng
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));

            // 2. Sinh PDF từ InvoiceService bản nâng cấp
            byte[] pdfContents = invoiceService.createInvoicePdf(order);

            // 3. Cấu hình Header để trình duyệt hiểu đây là file PDF tải về
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            // Đặt tên file động theo mã đơn hàng (Ví dụ: HoaDon_ELORA16789.pdf)
            String filename = "HoaDon_" + order.getOrderNumber() + ".pdf";
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

            return new ResponseEntity<>(pdfContents, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}