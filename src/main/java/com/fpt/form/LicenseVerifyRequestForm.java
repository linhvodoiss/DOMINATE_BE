package com.fpt.form;

import lombok.Data;

@Data
public class LicenseVerifyRequestForm {
    private String licenseKey;
    private String hardwareId;
    private Long userId;
}
