package ELORA.ELORA.dto.response;
import lombok.Data;
import java.math.BigDecimal;
@Data public class VariantDTO {
    private Integer id; private String name; private String value; private BigDecimal price;
}