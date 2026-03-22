package ELORA.ELORA.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReviewResponse {
    private Integer id;
    private String fullName;
    private Integer rating;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}