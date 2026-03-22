package ELORA.ELORA.dto.response;
import lombok.Data;
import java.math.BigDecimal;
@Data public class OrderResponse {
    private Integer orderId; private String orderNumber;
    private BigDecimal totalAmount; private String paymentUrl; // Chứa link VNPAY nếu thanh toán online
}