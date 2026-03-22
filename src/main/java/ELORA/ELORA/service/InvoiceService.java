package ELORA.ELORA.service;

import ELORA.ELORA.entity.Order;
import ELORA.ELORA.entity.OrderDetail;
import ELORA.ELORA.repository.OrderDetailRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
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
        Document document = new Document(PageSize.A4, 36, 36, 50, 50);
        PdfWriter.getInstance(document, baos);
        document.open();

        String regularPath = new ClassPathResource("fonts/times.ttf").getPath();
        String boldPath = new ClassPathResource("fonts/timesbd.ttf").getPath();
        String italicPath = new ClassPathResource("fonts/timesi.ttf").getPath();

        BaseFont bfRegular = BaseFont.createFont(regularPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont bfBold = BaseFont.createFont(boldPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont bfItalic = BaseFont.createFont(italicPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        Color pinkLight = new Color(248, 187, 208);      // #f8bbd0 - Hồng pastel chính
        Color pinkSoft = new Color(252, 228, 236);       // #fce4ec - Hồng nhạt
        Color pinkVeryLight = new Color(253, 242, 248);  // #fdf2f8 - Hồng rất nhạt
        Color brownDark = new Color(45, 33, 36);         // #2D2124 - Nâu đen

        Font titleFont = new Font(bfBold, 22, Font.BOLD, pinkLight);
        Font subTitleFont = new Font(bfBold, 14, Font.BOLD, brownDark);
        Font normalFont = new Font(bfRegular, 10, Font.NORMAL, brownDark);
        Font boldFont = new Font(bfBold, 10, Font.BOLD, brownDark);
        Font smallItalicFont = new Font(bfItalic, 9, Font.NORMAL, new Color(150, 150, 150));
        Font headerTableFont = new Font(bfBold, 10, Font.BOLD, brownDark);
        Font accentFont = new Font(bfBold, 12, Font.BOLD, pinkLight);

        // 1. HEADER - Tên thương hiệu với màu hồng
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1f, 1f});

        // Logo và tên thương hiệu
        PdfPCell brandCell = new PdfPCell();
        brandCell.setBorder(Rectangle.NO_BORDER);

        Paragraph brandName = new Paragraph();
        brandName.add(new Chunk("ELORA ", titleFont));
        brandName.add(new Chunk("COSMETIC", new Font(bfRegular, 18, Font.NORMAL, pinkLight)));
        brandCell.addElement(brandName);

        Paragraph slogan = new Paragraph("Botanic Beauty", new Font(bfItalic, 10, Font.NORMAL, pinkLight));
        brandCell.addElement(slogan);
        headerTable.addCell(brandCell);

        PdfPCell infoCell = new PdfPCell(new Phrase("HÓA ĐƠN ĐIỆN TỬ\n#" + order.getOrderNumber(), new Font(bfRegular, 12, Font.NORMAL, brownDark)));
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        headerTable.addCell(infoCell);

        document.add(headerTable);

        document.add(new Paragraph("\n"));
        LineSeparator line = new LineSeparator(1f, 100, pinkLight, Element.ALIGN_CENTER, -2);
        document.add(line);
        document.add(new Paragraph("\n"));

        PdfPTable clientTable = new PdfPTable(2);
        clientTable.setWidthPercentage(100);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setBackgroundColor(pinkVeryLight);
        leftCell.setPadding(10);
        leftCell.addElement(new Phrase("KHÁCH HÀNG", boldFont));
        leftCell.addElement(new Phrase(order.getUser().getFullName(), normalFont));
        leftCell.addElement(new Phrase(order.getAddress().getRecipientPhone(), normalFont));
        leftCell.addElement(new Phrase(order.getAddress().getDetailedAddress() + ", " + order.getAddress().getWard(), normalFont));
        clientTable.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setBackgroundColor(pinkVeryLight);
        rightCell.setPadding(10);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(new Phrase("THANH TOÁN", boldFont));
        rightCell.addElement(new Phrase("Ngày đặt: " + java.time.LocalDate.now(), normalFont));
        rightCell.addElement(new Phrase("Phương thức: " + order.getPaymentMethod(), normalFont));
        clientTable.addCell(rightCell);

        document.add(clientTable);
        document.add(new Paragraph("\n\n"));

        // 3. BẢNG SẢN PHẨM
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.6f, 3.4f, 1f, 1.5f, 1.5f});
        table.setSpacingBefore(10f);

        String[] headers = {"STT", "Sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h.toUpperCase(), headerTableFont));
            cell.setBackgroundColor(pinkSoft); // #fce4ec
            cell.setBorder(Rectangle.BOTTOM);
            cell.setBorderColor(pinkLight);
            cell.setPadding(10);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());
        int stt = 1;
        for (OrderDetail d : details) {

            table.addCell(createTableCell(String.valueOf(stt++), normalFont, Element.ALIGN_CENTER, pinkLight));


            PdfPCell prodCell = new PdfPCell();
            prodCell.addElement(new Phrase(d.getProductName(), normalFont));
            if (d.getVariantInfo() != null) {
                prodCell.addElement(new Phrase("(" + d.getVariantInfo() + ")", smallItalicFont));
            }
            prodCell.setBorder(Rectangle.BOTTOM);
            prodCell.setBorderColor(pinkLight);
            prodCell.setPadding(8);
            table.addCell(prodCell);


            table.addCell(createTableCell(String.valueOf(d.getQuantity()), normalFont, Element.ALIGN_CENTER, pinkLight));
            table.addCell(createTableCell(formatCurrency(d.getPrice()), normalFont, Element.ALIGN_RIGHT, pinkLight));

            PdfPCell totalCell = createTableCell(formatCurrency(d.getTotal()), boldFont, Element.ALIGN_RIGHT, pinkLight);
            totalCell.setPhrase(new Phrase(formatCurrency(d.getTotal()), new Font(bfBold, 10, Font.BOLD, pinkLight)));
            table.addCell(totalCell);
        }
        document.add(table);


        document.add(new Paragraph("\n"));
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(40);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addSummaryRow(summaryTable, "Tạm tính:", formatCurrency(order.getSubtotal()), normalFont, pinkLight);
        if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            addSummaryRow(summaryTable, "Giảm giá:", "-" + formatCurrency(order.getDiscountAmount()), normalFont, pinkLight);
        }
        addSummaryRow(summaryTable, "Phí vận chuyển:", formatCurrency(order.getShippingFee()), normalFont, pinkLight);

        PdfPCell totalLabel = new PdfPCell(new Phrase("TỔNG CỘNG", new Font(bfBold, 12, Font.BOLD, brownDark)));
        totalLabel.setBorder(Rectangle.TOP);
        totalLabel.setBorderColor(pinkLight);
        totalLabel.setPaddingTop(10);
        totalLabel.setPaddingBottom(5);
        totalLabel.setBackgroundColor(pinkSoft);
        summaryTable.addCell(totalLabel);

        PdfPCell totalVal = new PdfPCell(new Phrase(formatCurrency(order.getTotalAmount()), new Font(bfBold, 14, Font.BOLD, pinkLight)));
        totalVal.setBorder(Rectangle.TOP);
        totalVal.setBorderColor(pinkLight);
        totalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalVal.setPaddingTop(10);
        totalVal.setPaddingBottom(5);
        totalVal.setBackgroundColor(pinkSoft);
        summaryTable.addCell(totalVal);

        document.add(summaryTable);

        document.add(new Paragraph("\n\n\n\n"));
        document.add(line);

        Paragraph thanks = new Paragraph("Cảm ơn bạn đã lựa chọn Elora Cosmetic!", new Font(bfItalic, 16, Font.NORMAL, pinkLight));
        thanks.setAlignment(Element.ALIGN_CENTER);
        document.add(thanks);

        document.add(new Paragraph("\n"));

        Paragraph footer = new Paragraph("Mọi thắc mắc vui lòng liên hệ 1900 1234 - hello@elora.vn", smallItalicFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    private PdfPCell createTableCell(String text, Font font, int alignment, Color borderColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        return cell;
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font font, Color accentColor) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, font));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setPadding(3);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, font));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellValue.setPadding(3);
        table.addCell(cellValue);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0 VNĐ";
        return String.format("%,.0f VNĐ", value);
    }
}