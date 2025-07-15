package com.fpt.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LicenseVerifyResponse {
    private boolean valid;
    private int code;
    private String type;
    private String message;
    private LocalDateTime expiresAt;
}
