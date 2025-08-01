package com.fpt.repository;

import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long>, JpaSpecificationExecutor<PaymentOrder> {
    List<PaymentOrder> findByUserId(Long userId);
    boolean existsByOrderId(Integer orderId);
    Optional<PaymentOrder> findByOrderId(Integer orderId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM PaymentOrder o WHERE o.orderId = :orderId")
    Optional<PaymentOrder> findByOrderIdForUpdate(@Param("orderId") Integer orderId);

    Long countByPaymentStatus(PaymentOrder.PaymentStatus status);
    Long countByPaymentMethod(PaymentOrder.PaymentMethod method);

    Long countBySubscriptionPackageIdAndPaymentStatus(Long subscriptionId, PaymentOrder.PaymentStatus status);

    List<PaymentOrder> findAllByPaymentMethodAndPaymentStatus(
            PaymentOrder.PaymentMethod method,
            PaymentOrder.PaymentStatus status
    );

}
