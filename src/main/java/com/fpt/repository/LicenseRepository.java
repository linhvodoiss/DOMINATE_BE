package com.fpt.repository;

import com.fpt.dto.UserLicenseViewDTO;
import com.fpt.entity.License;
import com.fpt.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long>, JpaSpecificationExecutor<License> {
    List<License> findByUserId(Long userId);
    Optional<License> findByLicenseKey(String licenseKey);
    boolean existsByUserIdAndCanUsedTrue(Long userId);
    Optional<License> findByOrderId(Integer orderId);
    Optional<License> findTopByUserIdAndCanUsedOrderByCreatedAtDesc(Long userId, boolean canUsed);
    Optional<License> findTopByUserIdAndCanUsedOrderByCreatedAtAsc(Long userId, boolean canUsed);

}
