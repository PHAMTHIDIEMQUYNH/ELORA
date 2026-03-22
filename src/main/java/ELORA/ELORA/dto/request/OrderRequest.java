package ELORA.ELORA.dto.request;
import lombok.Data;
@Data public class OrderRequest {
    private Integer userId; private Integer addressId;
    private String couponCode; private String paymentMethod; // COD hoặc VNPAY
    private String notes;
}