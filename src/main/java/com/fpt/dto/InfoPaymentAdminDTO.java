package com.fpt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoPaymentAdminDTO {
    private Long orderCode;
    private Integer amount;
    private Integer amountPaid;
    private String status;
    private String createdAt;

    private String transactionDateTime;
    private String counterAccountName;
    private String counterAccountNumber;
    private String reference;
    private String description;

    private String canceledAt;
    private String cancellationReason;
}
