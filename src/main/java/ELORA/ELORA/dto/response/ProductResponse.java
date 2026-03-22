package ELORA.ELORA.dto.response;
import lombok.Data;
import java.math.BigDecimal;
@Data public class ProductResponse {
    private Integer id; private String name; private BigDecimal price;
    private String mainImageUrl; private String categoryName; private String brandName;
    private Boolean isFeatured;
}