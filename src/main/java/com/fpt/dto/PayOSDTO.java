package com.fpt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOSDTO {
    private String bin;                // Mã định danh ngân hàng (BIN)

    private String accountNumber;     // Số tài khoản ngân hàng

    private String accountName;       // Tên tài khoản (5-50 ký tự)

    private String currency;          // Đơn vị tiền tệ

    private String paymentLinkId;     // Mã link thanh toán

    private Integer amount;           // Số tiền thanh toán

    private String description;       // Mô tả thanh toán

    private Integer orderCode;        // Mã đơn hàng

    private String status;            // Trạng thái link

    private String checkoutUrl;       // Link thanh toán

    private String qrCode;            // Mã VietQR (dạng text)
}
