package com.fpt.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fpt.dto.PayOSDTO;
import com.fpt.form.PayOSForm;
import com.fpt.payload.SuccessNoResponse;
import com.fpt.payload.SuccessResponse;
import com.fpt.service.IPaymentOrderService;
import com.fpt.service.PayOSService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

@RestController
@Validated
@RequestMapping("/api/v1/payment")
public class PayOSController {
    private static final Logger LOGGER = Logger.getLogger(PayOSController.class.getName());
    @Value("${payos.checksum-key}")
    private String checksumKey;
    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;
    @Value("${payos.webhook-url}")
    private String webhookUrl;

    private final PayOSService payOSService;
    private final IPaymentOrderService paymentOrderService;
    public PayOSController(PayOSService payOSService, IPaymentOrderService paymentOrderService) {
        this.payOSService = payOSService;
        this.paymentOrderService = paymentOrderService;
    }

    /**
     * Endpoint Link payment FE (Next.js).
     */
    @PostMapping("/create-payment")
    public ResponseEntity<SuccessResponse<PayOSDTO>> createPayment(@Valid @RequestBody PayOSForm form) throws Exception {
        long orderCode = form.getOrderCode();
        long amount = form.getAmount();
        String description = form.getDescription();

        PayOSDTO payOSDTO = payOSService.createPaymentLink(
                amount,
                orderCode,
                description,
                cancelUrl,
                returnUrl
        );

        SuccessResponse<PayOSDTO> response = new SuccessResponse<>(
                200,
                "Tạo link thanh toán thành công",
                payOSDTO
        );

        return ResponseEntity.ok(response);
    }

    /**
     * PayOS send webhook to update order status
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody Map<String, Object> webhookPayload) {
        try {
            LOGGER.info("📥 Nhận webhook từ PayOS: " + webhookPayload);

            Object dataObj = webhookPayload.get("data");
            String receivedSignature = (String) webhookPayload.get("signature");

            if (dataObj == null || receivedSignature == null) {
                LOGGER.warning("❌ Thiếu trường data hoặc signature");
                return ResponseEntity.ok(Map.of("success", false, "message", "Missing data or signature"));
            }

            Map<String, Object> data = (Map<String, Object>) dataObj;

            // Log chi tiết dữ liệu nhận được
            LOGGER.info("🔍 Dữ liệu webhook: orderCode=" + data.get("orderCode") + ", amount=" + data.get("amount") + ", status=" + data.get("status"));

            // Bỏ qua webhook test (ví dụ: orderCode=123 với transactionDateTime cũ)
            if (data.containsKey("orderCode") && "123".equals(data.get("orderCode").toString()) &&
                    data.containsKey("transactionDateTime") && ((String) data.get("transactionDateTime")).startsWith("2023")) {
                LOGGER.info("⏭️ Bỏ qua webhook test với orderCode=123 và transactionDateTime cũ");
                return ResponseEntity.ok(Map.of("success", true, "message", "Test webhook ignored"));
            }

            // Kiểm tra chữ ký
            List<String> sortedKeys = new ArrayList<>(data.keySet());
            Collections.sort(sortedKeys);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sortedKeys.size(); i++) {
                String key = sortedKeys.get(i);
                Object value = data.get(key);
                sb.append(key).append("=").append(value != null ? value.toString() : "");
                if (i < sortedKeys.size() - 1) sb.append("&");
            }
            String stringToSign = sb.toString();
            LOGGER.info("🔏 Chuỗi data ký: " + stringToSign);
            String computedSignature = calculateHmacSha256(stringToSign, checksumKey);
            LOGGER.info("🔐 Chữ ký tạo ra: " + computedSignature);

            if (!computedSignature.equalsIgnoreCase(receivedSignature)) {
                LOGGER.warning("❌ Chữ ký không hợp lệ. Nhận: " + receivedSignature + " | Tính: " + computedSignature);
                return ResponseEntity.ok(Map.of("success", false, "message", "Invalid signature"));
            }

            // Lấy và xử lý orderCode từ dữ liệu webhook
            long orderCode = Long.parseLong(data.get("orderCode").toString());
            String statusOrder = (String) data.get("status");
            long amount = Long.parseLong(data.get("amount").toString());
            String status = (String) data.get("code");

            // Kiểm tra sự tồn tại của đơn hàng
            if (!paymentOrderService.orderIdExists((int) orderCode)) {
                LOGGER.warning("❌ Không tìm thấy đơn hàng với orderCode: " + orderCode + ", Dữ liệu: " + data);
                return ResponseEntity.ok(Map.of("success", false, "message", "Order not found"));
            }
            LOGGER.warning("⚠️ Trạng thái status=" + statusOrder + ", code=" + status);
            LOGGER.info("📦 Dữ liệu data (raw): " + data);

            // Map trạng thái về hệ thống nội bộ
            String internalStatus;
            if ("PAID".equals(statusOrder) || "00".equals(status)) {
                internalStatus = "SUCCESS";
            } else if ("CANCELLED".equals(statusOrder)) {
                internalStatus = "FAILED";
            } else {
                internalStatus = "PENDING";
                LOGGER.warning("⚠️ Trạng thái không rõ ràng, mặc định PENDING: status=" + statusOrder + ", code=" + status);
            }

            LOGGER.info("✅ Webhook hợp lệ - OrderCode: " + orderCode + ", Amount: " + amount + ", Status nội bộ: " + internalStatus);
            paymentOrderService.changeStatusOrderByOrderId((int) orderCode, internalStatus);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            LOGGER.severe("❌ Lỗi xử lý webhook: " + e.getMessage() + ", Dữ liệu: " + webhookPayload);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Internal server error"));
        }
    }
    private String calculateHmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : rawHmac) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @PostMapping("/cancel/{paymentLinkId}")
    public ResponseEntity<SuccessNoResponse> cancelPaymentRequest(
            @PathVariable("paymentLinkId") Integer paymentLinkId,
            @RequestParam(defaultValue = "Cancel payment") String reason) {
        try {
            payOSService.cancelPaymentRequest(paymentLinkId, reason);
            paymentOrderService.changeStatusOrderByOrderId(paymentLinkId, "FAILED");
            return ResponseEntity.ok(new SuccessNoResponse(200, "Huỷ đơn hàng thành công"));
        } catch (Exception e) {
            LOGGER.severe("❌ Lỗi huỷ đơn hàng: " + e.getMessage());
            return ResponseEntity.status(500).body(new SuccessNoResponse(500, e.getMessage()));
        }
    }
    @GetMapping("/{paymentLinkId}")
    public ResponseEntity<?> getPaymentInfo(@PathVariable Integer paymentLinkId) {
        try {
            Map<String, Object> info = payOSService.getPaymentLinkInfo(paymentLinkId);
            LOGGER.severe("❌ Thông tin info: " + info);
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> dataPayment = (Map<String, Object>) info.get("data");
            response.put("code", 200);
            response.put("message", "Lấy đơn hàng thành công");
            response.put("data", dataPayment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new SuccessNoResponse(404, "Lấy đơn hàng thất bại: " + e.getMessage()));
        }
    }




    @GetMapping("/confirm-webhook")
    public ResponseEntity<String> confirmWebhookManually() {
        try {
            payOSService.confirmWebhook(webhookUrl);
            return ResponseEntity.ok("✅ Đăng ký webhook thành công với PayOS");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Lỗi xác nhận webhook: " + e.getMessage());
        }
    }

}
