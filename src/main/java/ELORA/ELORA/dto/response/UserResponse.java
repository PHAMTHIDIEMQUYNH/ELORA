package ELORA.ELORA.dto.response;
import lombok.Data;
@Data public class UserResponse {
    private Integer id; private String username; private String email;
    private String fullName; private String role; private String avatarUrl;
}