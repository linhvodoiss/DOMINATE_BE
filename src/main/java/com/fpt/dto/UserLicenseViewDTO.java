package com.fpt.dto;

import java.time.LocalDateTime;

public interface UserLicenseViewDTO {
    Long getId(); // license id
    String getLicenseKey();
    Integer getDuration();
    String getIp();
    String getSubscriptionName();
}
