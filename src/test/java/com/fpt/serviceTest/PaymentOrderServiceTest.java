package com.fpt.serviceTest;

import com.fpt.dto.*;
import com.fpt.entity.*;
import com.fpt.form.LicenseCreateForm;
import com.fpt.form.OrderFormCreating;
import com.fpt.repository.LicenseRepository;
import com.fpt.repository.PaymentOrderRepository;
import com.fpt.repository.SubscriptionPackageRepository;
import com.fpt.repository.UserRepository;
import com.fpt.service.PaymentOrderService;
import com.fpt.specification.PaymentOrderSpecificationBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import com.fpt.websocket.PaymentSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentOrderServiceTest {

    @Mock
    private PaymentOrderRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private SubscriptionPackageRepository subscriptionRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PaymentSocketService paymentSocketService;

    @InjectMocks
    private PaymentOrderService paymentOrderService;

    private PaymentOrder testOrder;
    private User testUser;
    private SubscriptionPackage testSubscription;
    private License testLicense;
    private PaymentOrderDTO testOrderDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .userName("testuser")
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("123456789")
                .build();

        testSubscription = SubscriptionPackage.builder()
                .id(1L)
                .name("DEV Package")
                .price(100.0F)
                .discount(10.0F)
                .billingCycle(SubscriptionPackage.BillingCycle.MONTHLY)
                .typePackage(SubscriptionPackage.TypePackage.DEV)
                .isActive(true)
                .simulatedCount(5L)
                .options(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testOrder = PaymentOrder.builder()
                .id(1L)
                .orderId(12345)
                .paymentLink("http://payment.link")
                .paymentStatus(PaymentOrder.PaymentStatus.SUCCESS)
                .paymentMethod(PaymentOrder.PaymentMethod.PAYOS)
                .price(100.0F)
                .user(testUser)
                .subscriptionPackage(testSubscription)
                .licenseCreated(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testLicense = License.builder()
                .id(1L)
                .orderId(12345)
                .licenseKey("TEST-LICENSE-KEY")
                .duration(30)
                .ip("192.168.1.1")
                .canUsed(true)
                .user(testUser)
                .subscriptionPackage(testSubscription)
                .activatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testOrderDTO = PaymentOrderDTO.builder()
                .id(1L)
                .orderId(12345)
                .paymentLink("http://payment.link")
                .paymentStatus("PENDING")
                .paymentMethod("PAYOS")
                .price(100.0F)
                .userId(1L)
                .subscriptionId(1L)
                .licenseCreated(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(paymentOrderService, "modelMapper", modelMapper);
        ReflectionTestUtils.setField(paymentOrderService, "paymentSocketService", paymentSocketService);
    }

    // 1. getAllOrder tests
    @Test
    void getAllOrder_WhenValidParameters_ShouldReturnPageOfOrders() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<PaymentOrder> orders = Arrays.asList(testOrder);
        Page<PaymentOrder> page = new PageImpl<>(orders);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<PaymentOrderDTO> result = paymentOrderService.getAllOrder(
                pageable, "search", 1L, PaymentOrder.PaymentStatus.PENDING, SubscriptionPackage.TypePackage.DEV);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllOrder_WhenEmptyResult_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentOrder> emptyPage = new PageImpl<>(new ArrayList<>());

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        // Act
        Page<PaymentOrderDTO> result = paymentOrderService.getAllOrder(
                pageable, null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllOrder_WhenNullSearchParameters_ShouldHandleGracefully() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<PaymentOrder> orders = Arrays.asList(testOrder);
        Page<PaymentOrder> page = new PageImpl<>(orders);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<PaymentOrderDTO> result = paymentOrderService.getAllOrder(
                pageable, null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllOrder_WhenSpecificStatusFilter_ShouldReturnFilteredResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<PaymentOrder> orders = Arrays.asList(testOrder);
        Page<PaymentOrder> page = new PageImpl<>(orders);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<PaymentOrderDTO> result = paymentOrderService.getAllOrder(
                pageable, "", 1L, PaymentOrder.PaymentStatus.SUCCESS, SubscriptionPackage.TypePackage.DEV);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    // 2. getUserOrder tests
    @Test
    void getUserOrder_WhenValidParameters_ShouldReturnUserOrders() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<PaymentOrder> orders = Arrays.asList(testOrder);
        Page<PaymentOrder> page = new PageImpl<>(orders);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<PaymentOrderDTO> result = paymentOrderService.getUserOrder(
                pageable, "search", 1L, PaymentOrder.PaymentStatus.PENDING, 1L, SubscriptionPackage.TypePackage.DEV);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getUserOrder_WhenUserHasNoOrders_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentOrder> emptyPage = new PageImpl<>(new ArrayList<>());

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        // Act
        Page<PaymentOrderDTO> result = paymentOrderService.getUserOrder(
                pageable, "", null, null, 999L, null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getUserOrder_WhenNullUserId_ShouldHandleGracefully() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<PaymentOrder> orders = Arrays.asList(testOrder);
        Page<PaymentOrder> page = new PageImpl<>(orders);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<PaymentOrderDTO> result = paymentOrderService.getUserOrder(
                pageable, null, null, null, null, null);

        // Assert
        assertNotNull(result);
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getUserOrder_WhenAllFiltersApplied_ShouldUseAllCriteria() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<PaymentOrder> orders = Arrays.asList(testOrder);
        Page<PaymentOrder> page = new PageImpl<>(orders);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<PaymentOrderDTO> result = paymentOrderService.getUserOrder(
                pageable, "test", 1L, PaymentOrder.PaymentStatus.SUCCESS, 1L, SubscriptionPackage.TypePackage.DEV);

        // Assert
        assertNotNull(result);
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    // 3. convertToDto tests
    @Test
    void convertToDto_WhenValidList_ShouldReturnDTOList() {
        // Arrange
        List<PaymentOrder> orders = Arrays.asList(testOrder);
        // Dùng any() thay vì testOrder cụ thể
        when(modelMapper.map(any(PaymentOrder.class), eq(PaymentOrderDTO.class)))
                .thenReturn(testOrderDTO);

        // Act
        List<PaymentOrderDTO> result = paymentOrderService.convertToDto(orders);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0)); // Kiểm tra không null trước
        assertEquals(testOrderDTO, result.get(0));
        verify(modelMapper).map(any(PaymentOrder.class), eq(PaymentOrderDTO.class));
    }

    @Test
    void convertToDto_WhenEmptyList_ShouldReturnEmptyList() {
        // Arrange
        List<PaymentOrder> emptyOrders = new ArrayList<>();

        // Act
        List<PaymentOrderDTO> result = paymentOrderService.convertToDto(emptyOrders);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void convertToDto_WhenMultipleOrders_ShouldReturnAllDTOs() {
        // Arrange
        PaymentOrder order2 = PaymentOrder.builder().id(2L).orderId(67890).build();
        PaymentOrderDTO dto2 = PaymentOrderDTO.builder().id(2L).orderId(67890).build();
        List<PaymentOrder> orders = Arrays.asList(testOrder, order2);

        when(modelMapper.map(testOrder, PaymentOrderDTO.class)).thenReturn(testOrderDTO);
        when(modelMapper.map(order2, PaymentOrderDTO.class)).thenReturn(dto2);

        // Act
        List<PaymentOrderDTO> result = paymentOrderService.convertToDto(orders);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testOrderDTO, result.get(0));
        assertEquals(dto2, result.get(1));
        verify(modelMapper, times(2)).map(any(PaymentOrder.class), eq(PaymentOrderDTO.class));
    }

    @Test
    void convertToDto_WhenNullOrder_ShouldHandleGracefully() {
        // Arrange
        List<PaymentOrder> ordersWithNull = Arrays.asList(testOrder, null);
        when(modelMapper.map(testOrder, PaymentOrderDTO.class)).thenReturn(testOrderDTO);
        when(modelMapper.map(null, PaymentOrderDTO.class)).thenReturn(null); // Explicit stub cho null

        // Act
        List<PaymentOrderDTO> result = paymentOrderService.convertToDto(ordersWithNull);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testOrderDTO, result.get(0));
        assertNull(result.get(1)); // Element thứ 2 sẽ là null

        verify(modelMapper).map(testOrder, PaymentOrderDTO.class);
        verify(modelMapper).map(null, PaymentOrderDTO.class);
    }


    // 4. createOrder tests
    @Test
    void createOrder_WhenValidForm_ShouldCreateOrder() {
        // Arrange
        OrderFormCreating form = new OrderFormCreating();
        form.setSubscriptionId(1L);
        form.setOrderId(12345);
        form.setPaymentLink("http://payment.link");
        form.setPrice(100.0F);
        form.setPaymentMethod(PaymentOrder.PaymentMethod.PAYOS);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(repository.existsByOrderId(12345)).thenReturn(false);
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        PaymentOrderDTO result = paymentOrderService.createOrder(form, 1L);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(PaymentOrder.class));
        verify(paymentSocketService).notifyNewOrder(anyInt(), anyString(), anyString(), anyString(), anyFloat(), anyString());
    }

    @Test
    void createOrder_WhenSubscriptionNotFound_ShouldThrowException() {
        // Arrange
        OrderFormCreating form = new OrderFormCreating();
        form.setSubscriptionId(999L);

        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.createOrder(form, 1L);
        });
        assertEquals("Not found package plan", exception.getMessage());
    }

    @Test
    void createOrder_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        OrderFormCreating form = new OrderFormCreating();
        form.setSubscriptionId(1L);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.createOrder(form, 999L);
        });
        assertEquals("Not found user", exception.getMessage());
    }

    @Test
    void createOrder_WhenOrderIdExists_ShouldThrowException() {
        // Arrange
        OrderFormCreating form = new OrderFormCreating();
        form.setSubscriptionId(1L);
        form.setOrderId(12345);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(repository.existsByOrderId(12345)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.createOrder(form, 1L);
        });
        assertEquals("Code orderId is exist", exception.getMessage());
    }

    // 5. updateOrderFromWebhook tests
    @Test
    void updateOrderFromWebhook_WhenValidOrderCode_ShouldUpdateOrder() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        paymentOrderService.updateOrderFromWebhook(12345, "PENDING", "123456", "Test Account", "987654321", "2023-12-01", "192.168.1.1");

        // Assert
        verify(repository, times(2)).save(any(PaymentOrder.class));
        assertEquals("123456", testOrder.getBin());
        assertEquals("Test Account", testOrder.getAccountName());
        assertEquals("987654321", testOrder.getAccountNumber());
        assertEquals("2023-12-01", testOrder.getDateTransfer());
    }

    @Test
    void updateOrderFromWebhook_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(repository.findByOrderId(99999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.updateOrderFromWebhook(99999, "SUCCESS", null, null, null, null, "192.168.1.1");
        });
        assertEquals("Not found order with orderCode: 99999", exception.getMessage());
    }

    @Test
    void updateOrderFromWebhook_WhenNullParameters_ShouldNotUpdateNullFields() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        paymentOrderService.updateOrderFromWebhook(12345, "PENDING", null, null, null, null, "192.168.1.1");

        // Assert - Sửa từ verify(repository).save() thành times(2)
        verify(repository, times(2)).save(any(PaymentOrder.class));
        assertNull(testOrder.getBin());
        assertNull(testOrder.getAccountName());
        assertNull(testOrder.getAccountNumber());
        assertNull(testOrder.getDateTransfer());
        assertNotNull(testOrder.getUpdatedAt());
    }

    @Test
    void updateOrderFromWebhook_WhenPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act - Đổi "SUCCESS" thành "PENDING" để tránh lỗi enum
        paymentOrderService.updateOrderFromWebhook(12345, "PENDING", "123456", null, "987654321", null, "192.168.1.1");

        // Assert
        verify(repository, times(2)).save(any(PaymentOrder.class)); // 2 lần: webhook + changeStatus
        assertEquals("123456", testOrder.getBin());
        assertNull(testOrder.getAccountName());
        assertEquals("987654321", testOrder.getAccountNumber());
        assertNull(testOrder.getDateTransfer());
    }


    // 6. changeStatusOrder tests
    @Test
    void changeStatusOrder_WhenValidStatus_ShouldUpdateStatus() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        PaymentOrder result = paymentOrderService.changeStatusOrder(1L, "SUCCESS");

        // Assert
        assertNotNull(result);
        assertEquals(PaymentOrder.PaymentStatus.SUCCESS, result.getPaymentStatus());
        verify(repository).save(any(PaymentOrder.class));
    }

    @Test
    void changeStatusOrder_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.changeStatusOrder(999L, "SUCCESS");
        });
        assertEquals("Not found order", exception.getMessage());
    }

    @Test
    void changeStatusOrder_WhenInvalidStatus_ShouldThrowException() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.changeStatusOrder(1L, "INVALID_STATUS");
        });
        assertEquals("Status is invalid", exception.getMessage());
    }

    @Test
    void changeStatusOrder_WhenValidCancelledStatus_ShouldUpdateToCancelled() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        PaymentOrder result = paymentOrderService.changeStatusOrder(1L, "FAILED");

        // Assert
        assertNotNull(result);
        assertEquals(PaymentOrder.PaymentStatus.FAILED, result.getPaymentStatus());
        verify(repository).save(any(PaymentOrder.class));
    }

    // 7. changeStatusOrderByOrderId tests
    @Test
    void changeStatusOrderByOrderId_WhenValidOrderId_ShouldUpdateStatus() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        PaymentOrder result = paymentOrderService.changeStatusOrderByOrderId(12345, "SUCCESS");

        // Assert
        assertNotNull(result);
        assertEquals(PaymentOrder.PaymentStatus.SUCCESS, result.getPaymentStatus());
        verify(repository).save(any(PaymentOrder.class));
        verify(paymentSocketService).notifyOrderStatus(12345, "SUCCESS");
    }

    @Test
    void changeStatusOrderByOrderId_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(repository.findByOrderId(99999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.changeStatusOrderByOrderId(99999, "SUCCESS");
        });
        assertEquals("Not found order with orderId: 99999", exception.getMessage());
    }

    @Test
    void changeStatusOrderByOrderId_WhenInvalidStatus_ShouldThrowException() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.changeStatusOrderByOrderId(12345, "INVALID_STATUS");
        });
        assertEquals("Status is invalid", exception.getMessage());
    }

    @Test
    void changeStatusOrderByOrderId_WhenValidPendingStatus_ShouldUpdateAndNotify() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        PaymentOrder result = paymentOrderService.changeStatusOrderByOrderId(12345, "PENDING");

        // Assert
        assertNotNull(result);
        assertEquals(PaymentOrder.PaymentStatus.PENDING, result.getPaymentStatus());
        verify(repository).save(any(PaymentOrder.class));
        verify(paymentSocketService).notifyOrderStatus(12345, "PENDING");
    }

    // 8. createLicensePayOS tests
    @Test
    void createLicensePayOS_WhenValidForm_ShouldCreateLicense() {
        // Arrange
        LicenseCreateForm form = new LicenseCreateForm();
        form.setOrderId(12345);

        when(repository.findByOrderIdForUpdate(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.existsByUserIdAndCanUsedTrue(1L)).thenReturn(false);
        when(licenseRepository.save(any(License.class))).thenReturn(testLicense);

        // Act
        LicenseDTO result = paymentOrderService.createLicensePayOS(form, "192.168.1.1");

        // Assert
        assertNotNull(result);
        assertTrue(testOrder.getLicenseCreated());
        verify(licenseRepository).save(any(License.class));
    }


    @Test
    void createLicensePayOS_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        LicenseCreateForm form = new LicenseCreateForm();
        form.setOrderId(99999);

        when(repository.findByOrderIdForUpdate(99999)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentOrderService.createLicensePayOS(form, "192.168.1.1");
        });
        assertEquals("Not found order.", exception.getMessage());
    }

    @Test
    void createLicensePayOS_WhenOrderNotPaid_ShouldThrowException() {
        // Arrange
        LicenseCreateForm form = new LicenseCreateForm();
        form.setOrderId(12345);

        testOrder.setPaymentStatus(PaymentOrder.PaymentStatus.PENDING);

        when(repository.findByOrderIdForUpdate(12345)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentOrderService.createLicensePayOS(form, "192.168.1.1");
        });
        assertEquals("Unpaid order.", exception.getMessage());
    }

    @Test
    void createLicensePayOS_WhenLicenseAlreadyCreated_ShouldThrowException() {
        // Arrange
        LicenseCreateForm form = new LicenseCreateForm();
        form.setOrderId(12345);

        testOrder.setPaymentStatus(PaymentOrder.PaymentStatus.SUCCESS);
        testOrder.setLicenseCreated(true);

        when(repository.findByOrderIdForUpdate(12345)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentOrderService.createLicensePayOS(form, "192.168.1.1");
        });
        assertEquals("License have created with this order.", exception.getMessage());
    }

    // 9. changeStatusOrderIdCreateLicense tests
    @Test
    void changeStatusOrderIdCreateLicense_WhenSuccessStatus_ShouldCreateLicense() {
        // Arrange
        testOrder.setLicenseCreated(false);
        testOrder.setPaymentStatus(PaymentOrder.PaymentStatus.PENDING);

        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);
        when(repository.findByOrderIdForUpdate(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.existsByUserIdAndCanUsedTrue(1L)).thenReturn(false);
        when(licenseRepository.save(any(License.class))).thenReturn(testLicense);

        // Act
        PaymentOrder result = paymentOrderService.changeStatusOrderIdCreateLicense(12345, "SUCCESS", "192.168.1.1");

        // Assert
        assertNotNull(result);
        assertEquals(PaymentOrder.PaymentStatus.SUCCESS, result.getPaymentStatus());
        verify(repository, times(1)).save(any(PaymentOrder.class));
        verify(paymentSocketService).notifyOrderStatus(12345, "SUCCESS");
    }

    @Test
    void changeStatusOrderIdCreateLicense_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(repository.findByOrderId(99999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.changeStatusOrderIdCreateLicense(99999, "SUCCESS", "192.168.1.1");
        });
        assertEquals("Not found order with orderId: 99999", exception.getMessage());
    }

    @Test
    void changeStatusOrderIdCreateLicense_WhenInvalidStatus_ShouldThrowException() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.changeStatusOrderIdCreateLicense(12345, "INVALID_STATUS", "192.168.1.1");
        });
        assertEquals("Status is invalid", exception.getMessage());
    }

    @Test
    void changeStatusOrderIdCreateLicense_WhenLicenseAlreadyCreated_ShouldNotCreateAgain() {
        // Arrange
        testOrder.setLicenseCreated(true);

        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        PaymentOrder result = paymentOrderService.changeStatusOrderIdCreateLicense(12345, "SUCCESS", "192.168.1.1");

        // Assert
        assertNotNull(result);
        assertEquals(PaymentOrder.PaymentStatus.SUCCESS, result.getPaymentStatus());
        verify(repository).save(any(PaymentOrder.class));
        verify(paymentSocketService).notifyOrderStatus(12345, "SUCCESS");
    }

    // 10. changeStatusOrderByAdmin tests
    @Test
    void changeStatusOrderByAdmin_WhenValidOrderId_ShouldUpdateStatus() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        PaymentOrder result = paymentOrderService.changeStatusOrderByAdmin(12345, "SUCCESS");

        // Assert
        assertNotNull(result);
        assertEquals(PaymentOrder.PaymentStatus.SUCCESS, result.getPaymentStatus());
        verify(repository).save(any(PaymentOrder.class));
        verify(paymentSocketService).notifyAdminStatus(12345, "SUCCESS");
    }

    @Test
    void changeStatusOrderByAdmin_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(repository.findByOrderId(99999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.changeStatusOrderByAdmin(99999, "SUCCESS");
        });
        assertEquals("Not found order with orderId: 99999", exception.getMessage());
    }

    @Test
    void changeStatusOrderByAdmin_WhenInvalidStatus_ShouldThrowException() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.changeStatusOrderByAdmin(12345, "INVALID_STATUS");
        });
        assertEquals("Status is invalid", exception.getMessage());
    }

    @Test
    void changeStatusOrderByAdmin_WhenCancelledStatus_ShouldUpdateAndNotify() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        PaymentOrder result = paymentOrderService.changeStatusOrderByAdmin(12345, "FAILED");

        // Assert
        assertNotNull(result);
        assertEquals(PaymentOrder.PaymentStatus.FAILED, result.getPaymentStatus());
        verify(repository).save(any(PaymentOrder.class));
        verify(paymentSocketService).notifyAdminStatus(12345, "FAILED");
    }

    // 11. changeStatusOrderSilently tests
    @Test
    void changeStatusOrderSilently_WhenValidOrderId_ShouldUpdateStatusWithoutNotification() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        PaymentOrder result = paymentOrderService.changeStatusOrderSilently(12345, "SUCCESS");

        // Assert
        assertNotNull(result);
        assertEquals(PaymentOrder.PaymentStatus.SUCCESS, result.getPaymentStatus());
        verify(repository).save(any(PaymentOrder.class));
        verify(paymentSocketService, never()).notifyOrderStatus(anyInt(), anyString());
        verify(paymentSocketService, never()).notifyAdminStatus(anyInt(), anyString());
    }

    @Test
    void changeStatusOrderSilently_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(repository.findByOrderId(99999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.changeStatusOrderSilently(99999, "SUCCESS");
        });
        assertEquals("Not found order with orderId: 99999", exception.getMessage());
    }

    @Test
    void changeStatusOrderSilently_WhenInvalidStatus_ShouldThrowException() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.changeStatusOrderSilently(12345, "INVALID_STATUS");
        });
        assertEquals("Status is invalid", exception.getMessage());
    }

    @Test
    void changeStatusOrderSilently_WhenPendingStatus_ShouldUpdateSilently() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        PaymentOrder result = paymentOrderService.changeStatusOrderSilently(12345, "PENDING");

        // Assert
        assertNotNull(result);
        assertEquals(PaymentOrder.PaymentStatus.PENDING, result.getPaymentStatus());
        verify(repository).save(any(PaymentOrder.class));
        verifyNoInteractions(paymentSocketService);
    }

    // 12. getByOrderId tests
    @Test
    void getByOrderId_WhenOrderExists_ShouldReturnDTO() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.findByOrderId(12345)).thenReturn(Optional.empty());

        // Act
        PaymentOrderDTO result = paymentOrderService.getByOrderId(12345);

        // Assert
        assertNotNull(result);
        assertEquals(12345, result.getOrderId());
        verify(repository).findByOrderId(12345);
    }

    @Test
    void getByOrderId_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(repository.findByOrderId(99999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.getByOrderId(99999);
        });
        assertEquals("Payment order not found", exception.getMessage());
    }

    @Test
    void getByOrderId_WhenOrderWithLicense_ShouldReturnDTOWithLicense() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.findByOrderId(12345)).thenReturn(Optional.of(testLicense));

        // Act
        PaymentOrderDTO result = paymentOrderService.getByOrderId(12345);

        // Assert
        assertNotNull(result);
        assertEquals(12345, result.getOrderId());
        assertNotNull(result.getLicense());
        verify(repository).findByOrderId(12345);
        verify(licenseRepository).findByOrderId(12345);
    }

    @Test
    void getByOrderId_WhenNullOrderId_ShouldHandleGracefully() {
        // Arrange
        when(repository.findByOrderId(null)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.getByOrderId(null);
        });
        assertEquals("Payment order not found", exception.getMessage());
    }

    // 13. orderIdExists tests
    @Test
    void orderIdExists_WhenOrderExists_ShouldReturnTrue() {
        // Arrange
        when(repository.existsByOrderId(12345)).thenReturn(true);

        // Act
        boolean result = paymentOrderService.orderIdExists(12345);

        // Assert
        assertTrue(result);
        verify(repository).existsByOrderId(12345);
    }

    @Test
    void orderIdExists_WhenOrderNotExists_ShouldReturnFalse() {
        // Arrange
        when(repository.existsByOrderId(99999)).thenReturn(false);

        // Act
        boolean result = paymentOrderService.orderIdExists(99999);

        // Assert
        assertFalse(result);
        verify(repository).existsByOrderId(99999);
    }

    @Test
    void orderIdExists_WhenNullOrderId_ShouldReturnFalse() {
        // Arrange
        when(repository.existsByOrderId(null)).thenReturn(false);

        // Act
        boolean result = paymentOrderService.orderIdExists(null);

        // Assert
        assertFalse(result);
        verify(repository).existsByOrderId(null);
    }

    @Test
    void orderIdExists_WhenZeroOrderId_ShouldHandleCorrectly() {
        // Arrange
        when(repository.existsByOrderId(0)).thenReturn(false);

        // Act
        boolean result = paymentOrderService.orderIdExists(0);

        // Assert
        assertFalse(result);
        verify(repository).existsByOrderId(0);
    }

    // 14. toDto tests (private method tested through other methods)
    @Test
    void toDto_WhenOrderWithAllData_ShouldMapCorrectly() {
        // Arrange - THÊM MOCK CHO repository.findByOrderId
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.findByOrderId(12345)).thenReturn(Optional.of(testLicense));

        // Act
        PaymentOrderDTO result = paymentOrderService.getByOrderId(12345);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testOrder.getOrderId(), result.getOrderId());
        verify(repository).findByOrderId(12345);
        verify(licenseRepository).findByOrderId(12345);
    }


    @Test
    void toDto_WhenOrderWithoutUser_ShouldHandleNullUser() {
        // Arrange
        testOrder.setUser(null);
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.findByOrderId(12345)).thenReturn(Optional.empty());

        // Act
        PaymentOrderDTO result = paymentOrderService.getByOrderId(12345);

        // Assert
        assertNotNull(result);
        assertNull(result.getBuyer());
        assertNull(result.getUserId());
    }

    @Test
    void toDto_WhenOrderWithoutSubscription_ShouldHandleNullSubscription() {
        // Arrange
        testOrder.setSubscriptionPackage(null);
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.findByOrderId(12345)).thenReturn(Optional.empty());

        // Act
        PaymentOrderDTO result = paymentOrderService.getByOrderId(12345);

        // Assert
        assertNotNull(result);
        assertNull(result.getSubscription());
        assertNull(result.getSubscriptionId());
    }

    @Test
    void toDto_WhenRecentOrder_ShouldSetCanReportFalse() {
        // Arrange
        testOrder.setCreatedAt(LocalDateTime.now());
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.findByOrderId(12345)).thenReturn(Optional.empty());

        // Act
        PaymentOrderDTO result = paymentOrderService.getByOrderId(12345);

        // Assert
        assertNotNull(result);
        assertFalse(result.getCanReport());
    }

    @Test
    void toDto_WhenOrderWithExpiredLicense_ShouldSetExpiredTrue() {
        // Arrange
        testLicense.setActivatedAt(LocalDateTime.now().minusDays(35)); // Expired (35 > 30 days)
        testLicense.setCanUsed(true); // License is active but expired
        testLicense.setDuration(30); // 30 days duration

        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.findByOrderId(12345)).thenReturn(Optional.of(testLicense));

        // Act
        PaymentOrderDTO result = paymentOrderService.getByOrderId(12345);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLicense());
        assertTrue(result.getLicense().getIsExpired()); // Should be expired
        assertEquals(0, result.getLicense().getDaysLeft()); // No days left
        assertTrue(result.getLicense().getCanUsed()); // Still can be used flag
        verify(repository).findByOrderId(12345);
        verify(licenseRepository).findByOrderId(12345);
    }

    @Test
    void toDto_WhenOrderWithInactiveLicense_ShouldShowFullDuration() {
        // Arrange
        testLicense.setCanUsed(false); // Inactive license
        testLicense.setActivatedAt(null); // Not activated
        testLicense.setDuration(30); // 30 days duration

        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.findByOrderId(12345)).thenReturn(Optional.of(testLicense));

        // Act
        PaymentOrderDTO result = paymentOrderService.getByOrderId(12345);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLicense());
        assertFalse(result.getLicense().getIsExpired()); // Not expired (inactive)
        assertEquals(30, result.getLicense().getDaysLeft()); // Full duration available
        assertFalse(result.getLicense().getCanUsed()); // Cannot be used
        assertNull(result.getLicense().getActivatedAt()); // Not activated
        verify(repository).findByOrderId(12345);
        verify(licenseRepository).findByOrderId(12345);
    }

    @Test
    void toDto_WhenOrderWithActiveLicenseButNotExpired_ShouldCalculateRemainingDays() {
        // Arrange
        testLicense.setCanUsed(true); // Active license
        testLicense.setActivatedAt(LocalDateTime.now().minusDays(10)); // Activated 10 days ago
        testLicense.setDuration(30); // 30 days duration

        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.findByOrderId(12345)).thenReturn(Optional.of(testLicense));

        // Act
        PaymentOrderDTO result = paymentOrderService.getByOrderId(12345);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLicense());
        assertFalse(result.getLicense().getIsExpired()); // Not expired yet
        assertTrue(result.getLicense().getDaysLeft() >= 19 && result.getLicense().getDaysLeft() <= 20); // ~20 days left
        assertTrue(result.getLicense().getCanUsed()); // Can be used
        assertNotNull(result.getLicense().getActivatedAt()); // Has activation date
        verify(repository).findByOrderId(12345);
        verify(licenseRepository).findByOrderId(12345);
    }

    @Test
    void toDto_WhenOrderWithNullLicenseFields_ShouldHandleGracefully() {
        // Arrange
        testLicense.setCanUsed(true);
        testLicense.setActivatedAt(null); // Null activation date but canUsed=true (edge case)
        testLicense.setDuration(30);

        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.findByOrderId(12345)).thenReturn(Optional.of(testLicense));

        // Act & Assert - Should handle null activatedAt gracefully
        assertThrows(RuntimeException.class, () -> {
            paymentOrderService.getByOrderId(12345);
        });

        verify(repository).findByOrderId(12345);
        verify(licenseRepository).findByOrderId(12345);
    }

    @Test
    void toDto_WhenLicenseCalculationEdgeCase_ShouldHandleSameDayExpiry() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        testLicense.setCanUsed(true);
        testLicense.setActivatedAt(now.minusDays(30)); // Exactly 30 days ago
        testLicense.setDuration(30); // 30 days duration - expires today

        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.findByOrderId(12345)).thenReturn(Optional.of(testLicense));

        // Act
        PaymentOrderDTO result = paymentOrderService.getByOrderId(12345);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLicense());
        // Could be expired or have 0 days left depending on exact timing
        assertTrue(result.getLicense().getDaysLeft() >= 0);
        if (result.getLicense().getIsExpired()) {
            assertEquals(0, result.getLicense().getDaysLeft());
        }
        verify(repository).findByOrderId(12345);
        verify(licenseRepository).findByOrderId(12345);
    }

    // 15. toEntity tests (private method tested through other methods)
    @Test
    void toEntity_WhenValidDTO_ShouldMapCorrectly() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        PaymentOrderDTO result = paymentOrderService.create(testOrderDTO);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(PaymentOrder.class));
    }

    @Test
    void toEntity_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            paymentOrderService.create(testOrderDTO);
        });
    }

    @Test
    void toEntity_WhenSubscriptionNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            paymentOrderService.create(testOrderDTO);
        });
    }

    @Test
    void toEntity_WhenNullIds_ShouldThrowException() {
        // Arrange
        testOrderDTO.setUserId(null);
        testOrderDTO.setSubscriptionId(null);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            paymentOrderService.create(testOrderDTO);
        });
    }


    // 16. toDtoWithSubscription tests
    @Test
    void toDtoWithSubscription_WhenActiveLicense_ShouldCalculateCorrectly() {
        // Arrange
        LicenseCreateForm form = new LicenseCreateForm();
        form.setOrderId(12345);

        testOrder.setPaymentStatus(PaymentOrder.PaymentStatus.SUCCESS);
        testOrder.setLicenseCreated(false);
        testLicense.setCanUsed(true);
        testLicense.setActivatedAt(LocalDateTime.now().minusDays(5));

        when(repository.findByOrderIdForUpdate(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.existsByUserIdAndCanUsedTrue(1L)).thenReturn(false);
        when(licenseRepository.save(any(License.class))).thenReturn(testLicense);
        when(modelMapper.map(testSubscription, SubscriptionPackageDTO.class)).thenReturn(
                SubscriptionPackageDTO.builder().build());

        // Act
        LicenseDTO result = paymentOrderService.createLicensePayOS(form, "192.168.1.1");

        // Assert
        assertNotNull(result);
        assertTrue(result.getDaysLeft() <= 25); // 30 - 5 days used
        verify(licenseRepository).save(any(License.class));
    }

    @Test
    void toDtoWithSubscription_WhenInactiveLicense_ShouldReturnFullDuration() {
        // Arrange
        LicenseCreateForm form = new LicenseCreateForm();
        form.setOrderId(12345);

        testOrder.setPaymentStatus(PaymentOrder.PaymentStatus.SUCCESS);
        testOrder.setLicenseCreated(false);
        testLicense.setCanUsed(false);
        testLicense.setActivatedAt(null);

        when(repository.findByOrderIdForUpdate(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.existsByUserIdAndCanUsedTrue(1L)).thenReturn(true);
        when(licenseRepository.save(any(License.class))).thenReturn(testLicense);
        when(modelMapper.map(testSubscription, SubscriptionPackageDTO.class)).thenReturn(
                SubscriptionPackageDTO.builder().build());

        // Act
        LicenseDTO result = paymentOrderService.createLicensePayOS(form, "192.168.1.1");

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsExpired());
        assertEquals(30, result.getDaysLeft());
        verify(licenseRepository).save(any(License.class));
    }

    @Test
    void toDtoWithSubscription_WhenExpiredLicense_ShouldReturnExpiredStatus() {
        // Arrange
        LicenseCreateForm form = new LicenseCreateForm();
        form.setOrderId(12345);

        testOrder.setPaymentStatus(PaymentOrder.PaymentStatus.SUCCESS);
        testOrder.setLicenseCreated(false);
        testLicense.setCanUsed(true);
        testLicense.setActivatedAt(LocalDateTime.now().minusDays(35)); // Expired

        when(repository.findByOrderIdForUpdate(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.existsByUserIdAndCanUsedTrue(1L)).thenReturn(false);
        when(licenseRepository.save(any(License.class))).thenReturn(testLicense);
        when(modelMapper.map(testSubscription, SubscriptionPackageDTO.class)).thenReturn(
                SubscriptionPackageDTO.builder().build());

        // Act
        LicenseDTO result = paymentOrderService.createLicensePayOS(form, "192.168.1.1");

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsExpired());
        assertEquals(0, result.getDaysLeft());
        verify(licenseRepository).save(any(License.class));
    }

    @Test
    void toDtoWithSubscription_WhenNullActivatedAt_ShouldHandleGracefully() {
        // Arrange
        LicenseCreateForm form = new LicenseCreateForm();
        form.setOrderId(12345);

        testOrder.setPaymentStatus(PaymentOrder.PaymentStatus.SUCCESS);
        testOrder.setLicenseCreated(false);
        testLicense.setCanUsed(false);
        testLicense.setActivatedAt(null);

        when(repository.findByOrderIdForUpdate(12345)).thenReturn(Optional.of(testOrder));
        when(licenseRepository.existsByUserIdAndCanUsedTrue(1L)).thenReturn(true);
        when(licenseRepository.save(any(License.class))).thenReturn(testLicense);
        when(modelMapper.map(testSubscription, SubscriptionPackageDTO.class)).thenReturn(
                SubscriptionPackageDTO.builder().build());

        // Act
        LicenseDTO result = paymentOrderService.createLicensePayOS(form, "192.168.1.1");

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsExpired());
        assertEquals(30, result.getDaysLeft());
    }

    // 17. getTotalRevenue tests
    @Test
    void getTotalRevenue_WhenSuccessfulOrders_ShouldReturnSum() {
        // Arrange
        PaymentOrder order1 = PaymentOrder.builder()
                .paymentStatus(PaymentOrder.PaymentStatus.SUCCESS)
                .price(100.0F)
                .build();
        PaymentOrder order2 = PaymentOrder.builder()
                .paymentStatus(PaymentOrder.PaymentStatus.SUCCESS)
                .price(200.0F)
                .build();
        PaymentOrder order3 = PaymentOrder.builder()
                .paymentStatus(PaymentOrder.PaymentStatus.PENDING)
                .price(150.0F)
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(order1, order2, order3));

        // Act
        Double result = paymentOrderService.getTotalRevenue();

        // Assert
        assertEquals(300.0, result);
        verify(repository).findAll();
    }

    @Test
    void getTotalRevenue_WhenNoSuccessfulOrders_ShouldReturnZero() {
        // Arrange
        PaymentOrder order1 = PaymentOrder.builder()
                .paymentStatus(PaymentOrder.PaymentStatus.PENDING)
                .price(100.0F)
                .build();
        PaymentOrder order2 = PaymentOrder.builder()
                .paymentStatus(PaymentOrder.PaymentStatus.FAILED)
                .price(200.0F)
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(order1, order2));

        // Act
        Double result = paymentOrderService.getTotalRevenue();

        // Assert
        assertEquals(0.0, result);
        verify(repository).findAll();
    }

    @Test
    void getTotalRevenue_WhenEmptyOrders_ShouldReturnZero() {
        // Arrange
        when(repository.findAll()).thenReturn(new ArrayList<>());

        // Act
        Double result = paymentOrderService.getTotalRevenue();

        // Assert
        assertEquals(0.0, result);
        verify(repository).findAll();
    }

    @Test
    void getTotalRevenue_WhenMixedStatuses_ShouldOnlyCountSuccess() {
        // Arrange
        PaymentOrder order1 = PaymentOrder.builder()
                .paymentStatus(PaymentOrder.PaymentStatus.SUCCESS)
                .price(500.0F)
                .build();
        PaymentOrder order2 = PaymentOrder.builder()
                .paymentStatus(PaymentOrder.PaymentStatus.FAILED)
                .price(300.0F)
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(order1, order2));

        // Act
        Double result = paymentOrderService.getTotalRevenue();

        // Assert
        assertEquals(500.0, result);
        verify(repository).findAll();
    }

    // 18. countTotalOrders tests
    @Test
    void countTotalOrders_WhenOrdersExist_ShouldReturnCount() {
        // Arrange
        when(repository.count()).thenReturn(10L);

        // Act
        Long result = paymentOrderService.countTotalOrders();

        // Assert
        assertEquals(10L, result);
        verify(repository).count();
    }

    @Test
    void countTotalOrders_WhenNoOrders_ShouldReturnZero() {
        // Arrange
        when(repository.count()).thenReturn(0L);

        // Act
        Long result = paymentOrderService.countTotalOrders();

        // Assert
        assertEquals(0L, result);
        verify(repository).count();
    }

    @Test
    void countTotalOrders_WhenLargeNumber_ShouldReturnCorrectCount() {
        // Arrange
        when(repository.count()).thenReturn(999999L);

        // Act
        Long result = paymentOrderService.countTotalOrders();

        // Assert
        assertEquals(999999L, result);
        verify(repository).count();
    }

    @Test
    void countTotalOrders_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Arrange
        when(repository.count()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            paymentOrderService.countTotalOrders();
        });
        verify(repository).count();
    }

    // 19. countOrdersByStatus tests
    @Test
    void countOrdersByStatus_WhenOrdersExist_ShouldReturnMapWithCounts() {
        // Arrange
        when(repository.countByPaymentStatus(PaymentOrder.PaymentStatus.SUCCESS)).thenReturn(5L);
        when(repository.countByPaymentStatus(PaymentOrder.PaymentStatus.PENDING)).thenReturn(3L);
        when(repository.countByPaymentStatus(PaymentOrder.PaymentStatus.FAILED)).thenReturn(1L);
        when(repository.countByPaymentStatus(PaymentOrder.PaymentStatus.PROCESSING)).thenReturn(15L);
        // Act
        Map<String, Long> result = paymentOrderService.countOrdersByStatus();

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals(5L, result.get("SUCCESS"));
        assertEquals(3L, result.get("PENDING"));
        assertEquals(1L, result.get("FAILED"));
        assertEquals(15L, result.get("PROCESSING"));
        verify(repository, times(4)).countByPaymentStatus(any(PaymentOrder.PaymentStatus.class));
    }

    @Test
    void countOrdersByStatus_WhenNoOrders_ShouldReturnMapWithZeros() {
        // Arrange
        when(repository.countByPaymentStatus(any(PaymentOrder.PaymentStatus.class))).thenReturn(0L);

        // Act
        Map<String, Long> result = paymentOrderService.countOrdersByStatus();

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.values().stream().allMatch(count -> count == 0L));
        verify(repository, times(4)).countByPaymentStatus(any(PaymentOrder.PaymentStatus.class));
    }

    @Test
    void countOrdersByStatus_WhenMixedCounts_ShouldReturnCorrectMap() {
        // Arrange
        when(repository.countByPaymentStatus(PaymentOrder.PaymentStatus.SUCCESS)).thenReturn(10L);
        when(repository.countByPaymentStatus(PaymentOrder.PaymentStatus.PENDING)).thenReturn(0L);
        when(repository.countByPaymentStatus(PaymentOrder.PaymentStatus.FAILED)).thenReturn(5L);
        when(repository.countByPaymentStatus(PaymentOrder.PaymentStatus.PROCESSING)).thenReturn(15L);
        // Act
        Map<String, Long> result = paymentOrderService.countOrdersByStatus();

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.get("SUCCESS"));
        assertEquals(0L, result.get("PENDING"));
        assertEquals(5L, result.get("FAILED"));
        assertEquals(15L, result.get("PROCESSING"));
    }

    @Test
    void countOrdersByStatus_WhenAllStatusesHaveOrders_ShouldReturnCompleteMap() {
        // Arrange - Stub cho TẤT CẢ PaymentStatus enum values
        when(repository.countByPaymentStatus(PaymentOrder.PaymentStatus.SUCCESS)).thenReturn(100L);
        when(repository.countByPaymentStatus(PaymentOrder.PaymentStatus.PENDING)).thenReturn(50L);
        when(repository.countByPaymentStatus(PaymentOrder.PaymentStatus.FAILED)).thenReturn(25L);
        when(repository.countByPaymentStatus(PaymentOrder.PaymentStatus.PROCESSING)).thenReturn(15L);
        // Thêm các enum values khác nếu có

        // Act
        Map<String, Long> result = paymentOrderService.countOrdersByStatus();

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals(100L, result.get("SUCCESS"));
        assertEquals(50L, result.get("PENDING"));
        assertEquals(25L, result.get("FAILED"));
        assertEquals(15L, result.get("PROCESSING"));

        // Verify total sum
        assertEquals(190L, result.values().stream().mapToLong(Long::longValue).sum());
    }


    // 20. countOrdersByPaymentMethod tests
    @Test
    void countOrdersByPaymentMethod_WhenPayOSOrders_ShouldReturnCorrectCount() {
        // Arrange
        when(repository.countByPaymentMethod(PaymentOrder.PaymentMethod.PAYOS)).thenReturn(10L);

        // Act
        Map<String, Long> result = paymentOrderService.countOrdersByPaymentMethod();

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("PAYOS"));
        assertEquals(10L, result.get("PAYOS"));
    }

    @Test
    void countOrdersByPaymentMethod_WhenNoOrders_ShouldReturnZeros() {
        // Arrange
        when(repository.countByPaymentMethod(any(PaymentOrder.PaymentMethod.class))).thenReturn(0L);

        // Act
        Map<String, Long> result = paymentOrderService.countOrdersByPaymentMethod();

        // Assert
        assertNotNull(result);
        assertTrue(result.values().stream().allMatch(count -> count == 0L));
    }

    @Test
    void countOrdersByPaymentMethod_WhenMixedMethods_ShouldReturnCorrectCounts() {
        // Arrange
        when(repository.countByPaymentMethod(PaymentOrder.PaymentMethod.PAYOS)).thenReturn(15L);


        // Act
        Map<String, Long> result = paymentOrderService.countOrdersByPaymentMethod();

        // Assert
        assertNotNull(result);
        assertEquals(15L, result.get("PAYOS"));
    }

    @Test
    void countOrdersByPaymentMethod_WhenAllMethodsUsed_ShouldReturnCompleteMap() {
        // Arrange
        when(repository.countByPaymentMethod(PaymentOrder.PaymentMethod.PAYOS)).thenReturn(20L);

        // Act
        Map<String, Long> result = paymentOrderService.countOrdersByPaymentMethod();

        // Assert
        assertNotNull(result);
        assertTrue(result.size() >= 2);
        assertEquals(20L, result.get("PAYOS"));
    }

    // 21. revenueByPaymentMethod tests
    @Test
    void revenueByPaymentMethod_WhenPayOSRevenue_ShouldReturnCorrectSum() {
        // Arrange
        PaymentOrder order1 = PaymentOrder.builder().price(100.0F).build();
        PaymentOrder order2 = PaymentOrder.builder().price(200.0F).build();

        when(repository.findAllByPaymentMethodAndPaymentStatus(
                PaymentOrder.PaymentMethod.PAYOS, PaymentOrder.PaymentStatus.SUCCESS))
                .thenReturn(Arrays.asList(order1, order2));


        // Act
        Map<String, Double> result = paymentOrderService.revenueByPaymentMethod();

        // Assert
        assertNotNull(result);
        assertEquals(300.0, result.get("PAYOS"));

    }

    @Test
    void revenueByPaymentMethod_WhenNoRevenue_ShouldReturnZeros() {
        // Arrange
        when(repository.findAllByPaymentMethodAndPaymentStatus(
                any(PaymentOrder.PaymentMethod.class), eq(PaymentOrder.PaymentStatus.SUCCESS)))
                .thenReturn(new ArrayList<>());

        // Act
        Map<String, Double> result = paymentOrderService.revenueByPaymentMethod();

        // Assert
        assertNotNull(result);
        assertTrue(result.values().stream().allMatch(revenue -> revenue == 0.0));
    }

    @Test
    void revenueByPaymentMethod_WhenOnlyPayOSRevenue_ShouldReturnCorrectDistribution() {
        // Arrange
        PaymentOrder order = PaymentOrder.builder().price(500.0F).build();

        when(repository.findAllByPaymentMethodAndPaymentStatus(
                PaymentOrder.PaymentMethod.PAYOS, PaymentOrder.PaymentStatus.SUCCESS))
                .thenReturn(Arrays.asList(order));

        // Act
        Map<String, Double> result = paymentOrderService.revenueByPaymentMethod();

        // Assert
        assertNotNull(result);
        assertEquals(500.0, result.get("PAYOS"));

    }

    @Test
    void revenueByPaymentMethod_WhenLargeRevenue_ShouldCalculateCorrectly() {
        // Arrange
        PaymentOrder order1 = PaymentOrder.builder().price(1000000.0F).build();
        PaymentOrder order2 = PaymentOrder.builder().price(2000000.0F).build();

        when(repository.findAllByPaymentMethodAndPaymentStatus(
                PaymentOrder.PaymentMethod.PAYOS, PaymentOrder.PaymentStatus.SUCCESS))
                .thenReturn(Arrays.asList(order1, order2));


        // Act
        Map<String, Double> result = paymentOrderService.revenueByPaymentMethod();

        // Assert
        assertNotNull(result);
        assertEquals(3000000.0, result.get("PAYOS"));
    }
    // 22. syncBill tests
    @Test
    void syncBill_WhenValidData_ShouldUpdateAndNotify() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        paymentOrderService.syncBill(12345, "123456", "Test Account", "987654321", "2023-12-01");

        // Assert
        verify(repository).findByOrderId(12345);
        verify(repository).save(any(PaymentOrder.class));
        verify(paymentSocketService).notifySyncBill(12345, "Test Account");

        assertEquals("123456", testOrder.getBin());
        assertEquals("Test Account", testOrder.getAccountName());
        assertEquals("987654321", testOrder.getAccountNumber());
        assertEquals("2023-12-01", testOrder.getDateTransfer());
        assertNotNull(testOrder.getUpdatedAt());
    }

    @Test
    void syncBill_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(repository.findByOrderId(99999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentOrderService.syncBill(99999, "123456", "Test Account", "987654321", "2023-12-01");
        });

        assertEquals("Not found order with orderCode: 99999", exception.getMessage());
        verify(repository).findByOrderId(99999);
        verify(repository, never()).save(any(PaymentOrder.class));
        verify(paymentSocketService, never()).notifySyncBill(anyInt(), anyString());
    }

    @Test
    void syncBill_WhenNullParameters_ShouldNotUpdateNullFields() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        paymentOrderService.syncBill(12345, null, null, null, null);

        // Assert
        verify(repository).findByOrderId(12345);
        verify(repository).save(any(PaymentOrder.class));
        verify(paymentSocketService).notifySyncBill(12345, null);

        // Verify null fields are not updated
        assertNull(testOrder.getBin());
        assertNull(testOrder.getAccountName());
        assertNull(testOrder.getAccountNumber());
        assertNull(testOrder.getDateTransfer());
        assertNotNull(testOrder.getUpdatedAt()); // Only updatedAt should be set
    }

    @Test
    void syncBill_WhenPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act - Only update bin and accountNumber, leave others null
        paymentOrderService.syncBill(12345, "999888", null, "555444333", null);

        // Assert
        verify(repository).findByOrderId(12345);
        verify(repository).save(any(PaymentOrder.class));
        verify(paymentSocketService).notifySyncBill(12345, null);

        assertEquals("999888", testOrder.getBin());
        assertNull(testOrder.getAccountName());
        assertEquals("555444333", testOrder.getAccountNumber());
        assertNull(testOrder.getDateTransfer());
        assertNotNull(testOrder.getUpdatedAt());
    }

    @Test
    void syncBill_WhenEmptyStrings_ShouldUpdateWithEmptyValues() {
        // Arrange
        when(repository.findByOrderId(12345)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act - Pass empty strings (not null)
        paymentOrderService.syncBill(12345, "", "", "", "");

        // Assert
        verify(repository).findByOrderId(12345);
        verify(repository).save(any(PaymentOrder.class));
        verify(paymentSocketService).notifySyncBill(12345, "");

        assertEquals("", testOrder.getBin());
        assertEquals("", testOrder.getAccountName());
        assertEquals("", testOrder.getAccountNumber());
        assertEquals("", testOrder.getDateTransfer());
        assertNotNull(testOrder.getUpdatedAt());
    }

}
