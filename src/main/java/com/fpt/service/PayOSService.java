package com.fpt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.dto.PayOSDTO;
import com.fpt.exception.CustomValidationException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

@Service
public class PayOSService {

    private static final Logger LOGGER = Logger.getLogger(PayOSService.class.getName());

    @Autowired
    private ModelMapper modelMapper;

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.payment-url}")
    private String paymentUrl;

    @Value("${payos.webhook-url}")
    private String webhookUrl;

    @Value("${payos.confirm-webhook-url}")
    private String confirmWebhookUrl;

    @Value("${server.port:8080}") // Lấy cổng từ application.properties, mặc định là 8080
    private int serverPort;

    private final RestTemplate restTemplate = new RestTemplate();

    public PayOSDTO createPaymentLink(long amount, long orderCode, String description,
                                      String cancelUrl, String returnUrl) throws Exception {

        // 1. Map root payload
        Map<String, Object> payloadMap = new LinkedHashMap<>();
        payloadMap.put("amount", amount);
        payloadMap.put("cancelUrl", cancelUrl);
        payloadMap.put("description", description);
        payloadMap.put("orderCode", orderCode);
        payloadMap.put("returnUrl", returnUrl);

        // 2. Order data following alphabet to signature
        StringBuilder dataToSign = new StringBuilder();
        payloadMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    if (dataToSign.length() > 0) {
                        dataToSign.append("&");
                    }
                    dataToSign.append(entry.getKey()).append("=").append(entry.getValue());
                });

        System.out.println("Chuỗi data ký: " + dataToSign);

        // 3. generate signature HMAC_SHA256
        String signature = generateSignature(dataToSign.toString(), checksumKey);
        System.out.println("Chữ ký tạo ra: " + signature);

        // 4. send request wIth signature
        payloadMap.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payloadMap, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(paymentUrl, request, Map.class);

        Map body = response.getBody();
        if (body == null || !"00".equals(body.get("code"))) {
            ObjectMapper mapper = new ObjectMapper();
            throw new IllegalStateException("Phản hồi từ PayOS lỗi: " + mapper.writeValueAsString(body));
        }

        Map<String, Object> data = (Map<String, Object>) body.get("data");
        PayOSDTO info = modelMapper.map(data, PayOSDTO.class);
        return info;
    }

    private String generateSignature(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secretKey);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Tự động xác nhận webhook khi khởi động
     */
    @PostConstruct
    public void autoConfirmWebhook() {
        try {
            LOGGER.info("🔄 Đang xác nhận webhook với PayOS...");

            // Gửi yêu cầu xác nhận webhook tới PayOS
            confirmWebhook(webhookUrl);

            LOGGER.info("✅ Webhook đã được xác nhận thành công với PayOS: " + webhookUrl);
        } catch (Exception e) {
            LOGGER.severe("❌ Lỗi khi đăng ký webhook với PayOS: " + e.getMessage() + ", URL: " + webhookUrl);
        }
    }

    public void confirmWebhook(String webhookUrl) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);

        Map<String, String> body = Map.of("webhookUrl", webhookUrl);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(confirmWebhookUrl, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !"00".equals(responseBody.get("code"))) {
            LOGGER.severe("❌ Phản hồi xác nhận webhook lỗi từ PayOS: " +
                    (responseBody != null ? new ObjectMapper().writeValueAsString(responseBody) : "null") +
                    ", HTTP Status: " + response.getStatusCode());

            throw new RuntimeException("Đăng ký webhook thất bại: " +
                    (responseBody != null ? responseBody.get("desc") : "Không có phản hồi"));
        }

        LOGGER.info("✅ Phản hồi xác nhận webhook thành công: " + responseBody.get("desc"));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> cancelPaymentRequest(Integer paymentLinkId, String reason) throws Exception {
        String url = paymentUrl + "/" + paymentLinkId + "/cancel";

        Map<String, String> body = Map.of("cancellationReason", reason);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        LOGGER.info("✅ Lấy response hủy: " + responseBody);

        if (responseBody == null || !"00".equals(responseBody.get("code"))) {
            throw new RuntimeException("Huỷ đơn thất bại: " +
                    (responseBody != null ? responseBody.get("desc") : "Không có phản hồi"));
        }

        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

        LOGGER.info("✅ Đã huỷ đơn hàng PayOS thành công: " + paymentLinkId);
        return data; // Trả về data
    }


    public Map<String, Object> getPaymentLinkInfo(Integer paymentLinkId) {
        String url = paymentUrl + "/" + paymentLinkId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        Map<String, Object> body = response.getBody();

        if (body == null || !"00".equals(body.get("code"))) {
            throw new RuntimeException("" + (body != null ? body.get("desc") : "Không có phản hồi"));
        }

        return body;
    }


}