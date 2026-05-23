package com.catering.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AesUtil {

    @Value("${aes.secret}")
    private String secret;

    private SecretKeySpec keySpec() {
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        byte[] k = new byte[16];
        System.arraycopy(key, 0, k, 0, Math.min(key.length, 16));
        return new SecretKeySpec(k, "AES");
    }

    public String encrypt(String plain) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec());
            return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("AES encrypt failed", e);
        }
    }

    public String decrypt(String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec());
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decrypt failed", e);
        }
    }

    public String encryptUserId(Long userId) {
        if (userId == null) return "";
        return encrypt(String.valueOf(userId));
    }

    /** 从评价表 user_id 字段解析真实用户 ID（兼容 enc_数字 与 AES） */
    public Long decryptUserId(String stored) {
        if (stored == null || stored.isEmpty()) return null;
        if (stored.startsWith("enc_")) {
            try {
                return Long.parseLong(stored.substring(4));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        try {
            return Long.parseLong(decrypt(stored));
        } catch (Exception e) {
            return null;
        }
    }

    /** 兼容 seed 数据中的 enc_1 格式 */
    public String legacyEncryptUserId(Long userId) {
        return "enc_" + userId;
    }
}
