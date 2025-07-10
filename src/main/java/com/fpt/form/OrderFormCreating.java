package com.fpt.form;

import com.fpt.entity.PaymentOrder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrderFormCreating {

    @NotNull(message = "Không xác định được đơn hàng")
    private Long subscriptionId;

    @NotNull(message = "Bạn chưa chọn phương thức thanh toán")
    private PaymentOrder.PaymentMethod paymentMethod;

}

