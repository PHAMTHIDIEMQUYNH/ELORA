package ELORA.ELORA.dto.response;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data public class OrderDetailResponse {
    private String orderNumber; private String status;
    private BigDecimal totalAmount; private LocalDateTime createdAt;
    private List<CartItemDTO> items;
}