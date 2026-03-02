package ELORA.ELORA.service;

import ELORA.ELORA.entity.Order;
import ELORA.ELORA.entity.OrderDetail;
import ELORA.ELORA.repository.OrderDetailRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
public class InvoiceService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public byte[] createInvoicePdf(Order order) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        // === CẤU HÌNH FONT TIMES NEW ROMAN VỚI CẢ 4 BIẾN THỂ ===
        String regularPath = new ClassPathResource("fonts/times.ttf").getPath();
        String boldPath = new ClassPathResource("fonts/timesbd.ttf").getPath();
        String italicPath = new ClassPathResource("fonts/timesi.ttf").getPath();
        String boldItalicPath = new ClassPathResource("fonts/timesbi.ttf").getPath();

        BaseFont bfRegular = BaseFont.createFont(regularPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont bfBold = BaseFont.createFont(boldPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont bfItalic = BaseFont.createFont(italicPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont bfBoldItalic = BaseFont.createFont(boldItalicPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        Font titleFont = new Font(bfBold, 20, Font.BOLD, new Color(255, 20, 147));
        Font headerFont = new Font(bfBold, 12, Font.BOLD, Color.WHITE);
        Font normalFont = new Font(bfRegular, 11, Font.NORMAL, Color.BLACK);
        Font italicFont = new Font(bfItalic, 10, Font.ITALIC, Color.GRAY);
        Font boldFont = new Font(bfBold, 11, Font.BOLD, Color.BLACK);
        Font boldItalicFont = new Font(bfBoldItalic, 11, Font.BOLDITALIC, Color.BLACK);

        // 1. Header Shop
        Paragraph brandName = new Paragraph("ELORA COSMETIC", titleFont);
        brandName.setAlignment(Element.ALIGN_CENTER);
        document.add(brandName);

        Paragraph info = new Paragraph(
                "Địa chỉ: 123 Đường Mỹ Phẩm, Quận 1, TP. Hồ Chí Minh\n" +
                        "Hotline: 0987.654.321 | Email: elora.cosmetic@gmail.com",
                italicFont
        );
        info.setAlignment(Element.ALIGN_CENTER);
        document.add(info);
        document.add(new Paragraph("\n"));

        // 2. Tiêu đề hóa đơn
        Paragraph invoiceHeader = new Paragraph("HÓA ĐƠN BÁN HÀNG", new Font(bfBold, 16, Font.BOLD));
        invoiceHeader.setAlignment(Element.ALIGN_CENTER);
        document.add(invoiceHeader);

        document.add(new Paragraph("Mã đơn hàng: #" + order.getOrderNumber(), italicFont));
        document.add(new Paragraph("Ngày đặt: " + java.time.LocalDate.now(), italicFont));
        document.add(new Paragraph("Phương thức thanh toán: " + order.getPaymentMethod(), italicFont));
        document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------", italicFont));

        // 3. Thông tin khách hàng
        document.add(new Paragraph("👤 Khách hàng: " + order.getUser().getFullName(), normalFont));
        document.add(new Paragraph("📞 Số điện thoại: " + order.getAddress().getRecipientPhone(), normalFont));
        document.add(new Paragraph("📍 Địa chỉ giao hàng: " +
                order.getAddress().getDetailedAddress() + ", " +
                order.getAddress().getWard() + ", " +
                order.getAddress().getDistrict() + ", " +
                order.getAddress().getProvince(), normalFont));
        document.add(new Paragraph("\n"));

        // 4. Bảng danh sách sản phẩm
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.5f, 3f, 1f, 1.5f, 1.5f});

        String[] headers = {"STT", "Sản phẩm", "SL", "Đơn giá", "Thành tiền"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new Color(255, 20, 147));
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());
        int stt = 1;
        for (OrderDetail d : details) {
            PdfPCell sttCell = new PdfPCell(new Phrase(String.valueOf(stt++), normalFont));
            sttCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            sttCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(sttCell);

            String prodInfo = d.getProductName();
            if (d.getVariantInfo() != null && !d.getVariantInfo().isEmpty()) {
                Phrase phrase = new Phrase();
                phrase.add(new Chunk(d.getProductName(), normalFont));
                phrase.add(new Chunk("\n(" + d.getVariantInfo() + ")", italicFont));
                PdfPCell prodCell = new PdfPCell(phrase);
                prodCell.setPadding(5);
                table.addCell(prodCell);
            } else {
                PdfPCell prodCell = new PdfPCell(new Phrase(prodInfo, normalFont));
                prodCell.setPadding(5);
                table.addCell(prodCell);
            }

            PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(d.getQuantity()), normalFont));
            qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qtyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(qtyCell);

            PdfPCell priceCell = new PdfPCell(new Phrase(formatCurrency(d.getPrice()), normalFont));
            priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            priceCell.setPadding(5);
            table.addCell(priceCell);

            PdfPCell totalCell = new PdfPCell(new Phrase(formatCurrency(d.getTotal()), boldFont));
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCell.setPadding(5);
            table.addCell(totalCell);
        }
        document.add(table);

        // 5. Tổng kết
        document.add(new Paragraph("\n"));
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(45);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addTotalRow(totalTable, "Tạm tính:", order.getSubtotal(), normalFont, bfRegular);
        addTotalRow(totalTable, "Giảm giá:", order.getDiscountAmount(), normalFont, bfRegular);
        addTotalRow(totalTable, "Phí vận chuyển:", order.getShippingFee(), normalFont, bfRegular);

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TỔNG CỘNG:", boldFont));
        totalLabelCell.setBorder(Rectangle.NO_BORDER);
        totalLabelCell.setBackgroundColor(new Color(255, 230, 240));
        totalLabelCell.setPadding(5);
        totalTable.addCell(totalLabelCell);

        PdfPCell totalValueCell = new PdfPCell(new Phrase(formatCurrency(order.getTotalAmount()), new Font(bfBold, 13, Font.BOLD, new Color(255, 20, 147))));
        totalValueCell.setBorder(Rectangle.NO_BORDER);
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setBackgroundColor(new Color(255, 230, 240));
        totalValueCell.setPadding(5);
        totalTable.addCell(totalValueCell);
        document.add(totalTable);

        // 6. Ghi chú & Chân trang
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("📝 Ghi chú: Hàng hóa đã được kiểm tra kỹ trước khi giao. Quý khách vui lòng kiểm tra lại khi nhận hàng.", italicFont));

        Paragraph footer = new Paragraph("\n\nCảm ơn quý khách đã tin tưởng và ủng hộ ELORA COSMETIC!", boldItalicFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        Paragraph contact = new Paragraph("📞 Hotline: 1900.1234 | 🌐 Website: www.elora.vn", italicFont);
        contact.setAlignment(Element.ALIGN_CENTER);
        document.add(contact);

        document.close();
        return baos.toByteArray();
    }

    private void addTotalRow(PdfPTable table, String label, BigDecimal value, Font font, BaseFont bf) {
        table.addCell(new PdfPCell(new Phrase(label, font)) {{ setBorder(Rectangle.NO_BORDER); setPadding(3); }});
        table.addCell(new PdfPCell(new Phrase(formatCurrency(value), font)) {{ setBorder(Rectangle.NO_BORDER); setHorizontalAlignment(Element.ALIGN_RIGHT); setPadding(3); }});
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0 VNĐ";
        return String.format("%,.0f VNĐ", value);
    }
}