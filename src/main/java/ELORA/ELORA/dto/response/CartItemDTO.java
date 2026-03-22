package ELORA.ELORA.dto.response;
import lombok.Data;
import java.math.BigDecimal;
@Data public class CartItemDTO {
    private Integer cartId; private String productName; private String variantInfo;
    private Integer quantity; private BigDecimal price; private BigDecimal total;
}