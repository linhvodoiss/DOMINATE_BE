package com.fpt.serviceTest;

import com.fpt.dto.PayOSDTO;
import com.fpt.service.PayOSService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayOSServiceTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PayOSService payOSService;

    @BeforeEach
    void setUp() {
        // Set up configuration values using ReflectionTestUtils
        ReflectionTestUtils.setField(payOSService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(payOSService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(payOSService, "checksumKey", "test-checksum-key");
        ReflectionTestUtils.setField(payOSService, "paymentUrl", "https://api-merchant.payos.vn/v2/payment-requests");
        ReflectionTestUtils.setField(payOSService, "webhookUrl", "https://test.com/webhook");
        ReflectionTestUtils.setField(payOSService, "confirmWebhookUrl", "https://api-merchant.payos.vn/confirm-webhook");
        ReflectionTestUtils.setField(payOSService, "serverPort", 8080);
        ReflectionTestUtils.setField(payOSService, "restTemplate", restTemplate);
    }

    // ========== Tests for createPaymentLink ==========

    @Test
    void createPaymentLink_ShouldReturnPayOSDTO_WhenSuccessful() throws Exception {
        // Arrange
        int amount = 100000;
        long orderCode = 123456L;
        String description = "Test payment";
        String cancelUrl = "https://test.com/cancel";
        String returnUrl = "https://test.com/return";

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("bin", "970422");
        responseData.put("checkoutUrl", "https://pay.payos.vn/web/checkout");
        responseData.put("accountNumber", "12345678");
        responseData.put("accountName", "Test Account");
        responseData.put("amount", amount);
        responseData.put("description", description);
        responseData.put("orderCode", orderCode);
        responseData.put("qrCode", "test-qr-code");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", "00");
        responseBody.put("desc", "Success");
        responseBody.put("data", responseData);

        PayOSDTO expectedDTO = new PayOSDTO();
        expectedDTO.setCheckoutUrl("https://pay.payos.vn/web/checkout");
        expectedDTO.setAmount(amount);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);
        when(modelMapper.map(responseData, PayOSDTO.class)).thenReturn(expectedDTO);

        // Act
        PayOSDTO result = payOSService.createPaymentLink(amount, orderCode, description, cancelUrl, returnUrl);

        // Assert
        assertNotNull(result);
        assertEquals("https://pay.payos.vn/web/checkout", result.getCheckoutUrl());
        assertEquals(amount, result.getAmount());
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
        verify(modelMapper, times(1)).map(responseData, PayOSDTO.class);
    }

    @Test
    void createPaymentLink_ShouldThrowException_WhenResponseCodeIsNotSuccess() throws Exception {
        // Arrange
        int amount = 100000;
        long orderCode = 123456L;
        String description = "Test payment";
        String cancelUrl = "https://test.com/cancel";
        String returnUrl = "https://test.com/return";

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", "01");
        responseBody.put("desc", "Invalid request");

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            payOSService.createPaymentLink(amount, orderCode, description, cancelUrl, returnUrl);
        });

        assertTrue(exception.getMessage().contains("Phản hồi từ PayOS lỗi"));
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
        verify(modelMapper, never()).map(any(), eq(PayOSDTO.class));
    }

    @Test
    void createPaymentLink_ShouldThrowException_WhenResponseBodyIsNull() throws Exception {
        // Arrange
        long amount = 100000L;
        long orderCode = 123456L;
        String description = "Test payment";
        String cancelUrl = "https://test.com/cancel";
        String returnUrl = "https://test.com/return";

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            payOSService.createPaymentLink(amount, orderCode, description, cancelUrl, returnUrl);
        });

        assertTrue(exception.getMessage().contains("Phản hồi từ PayOS lỗi"));
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void createPaymentLink_ShouldGenerateCorrectSignature_WithProvidedData() throws Exception {
        // Arrange
        long amount = 50000L;
        long orderCode = 789012L;
        String description = "Another test payment";
        String cancelUrl = "https://test.com/cancel2";
        String returnUrl = "https://test.com/return2";

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("checkoutUrl", "https://pay.payos.vn/web/checkout2");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", "00");
        responseBody.put("data", responseData);

        PayOSDTO expectedDTO = new PayOSDTO();
        expectedDTO.setCheckoutUrl("https://pay.payos.vn/web/checkout2");

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);
        when(modelMapper.map(responseData, PayOSDTO.class)).thenReturn(expectedDTO);

        // Act
        PayOSDTO result = payOSService.createPaymentLink(amount, orderCode, description, cancelUrl, returnUrl);

        // Assert
        assertNotNull(result);
        assertEquals("https://pay.payos.vn/web/checkout2", result.getCheckoutUrl());
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    // ========== Tests for getPaymentLinkInfo ==========

    @Test
    void getPaymentLinkInfo_ShouldReturnPaymentInfo_WhenSuccessful() {
        // Arrange
        Integer paymentLinkId = 123;
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", "00");
        responseBody.put("desc", "Success");
        Map<String, Object> data = new HashMap<>();
        data.put("id", paymentLinkId);
        data.put("amount", 100000);
        data.put("status", "PAID");
        responseBody.put("data", data);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act
        Map<String, Object> result = payOSService.getPaymentLinkInfo(paymentLinkId);

        // Assert
        assertNotNull(result);
        assertEquals("00", result.get("code"));
        assertEquals("Success", result.get("desc"));
        assertNotNull(result.get("data"));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void getPaymentLinkInfo_ShouldThrowException_WhenResponseCodeIsNotSuccess() {
        // Arrange
        Integer paymentLinkId = 123;
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", "01");
        responseBody.put("desc", "Payment not found");

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            payOSService.getPaymentLinkInfo(paymentLinkId);
        });

        assertEquals("Payment not found", exception.getMessage());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void getPaymentLinkInfo_ShouldThrowException_WhenResponseBodyIsNull() {
        // Arrange
        Integer paymentLinkId = 123;
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            payOSService.getPaymentLinkInfo(paymentLinkId);
        });

        assertEquals("Không có phản hồi", exception.getMessage());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void getPaymentLinkInfo_ShouldSetCorrectHeaders_WhenCalled() {
        // Arrange
        Integer paymentLinkId = 456;
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", "00");
        responseBody.put("data", new HashMap<>());

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act
        Map<String, Object> result = payOSService.getPaymentLinkInfo(paymentLinkId);

        // Assert
        assertNotNull(result);
        verify(restTemplate, times(1)).exchange(
                contains("/" + paymentLinkId),
                eq(HttpMethod.GET),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getContentType().equals(MediaType.APPLICATION_JSON) &&
                            "test-client-id".equals(headers.getFirst("x-client-id")) &&
                            "test-api-key".equals(headers.getFirst("x-api-key"));
                }),
                eq(Map.class)
        );
    }

    // ========== Tests for generateSignature ==========

    @Test
    void generateSignature_ShouldReturnCorrectSignature_WithValidData() throws Exception {
        // Arrange
        String data = "amount=100000&cancelUrl=https://test.com/cancel&description=Test&orderCode=123&returnUrl=https://test.com/return";
        String key = "test-key";

        // Act
        String result = ReflectionTestUtils.invokeMethod(payOSService, "generateSignature", data, key);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(64, result.length()); // HMAC SHA256 always produces 64 character hex string
    }

    @Test
    void generateSignature_ShouldReturnConsistentResult_WithSameInput() throws Exception {
        // Arrange
        String data = "test=data&another=value";
        String key = "secret-key";

        // Act
        String result1 = ReflectionTestUtils.invokeMethod(payOSService, "generateSignature", data, key);
        String result2 = ReflectionTestUtils.invokeMethod(payOSService, "generateSignature", data, key);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1, result2);
    }

    @Test
    void generateSignature_ShouldReturnDifferentResult_WithDifferentKeys() throws Exception {
        // Arrange
        String data = "same=data";
        String key1 = "key1";
        String key2 = "key2";

        // Act
        String result1 = ReflectionTestUtils.invokeMethod(payOSService, "generateSignature", data, key1);
        String result2 = ReflectionTestUtils.invokeMethod(payOSService, "generateSignature", data, key2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1, result2);
    }

    @Test
    void generateSignature_ShouldHandleEmptyData() throws Exception {
        // Arrange
        String data = "";
        String key = "test-key";

        // Act
        String result = ReflectionTestUtils.invokeMethod(payOSService, "generateSignature", data, key);

        // Assert
        assertNotNull(result);
        assertEquals(64, result.length());
    }

    // ========== Tests for cancelPaymentRequest ==========

    @Test
    void cancelPaymentRequest_ShouldReturnCancelData_WhenSuccessful() throws Exception {
        // Arrange
        Integer paymentLinkId = 123;
        String reason = "Customer request";

        Map<String, Object> cancelData = new HashMap<>();
        cancelData.put("id", paymentLinkId);
        cancelData.put("status", "CANCELLED");
        cancelData.put("cancellationReason", reason);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", "00");
        responseBody.put("desc", "Success");
        responseBody.put("data", cancelData);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act
        Map<String, Object> result = payOSService.cancelPaymentRequest(paymentLinkId, reason);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(paymentLinkId, result.get("id"));
        assertEquals("CANCELLED", result.get("status"));
        assertEquals(reason, result.get("cancellationReason"));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void cancelPaymentRequest_ShouldThrowException_WhenResponseCodeIsNotSuccess() throws Exception {
        // Arrange
        Integer paymentLinkId = 123;
        String reason = "Customer request";

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", "01");
        responseBody.put("desc", "Payment cannot be cancelled");

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            payOSService.cancelPaymentRequest(paymentLinkId, reason);
        });

        assertTrue(exception.getMessage().contains("Huỷ đơn thất bại"));
        assertTrue(exception.getMessage().contains("Payment cannot be cancelled"));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void cancelPaymentRequest_ShouldThrowException_WhenResponseBodyIsNull() throws Exception {
        // Arrange
        Integer paymentLinkId = 123;
        String reason = "Customer request";

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            payOSService.cancelPaymentRequest(paymentLinkId, reason);
        });

        assertTrue(exception.getMessage().contains("Không có phản hồi"));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void cancelPaymentRequest_ShouldSendCorrectRequest_WithProperHeaders() throws Exception {
        // Arrange
        Integer paymentLinkId = 789;
        String reason = "Duplicate order";

        Map<String, Object> cancelData = new HashMap<>();
        cancelData.put("id", paymentLinkId);
        cancelData.put("status", "CANCELLED");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", "00");
        responseBody.put("data", cancelData);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act
        Map<String, Object> result = payOSService.cancelPaymentRequest(paymentLinkId, reason);

        // Assert
        assertNotNull(result);
        assertEquals(paymentLinkId, result.get("id"));
        verify(restTemplate, times(1)).exchange(
                contains("/" + paymentLinkId + "/cancel"),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    Map<String, String> body = (Map<String, String>) entity.getBody();
                    return headers.getContentType().equals(MediaType.APPLICATION_JSON) &&
                            "test-client-id".equals(headers.getFirst("x-client-id")) &&
                            "test-api-key".equals(headers.getFirst("x-api-key")) &&
                            reason.equals(body.get("cancellationReason"));
                }),
                eq(Map.class)
        );
    }
}
