package ELORA.ELORA.dto.response;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
@Data public class ProductDetailResponse {
    private Integer id; private String name; private String description;
    private BigDecimal price; private Integer stock;
    private List<String> imageUrls;
    private List<VariantDTO> variants;
}