package com.fpt.service;

import com.fpt.dto.PaymentOrderDTO;
import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import com.fpt.form.OrderFormCreating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IPaymentOrderService {
    Page<PaymentOrderDTO> getAllPackage(Pageable pageable, String search, Long subscriptionId, PaymentOrder.PaymentStatus status, SubscriptionPackage.TypePackage type);
    Page<PaymentOrderDTO> getUserPackage(Pageable pageable, String search,Long subscriptionId, PaymentOrder.PaymentStatus status, Long userId,SubscriptionPackage.TypePackage type);
    List<PaymentOrderDTO> convertToDto(List<PaymentOrder> paymentOrders);
    PaymentOrderDTO createOrder(OrderFormCreating form, Long userId);
    PaymentOrder changeStatusOrder(Long orderId, String newStatus);
    PaymentOrder changeStatusOrderByAdmin(Integer orderId, String newStatus);
    PaymentOrder changeStatusOrderByOrderId(Integer orderId, String newStatus);
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
}
