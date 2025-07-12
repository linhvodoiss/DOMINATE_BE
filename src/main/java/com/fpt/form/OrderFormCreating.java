package com.fpt.form;

import com.fpt.entity.PaymentOrder;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class OrderFormCreating {

    @NotNull(message = "Không xác định được đơn hàng")
    private Long subscriptionId;

    @NotNull(message = "Bạn chưa chọn phương thức thanh toán")
    private PaymentOrder.PaymentMethod paymentMethod;

    @NotNull(message = "Bạn chưa điền link thanh toán")
    private String paymentLink;

    @NotNull(message = "Không được để trống orderId")
    @Min(value = 100_000_000, message = "orderId phải có 9 chữ số")
    @Max(value = 999_999_999, message = "orderId phải có 9 chữ số")
    private Integer orderId;
    private String bin;
    private String accountName;
    private String accountNumber;
    private String qrCode;
}

