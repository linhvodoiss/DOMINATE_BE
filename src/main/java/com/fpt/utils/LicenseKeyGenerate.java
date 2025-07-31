package com.fpt.utils;

import java.security.SecureRandom;

public class LicenseKeyGenerate {
    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateLicenseKey() {
        StringBuilder key = new StringBuilder("DOM-");

        for (int i = 0; i < 4; i++) {
            if (i > 0) key.append('-');
            for (int j = 0; j < 4; j++) {
                key.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
            }
        }

        return key.toString(); // Example: DOM-K4L9-W3TZ-7XCU
    }
}
