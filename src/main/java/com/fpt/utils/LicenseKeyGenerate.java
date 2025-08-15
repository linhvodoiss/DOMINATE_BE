package com.fpt.utils;

import com.fpt.repository.LicenseRepository;

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
    public static String generateUniqueLicenseKey(LicenseRepository licenseRepository) {
        String licenseKey;
        int attempts = 0;

        do {
            licenseKey = generateLicenseKey();
            attempts++;

            if (attempts > 10) {
                throw new RuntimeException("Failed to generate unique license key after 10 attempts.");
            }

        } while (licenseRepository.existsByLicenseKey(licenseKey));

        return licenseKey;
    }
}
