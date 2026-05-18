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

    public String encryptUserId(Long userId) {
        if (userId == null) return "";
        return encrypt(String.valueOf(userId));
    }

    /** 兼容 seed 数据中的 enc_1 格式 */
    public String legacyEncryptUserId(Long userId) {
        return "enc_" + userId;
    }
}
