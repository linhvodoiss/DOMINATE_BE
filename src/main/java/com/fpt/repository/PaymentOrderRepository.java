package com.fpt.repository;

import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long>, JpaSpecificationExecutor<PaymentOrder> {
    List<PaymentOrder> findByUserId(Long userId);
    boolean existsByOrderId(Integer orderId);
    Optional<PaymentOrder> findByOrderId(Integer orderId);
    Long countByPaymentStatus(PaymentOrder.PaymentStatus status);
    Long countByPaymentMethod(PaymentOrder.PaymentMethod method);

    List<PaymentOrder> findAllByPaymentMethodAndPaymentStatus(
            PaymentOrder.PaymentMethod method,
            PaymentOrder.PaymentStatus status
    );

}
