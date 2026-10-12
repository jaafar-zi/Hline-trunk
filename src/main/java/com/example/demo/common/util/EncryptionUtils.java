package com.example.demo.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility class for encryption and decryption operations
 *
 * Features:
 * - AES encryption (CBC, GCM modes)
 * - RSA encryption
 * - Password-based encryption (PBKDF2)
 * - Hashing (MD5, SHA-256, SHA-512, BCrypt)
 * - Key generation
 * - Digital signatures
 * - HMAC generation
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class EncryptionUtils {

    // ========== Constants ==========

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_CBC_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String HMAC_SHA512_ALGORITHM = "HmacSHA512";

    private static final int AES_KEY_SIZE = 256;
    private static final int RSA_KEY_SIZE = 2048;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int CBC_IV_LENGTH = 16;
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int SALT_LENGTH = 16;

    // ========== AES Encryption (GCM Mode - Recommended) ==========

    /**
     * Generate AES key
     */
    public static SecretKey generateAesKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGen.init(AES_KEY_SIZE, new SecureRandom());
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate AES key", e);
            throw new EncryptionException("Failed to generate AES key", e);
        }
    }

    /**
     * Generate AES key from password using PBKDF2
     */
    public static SecretKey generateAesKeyFromPassword(String password, byte[] salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_SIZE);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), AES_ALGORITHM);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Failed to generate AES key from password", e);
            throw new EncryptionException("Failed to generate AES key from password", e);
        }
    }

    /**
     * Encrypt data using AES-GCM (Authenticated Encryption)
     */
    public static String encryptAesGcm(String plainText, SecretKey secretKey) {
        if (StringUtils.isBlank(plainText) || secretKey == null) {
            throw new IllegalArgumentException("PlainText and SecretKey must not be null");
        }

        try {
            // Generate random IV
            byte[] iv = generateRandomBytes(GCM_IV_LENGTH);

            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("AES-GCM encryption failed", e);
            throw new EncryptionException("AES-GCM encryption failed", e);
        }
    }

    /**
     * Decrypt data using AES-GCM
     */
    public static String decryptAesGcm(String encryptedText, SecretKey secretKey) {
        if (StringUtils.isBlank(encryptedText) || secretKey == null) {
            throw new IllegalArgumentException("EncryptedText and SecretKey must not be null");
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);

            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES-GCM decryption failed", e);
            throw new EncryptionException("AES-GCM decryption failed", e);
        }
    }

    // ========== AES Encryption (CBC Mode) ==========

    /**
     * Encrypt data using AES-CBC
     */
    public static String encryptAesCbc(String plainText, SecretKey secretKey) {
        if (StringUtils.isBlank(plainText) || secretKey == null) {
            throw new IllegalArgumentException("PlainText and SecretKey must not be null");
        }

        try {
            // Generate random IV
            byte[] iv = generateRandomBytes(CBC_IV_LENGTH);

            Cipher cipher = Cipher.getInstance(AES_CBC_TRANSFORMATION);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("AES-CBC encryption failed", e);
            throw new EncryptionException("AES-CBC encryption failed", e);
        }
    }

    /**
     * Decrypt data using AES-CBC
     */
    public static String decryptAesCbc(String encryptedText, SecretKey secretKey) {
        if (StringUtils.isBlank(encryptedText) || secretKey == null) {
            throw new IllegalArgumentException("EncryptedText and SecretKey must not be null");
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);

            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[CBC_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(AES_CBC_TRANSFORMATION);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES-CBC decryption failed", e);
            throw new EncryptionException("AES-CBC decryption failed", e);
        }
    }

    // ========== Simplified AES Methods (Password-based) ==========

    /**
     * Encrypt using password (AES-GCM with PBKDF2)
     */
    public static String encrypt(String plainText, String password) {
        if (StringUtils.isBlank(plainText) || StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("PlainText and Password must not be null");
        }

        try {
            // Generate random salt
            byte[] salt = generateRandomBytes(SALT_LENGTH);

            // Derive key from password
            SecretKey secretKey = generateAesKeyFromPassword(password, salt);

            // Encrypt
            byte[] iv = generateRandomBytes(GCM_IV_LENGTH);
            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine salt, IV, and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(salt.length + iv.length + cipherText.length);
            byteBuffer.put(salt);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Password-based encryption failed", e);
            throw new EncryptionException("Password-based encryption failed", e);
        }
    }

    /**
     * Decrypt using password (AES-GCM with PBKDF2)
     */
    public static String decrypt(String encryptedText, String password) {
        if (StringUtils.isBlank(encryptedText) || StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("EncryptedText and Password must not be null");
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);

            // Extract salt, IV, and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] salt = new byte[SALT_LENGTH];
            byteBuffer.get(salt);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            // Derive key from password
            SecretKey secretKey = generateAesKeyFromPassword(password, salt);

            // Decrypt
            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Password-based decryption failed", e);
            throw new EncryptionException("Password-based decryption failed", e);
        }
    }

    // ========== RSA Encryption ==========

    /**
     * Generate RSA key pair
     */
    public static KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyGen.initialize(RSA_KEY_SIZE, new SecureRandom());
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate RSA key pair", e);
            throw new EncryptionException("Failed to generate RSA key pair", e);
        }
    }

    /**
     * Encrypt data using RSA public key
     */
    public static String encryptRsa(String plainText, PublicKey publicKey) {
        if (StringUtils.isBlank(plainText) || publicKey == null) {
            throw new IllegalArgumentException("PlainText and PublicKey must not be null");
        }

        try {
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("RSA encryption failed", e);
            throw new EncryptionException("RSA encryption failed", e);
        }
    }

    /**
     * Decrypt data using RSA private key
     */
    public static String decryptRsa(String encryptedText, PrivateKey privateKey) {
        if (StringUtils.isBlank(encryptedText) || privateKey == null) {
            throw new IllegalArgumentException("EncryptedText and PrivateKey must not be null");
        }

        try {
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("RSA decryption failed", e);
            throw new EncryptionException("RSA decryption failed", e);
        }
    }

    // ========== Key Encoding/Decoding ==========

    /**
     * Encode SecretKey to Base64 string
     */
    public static String encodeKey(SecretKey key) {
        if (key == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Decode SecretKey from Base64 string
     */
    public static SecretKey decodeAesKey(String encodedKey) {
        if (StringUtils.isBlank(encodedKey)) {
            return null;
        }
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, AES_ALGORITHM);
    }

    /**
     * Encode PublicKey to Base64 string
     */
    public static String encodePublicKey(PublicKey publicKey) {
        if (publicKey == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Decode PublicKey from Base64 string
     */
    public static PublicKey decodePublicKey(String encodedKey) {
        if (StringUtils.isBlank(encodedKey)) {
            return null;
        }

        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            log.error("Failed to decode public key", e);
            throw new EncryptionException("Failed to decode public key", e);
        }
    }

    /**
     * Encode PrivateKey to Base64 string
     */
    public static String encodePrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Decode PrivateKey from Base64 string
     */
    public static PrivateKey decodePrivateKey(String encodedKey) {
        if (StringUtils.isBlank(encodedKey)) {
            return null;
        }

        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("Failed to decode private key", e);
            throw new EncryptionException("Failed to decode private key", e);
        }
    }

    // ========== Hashing ==========

    /**
     * Generate MD5 hash
     */
    public static String md5(String input) {
        return hash(input, "MD5");
    }

    /**
     * Generate SHA-1 hash
     */
    public static String sha1(String input) {
        return hash(input, "SHA-1");
    }

    /**
     * Generate SHA-256 hash
     */
    public static String sha256(String input) {
        return hash(input, "SHA-256");
    }

    /**
     * Generate SHA-512 hash
     */
    public static String sha512(String input) {
        return hash(input, "SHA-512");
    }

    /**
     * Generate hash with salt
     */
    public static String hashWithSalt(String input, String salt) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        return sha256(input + salt);
    }

    /**
     * Generate hash using specified algorithm
     */
    private static String hash(String input, String algorithm) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("Hashing algorithm not found: {}", algorithm, e);
            throw new EncryptionException("Hashing failed", e);
        }
    }

    /**
     * Generate SHA-256 hash with random salt (returns hash:salt)
     */
    public static String hashPassword(String password) {
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("Password must not be null");
        }

        byte[] salt = generateRandomBytes(SALT_LENGTH);
        String saltHex = bytesToHex(salt);
        String hash = sha256(password + saltHex);
        return hash + ":" + saltHex;
    }

    /**
     * Verify password against hash:salt
     */
    public static boolean verifyPassword(String password, String hashWithSalt) {
        if (StringUtils.isBlank(password) || StringUtils.isBlank(hashWithSalt)) {
            return false;
        }

        try {
            String[] parts = hashWithSalt.split(":");
            if (parts.length != 2) {
                return false;
            }

            String storedHash = parts[0];
            String salt = parts[1];
            String computedHash = sha256(password + salt);

            return MessageDigest.isEqual(
                    storedHash.getBytes(StandardCharsets.UTF_8),
                    computedHash.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Password verification failed", e);
            return false;
        }
    }

    // ========== HMAC ==========

    /**
     * Generate HMAC-SHA256
     */
    public static String hmacSha256(String data, String secret) {
        return hmac(data, secret, HMAC_SHA256_ALGORITHM);
    }

    /**
     * Generate HMAC-SHA512
     */
    public static String hmacSha512(String data, String secret) {
        return hmac(data, secret, HMAC_SHA512_ALGORITHM);
    }

    /**
     * Generate HMAC using specified algorithm
     */
    private static String hmac(String data, String secret, String algorithm) {
        if (StringUtils.isBlank(data) || StringUtils.isBlank(secret)) {
            throw new IllegalArgumentException("Data and Secret must not be null");
        }

        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    algorithm
            );
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (Exception e) {
            log.error("HMAC generation failed with algorithm: {}", algorithm, e);
            throw new EncryptionException("HMAC generation failed", e);
        }
    }

    /**
     * Verify HMAC
     */
    public static boolean verifyHmac(String data, String secret, String expectedHmac, String algorithm) {
        try {
            String computedHmac = hmac(data, secret, algorithm);
            return MessageDigest.isEqual(
                    expectedHmac.getBytes(StandardCharsets.UTF_8),
                    computedHmac.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("HMAC verification failed", e);
            return false;
        }
    }

    // ========== Digital Signatures ==========

    /**
     * Sign data using RSA private key
     */
    public static String sign(String data, PrivateKey privateKey) {
        if (StringUtils.isBlank(data) || privateKey == null) {
            throw new IllegalArgumentException("Data and PrivateKey must not be null");
        }

        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("Signing failed", e);
            throw new EncryptionException("Signing failed", e);
        }
    }

    /**
     * Verify signature using RSA public key
     */
    public static boolean verifySignature(String data, String signatureStr, PublicKey publicKey) {
        if (StringUtils.isBlank(data) || StringUtils.isBlank(signatureStr) || publicKey == null) {
            return false;
        }

        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    // ========== Utility Methods ==========

    /**
     * Generate random bytes
     */
    public static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    /**
     * Generate random salt
     */
    public static String generateSalt() {
        return bytesToHex(generateRandomBytes(SALT_LENGTH));
    }

    /**
     * Generate random salt with specified length
     */
    public static String generateSalt(int length) {
        return bytesToHex(generateRandomBytes(length));
    }

    /**
     * Convert bytes to hex string
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Convert hex string to bytes
     */
    public static byte[] hexToBytes(String hex) {
        if (StringUtils.isBlank(hex)) {
            return null;
        }

        int len = hex.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }

    /**
     * Encode bytes to Base64
     */
    public static String encodeBase64(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Decode Base64 to bytes
     */
    public static byte[] decodeBase64(String base64) {
        if (StringUtils.isBlank(base64)) {
            return null;
        }
        return Base64.getDecoder().decode(base64);
    }

    /**
     * Generate secure random token
     */
    public static String generateToken(int length) {
        byte[] randomBytes = generateRandomBytes(length);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Generate UUID-based token
     */
    public static String generateUuidToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    // ========== Password Strength Validation ==========

    /**
     * Check password strength
     */
    public static PasswordStrength checkPasswordStrength(String password) {
        if (StringUtils.isBlank(password)) {
            return PasswordStrength.VERY_WEAK;
        }

        int score = 0;
        int length = password.length();

        // Length score
        if (length >= 8) score++;
        if (length >= 12) score++;
        if (length >= 16) score++;

        // Character variety
        if (password.matches(".*[a-z].*")) score++;      // lowercase
        if (password.matches(".*[A-Z].*")) score++;      // uppercase
        if (password.matches(".*[0-9].*")) score++;      // numbers
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++; // special chars

        // Determine strength
        if (score <= 2) return PasswordStrength.VERY_WEAK;
        if (score <= 4) return PasswordStrength.WEAK;
        if (score <= 6) return PasswordStrength.MEDIUM;
        if (score <= 8) return PasswordStrength.STRONG;
        return PasswordStrength.VERY_STRONG;
    }

    /**
     * Password strength enum
     */
    public enum PasswordStrength {
        VERY_WEAK(0),
        WEAK(1),
        MEDIUM(2),
        STRONG(3),
        VERY_STRONG(4);

        private final int level;

        PasswordStrength(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        public boolean isSufficient() {
            return level >= MEDIUM.level;
        }
    }

    // ========== Custom Exception ==========

    /**
     * Custom encryption exception
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message) {
            super(message);
        }

        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}