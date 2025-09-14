package org.retromc.jcode;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Generator {
    private final String secretKey;
    private final int codeLength = 6;

    public Generator(String secretKey) {
        this.secretKey = secretKey;
    }

    public String generateCode(UUID userUUID, String service) {
        String utc5MinuteEpochBlock = getUTC5MinuteEpochBlock();
        return generateCode(userUUID, utc5MinuteEpochBlock, service);
    }

    // Validate code with only current time block
    public boolean validateCode(UUID userUUID, String service, String code) {
        String utc5MinuteEpochBlock = getUTC5MinuteEpochBlock();
        return validateCode(userUUID, utc5MinuteEpochBlock, service, code);
    }

    // Overload: default to +/-1 block (5 minutes of skew on either side)
    public boolean validateCodeWithSkew(UUID userUUID, String service, String code) {
        return validateCodeWithSkew(userUUID, service, code, 1);
    }

    /**
     * Validate a code allowing for +/- `window` five-minute blocks of clock skew.
     * Example: window=1 checks previous, current, and next blocks.
     */
    public boolean validateCodeWithSkew(UUID userUUID, String service, String code, int window) {
        if (userUUID == null || service == null || code == null) return false;
        if (window < 0) window = 0;

        // Normalize once
        final String normalizedService = service.trim().toLowerCase(java.util.Locale.ROOT);

        // Compute current block once, then scan within window
        final long currentBlock = Long.parseLong(getUTC5MinuteEpochBlock());
        for (long delta = -window; delta <= window; delta++) {
            final String blockStr = Long.toString(currentBlock + delta);
            if (validateCode(userUUID, blockStr, normalizedService, code)) {
                return true; // short-circuit on first successful match
            }
        }
        return false;
    }

    private boolean validateCode(UUID userUUID, String utc5MinuteEpochBlock, String service, String code) {
        String expectedCode = generateCode(userUUID, utc5MinuteEpochBlock, service);
        return expectedCode.equalsIgnoreCase(code);
    }

    private String generateCode(UUID userUUID, String utc5MinuteEpochBlock, String service) {
        String codeString = userUUID.toString() + ":" + secretKey + ":" + utc5MinuteEpochBlock + ":" + service;
        try {
            String hash = generateSHA256(codeString);
            return hash.substring(0, codeLength).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private String getUTC5MinuteEpochBlock() {
        long epochMinutes = System.currentTimeMillis() / 60000; // ms → minutes
        long block = epochMinutes / 5; // group into 5-minute chunks
        return String.valueOf(block);
    }

    private String generateSHA256(String message) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.reset();
        sha256.update(message.getBytes());
        byte[] digest = sha256.digest();
        return String.format("%0" + (digest.length << 1) + "x", new Object[]{new BigInteger(1, digest)});
    }

}
