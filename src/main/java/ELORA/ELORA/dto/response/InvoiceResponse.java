package ELORA.ELORA.dto.response;
import lombok.Data;
@Data public class InvoiceResponse {
    private String invoiceNumber; private String pdfPath; private Boolean sentEmail;
}