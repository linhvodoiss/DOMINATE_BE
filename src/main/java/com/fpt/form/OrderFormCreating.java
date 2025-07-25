package com.fpt.form;

import com.fpt.entity.PaymentOrder;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class OrderFormCreating {

    @NotNull(message = "Package is undefined")
    private Long subscriptionId;

    @NotNull(message = "Payment method is not blank")
    private PaymentOrder.PaymentMethod paymentMethod;

    @NotNull(message = "Link payment is not blank")
    private String paymentLink;
    @NotNull(message = "Price is not blank")
    private Float price;
    @NotNull(message = "Order Id is not blank")
    @Min(value = 10_000_000, message = "orderId must have 8-9 digit")
    @Max(value = 999_999_999, message = "orderId must have 8-9 digit")
    private Integer orderId;
    private String bin;
    private String accountName;
    private String accountNumber;
    private String qrCode;
}

