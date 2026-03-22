package ELORA.ELORA.dto.request;
import lombok.Data;
@Data public class ReviewRequest {
    private Integer userId; private Integer productId;
    private Integer rating; private String title; private String content;
}