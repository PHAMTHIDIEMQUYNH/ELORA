package ELORA.ELORA.dto.request;
import lombok.Data;
@Data public class AddressRequest {
    private String recipientName; private String recipientPhone;
    private String province; private String district; private String ward;
    private String detailedAddress; private Boolean isDefault;
}