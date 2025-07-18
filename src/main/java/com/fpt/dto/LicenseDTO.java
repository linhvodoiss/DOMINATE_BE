package com.fpt.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicenseDTO {
    private Long id;
    private Integer orderId;
    private String licenseKey;
    private Integer duration;
    private String ip;
    private String hardwareId;
    private Long userId;
    private Long subscriptionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime activatedAt;
    private Boolean isExpired;
    private Integer daysLeft;
    private Boolean canUsed;
    private SubscriptionPackageDTO subscription;
}
