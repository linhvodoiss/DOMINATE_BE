package com.fpt.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrderDTO {
    private Long id;
    private Integer orderId;
    private String paymentLink;
    private String bin;
    private String accountName;
    private String accountNumber;
    private String qrCode;
    private String paymentStatus;
    private String paymentMethod;
    private Boolean licenseCreated;
    private Long userId;
    private Long subscriptionId;
    private LicenseDTO license;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean canReport;
    private SubscriptionPackageDTO subscription;
}


