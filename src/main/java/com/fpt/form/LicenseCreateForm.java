// LicenseCreateForm.java
package com.fpt.form;

import com.fpt.entity.SubscriptionPackage;
import lombok.Data;

@Data
public class LicenseCreateForm {
    private Integer orderId;
    private String hardwareId;
    private String licenseKey;
    private SubscriptionPackage.TypePackage type;
}
