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
    private Float price;
    private String paymentLink;
    private String bin;
    private String accountName;
    private String accountNumber;
    private String cancelReason;
    private String paymentStatus;
    private String paymentMethod;
    private Boolean licenseCreated;
    private Long userId;
    private Long subscriptionId;
    private LicenseDTO license;
    private String dateTransfer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean canReport;
    private SubscriptionPackageDTO subscription;
    private UserDTO buyer;
}


