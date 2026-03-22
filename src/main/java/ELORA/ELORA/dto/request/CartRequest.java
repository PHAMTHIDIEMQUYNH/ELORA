package ELORA.ELORA.dto.request;
import lombok.Data;
@Data public class CartRequest {
    private Integer userId; private Integer productId;
    private Integer variantId; private Integer quantity;
}