package ELORA.ELORA.service;

import ELORA.ELORA.entity.Order;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;

@Service
public class MomoService {

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.api-endpoint}")
    private String apiEndpoint;

    private String redirectUrl = "http://localhost:8081/api/payment/momo-return";

    @Value("${momo.ipn-url}")
    private String ipnUrl;

    /**
     * Tạo URL thanh toán MoMo
     * @param order Đối tượng đơn hàng
     * @return payUrl để chuyển hướng trình duyệt
     */
    public String createPaymentUrl(Order order) throws Exception {
        // 1. Lấy dữ liệu từ đối tượng Order
        String orderId = order.getOrderNumber();
        long amount = order.getTotalAmount().longValue();

        // 2. Tạo các tham số
        String requestId = partnerCode + System.currentTimeMillis();
        String orderInfo = "Thanh toan don hang ELORA #" + orderId;
        String extraData = "";
        String requestType = "payWithMethod";
        // 3. Tạo chuỗi raw signature theo đúng thứ tự MoMo yêu cầu
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        // 4. Ký HMAC-SHA256
        String signature = hmacSha256(secretKey, rawSignature);

        // 5. Log để debug
        System.out.println("MoMo rawSignature: " + rawSignature);
        System.out.println("MoMo signature: " + signature);

        // 6. Tạo body JSON gửi sang MoMo
        JSONObject requestBody = new JSONObject();
        requestBody.put("partnerCode", partnerCode);
        requestBody.put("partnerName", "ELORA COSMETIC");
        requestBody.put("storeId", "ELORA_STORE");
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amount);
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", redirectUrl);
        requestBody.put("ipnUrl", ipnUrl);
        requestBody.put("lang", "vi");
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", requestType);
        requestBody.put("signature", signature);

        // 7. Gửi request sang MoMo
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(apiEndpoint, entity, String.class);

        // 8. Parse response và lấy payUrl
        JSONObject responseBody = new JSONObject(response.getBody());

        System.out.println("MoMo response: " + responseBody.toString());

        if (responseBody.has("payUrl")) {
            return responseBody.getString("payUrl");
        } else {
            throw new Exception("MoMo response không có payUrl: " + responseBody.toString());
        }
    }

    /**
     * Hàm mã hóa HMAC-SHA256
     * @param key Secret key
     * @param data Dữ liệu cần mã hóa
     * @return Chuỗi hex sau khi mã hóa
     */
    private String hmacSha256(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] bytes = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));


        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();

        return result;
    }
}