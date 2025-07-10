package com.fpt.form;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class PayOSForm {
    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 25, message = "Mô tả không được vượt quá 25 ký tự")
    private String description;

    @NotNull(message = "Số tiền không được để trống")
    @Min(value = 1, message = "Số tiền phải lớn hơn 0")
    private Long amount;

    @NotNull(message = "Mã đơn hàng không được để trống")
    private Long orderCode;
    private String returnUrl;
    private String cancelUrl;
}
