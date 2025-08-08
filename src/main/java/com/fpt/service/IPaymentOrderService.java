package com.fpt.service;

import com.fpt.dto.LicenseDTO;
import com.fpt.dto.PaymentOrderDTO;
import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import com.fpt.form.LicenseCreateForm;
import com.fpt.form.OrderFormCreating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IPaymentOrderService {
    Page<PaymentOrderDTO> getAllOrder(Pageable pageable, String search, Long subscriptionId, PaymentOrder.PaymentStatus status, SubscriptionPackage.TypePackage type);
    Page<PaymentOrderDTO> getUserOrder(Pageable pageable, String search,Long subscriptionId, PaymentOrder.PaymentStatus status, Long userId,SubscriptionPackage.TypePackage type);
    List<PaymentOrderDTO> convertToDto(List<PaymentOrder> paymentOrders);
    PaymentOrderDTO createOrder(OrderFormCreating form, Long userId);
    void updateOrderFromWebhook(int orderCode, String internalStatus,
                                String bin, String accountName, String accountNumber,String dateTransfer,String id);
    void syncBill(int orderCode, String bin, String accountName, String accountNumber,String dateTransfer);
    void addReasonCancel(int orderCode, String cancelReason,String dateTransfer);
    PaymentOrder changeStatusOrder(Long orderId, String newStatus);
    PaymentOrder changeStatusOrderByAdmin(Integer orderId, String newStatus);
    PaymentOrder changeStatusOrderByOrderId(Integer orderId, String newStatus);
     LicenseDTO createLicensePayOS(LicenseCreateForm form, String ip);
    PaymentOrder changeStatusOrderIdCreateLicense(Integer orderId, String newStatus, String ip);
    PaymentOrder changeStatusOrderSilently(Integer orderId, String newStatus);

    List<PaymentOrderDTO> getAll();
//    List<PaymentOrderDTO> getByUserId(Long userId);
    PaymentOrderDTO getById(Long id);
    PaymentOrderDTO getByOrderId(Integer orderId);
    boolean orderExists(Long id);
    boolean orderIdExists(Integer id);
    PaymentOrderDTO create(PaymentOrderDTO dto);
    PaymentOrderDTO update(Long id, PaymentOrderDTO dto);
    void delete(Long id);
    Double getTotalRevenue();
    Long countTotalOrders();
    Map<String, Long> countOrdersByStatus();
    Map<String, Long> countOrdersByPaymentMethod();
    Map<String, Double> revenueByPaymentMethod();
}
