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
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                "Create link payment successfully.",
                payOSDTO
        );

        return ResponseEntity.ok(response);
    }

    /**
     * PayOS send webhook to update order status
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody Map<String, Object> webhookPayload, HttpServletRequest request) {
        try {
            LOGGER.info("📥 Nhận webhook từ PayOS: " + webhookPayload);

            Object dataObj = webhookPayload.get("data");
            String receivedSignature = (String) webhookPayload.get("signature");

            if (dataObj == null || receivedSignature == null) {
                LOGGER.warning("❌ Thiếu trường data hoặc signature");
                return ResponseEntity.ok(Map.of("success", false, "message", "Missing data or signature"));
            }

            Map<String, Object> data = (Map<String, Object>) dataObj;

            LOGGER.info("🔍 Dữ liệu webhook: orderCode=" + data.get("orderCode") + ", amount=" + data.get("amount") + ", status=" + data.get("status"));

            // Bỏ qua webhook test
            if ("123".equals(data.get("orderCode").toString()) &&
                    data.containsKey("transactionDateTime") &&
                    ((String) data.get("transactionDateTime")).startsWith("2023")) {
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

            // Xử lý dữ liệu đơn hàng
            long orderCode = Long.parseLong(data.get("orderCode").toString());
            String statusOrder = (String) data.get("status");
            String status = (String) data.get("code");

            if (!paymentOrderService.orderIdExists((int) orderCode)) {
                LOGGER.warning("❌ Không tìm thấy đơn hàng với orderCode: " + orderCode);
                return ResponseEntity.ok(Map.of("success", false, "message", "Order not found"));
            }

            // Map trạng thái
            String internalStatus;
            if ("PAID".equals(statusOrder) || "00".equals(status)) {
                internalStatus = "SUCCESS";
            } else if ("CANCELLED".equals(statusOrder)) {
                internalStatus = "FAILED";
            } else {
                internalStatus = "PENDING";
                LOGGER.warning("⚠️ Trạng thái không rõ ràng, mặc định PENDING: status=" + statusOrder + ", code=" + status);
            }

            // Lấy thêm 4 field từ webhook
            String bin = (String) data.get("counterAccountBankId");
            String accountName = (String) data.get("counterAccountName");
            String accountNumber = (String) data.get("counterAccountNumber");
            String dateTransfer=(String) data.get("transactionDateTime");
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = request.getRemoteAddr();
            }
            // Cập nhật vào DB
            LOGGER.info("✅ Cập nhật đơn hàng từ webhook - orderCode=" + orderCode + ", internalStatus=" + internalStatus);
            paymentOrderService.updateOrderFromWebhook((int) orderCode, internalStatus, bin, accountName, accountNumber,dateTransfer,ip);

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
    public ResponseEntity<SuccessResponse<Map<String, Object>>> cancelPaymentRequest(
            @PathVariable("paymentLinkId") Integer paymentLinkId,
            @RequestParam(defaultValue = "Cancel payment") String reason) {
        try {
            Map<String, Object> cancelData = payOSService.cancelPaymentRequest(paymentLinkId, reason);
            paymentOrderService.changeStatusOrderByOrderId(paymentLinkId, "FAILED");
            String cancellationReason = (String) cancelData.get("cancellationReason");
            String canceledAt = (String) cancelData.get("canceledAt");
            paymentOrderService.addReasonCancel(paymentLinkId,cancellationReason,canceledAt);
            return ResponseEntity.ok(
                    new SuccessResponse<>(200, "Cancel order successfully", cancelData)
            );
        } catch (Exception e) {
            LOGGER.severe("❌ Lỗi huỷ đơn hàng: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(new SuccessResponse<>(500, e.getMessage(), null));
        }
    }

    @PostMapping("/cancelPro/{paymentLinkId}")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> cancelProPaymentRequest(
            @PathVariable("paymentLinkId") Integer paymentLinkId) {
        try {
            paymentOrderService.changeStatusOrderByOrderId(paymentLinkId, "FAILED");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String cancellationReason = "Customer cancel from PayOS payment";
            String canceledAt = LocalDateTime.now().format(formatter);
            paymentOrderService.addReasonCancel(paymentLinkId,cancellationReason,canceledAt);
            return ResponseEntity.ok(
                    new SuccessResponse<>(200, "Cancel order successfully", null)
            );
        } catch (Exception e) {
            LOGGER.severe("❌ Lỗi huỷ đơn hàng: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(new SuccessResponse<>(500, e.getMessage(), null));
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
            response.put("message", "Take payment successfully");
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


    @PostMapping("/sync/{paymentLinkId}")
    public ResponseEntity<?> syncPayment(@PathVariable Integer paymentLinkId) {
        try {
            Map<String, Object> info = payOSService.getPaymentLinkInfo(paymentLinkId);
            Map<String, Object> dataPayment = (Map<String, Object>) info.get("data");

            String status = (String) dataPayment.get("status");
            if (!"PAID".equalsIgnoreCase(status)) {
                return ResponseEntity.status(400).body(new SuccessNoResponse(400, "Order haven't payment."));
            }

            Integer orderCode = (Integer) dataPayment.get("orderCode");
            List<Map<String, Object>> transactions = (List<Map<String, Object>>) dataPayment.get("transactions");
            if (transactions == null || transactions.isEmpty()) {
                return ResponseEntity.status(400).body(new SuccessNoResponse(400, "You have no transaction."));
            }

            Map<String, Object> transaction = transactions.get(0);
            String bin = (String) transaction.get("counterAccountBankId");
            String accountName = (String) transaction.get("counterAccountName");
            String accountNumber = (String) transaction.get("counterAccountNumber");

            String dateTransfer = (String) transaction.get("transactionDateTime");

            paymentOrderService.syncBill(orderCode, bin, accountName, accountNumber, dateTransfer);

            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "Sync bill successfully",
                    "orderCode", orderCode
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(new SuccessNoResponse(500, "Sync bill error: " + e.getMessage()));
        }
    }

}
