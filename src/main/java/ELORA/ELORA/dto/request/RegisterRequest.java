package ELORA.ELORA.dto.request;
import lombok.Data;
@Data public class RegisterRequest {
    private String username; private String password; private String email;
    private String fullName; private String phone;
}