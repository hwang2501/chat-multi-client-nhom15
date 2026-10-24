package com.nhom15.chat.common;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

/**
 * Các hàm mã hóa dùng chung: RSA-2048 (OAEP SHA-256) để trao đổi khóa,
 * AES-256-GCM để mã hóa từng gói tin. Không có khóa nào được viết cứng trong code.
 */
public final class CryptoUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int IV_LEN = 12;      // 96 bit, chuẩn cho GCM
    private static final int TAG_BITS = 128;   // thẻ xác thực GCM

    private CryptoUtil() {}

    // ---------- RSA ----------

    public static KeyPair generateRsa() {
        try {
            KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
            g.initialize(2048);
            return g.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static OAEPParameterSpec oaep() {
        return new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
    }

    public static byte[] rsaEncrypt(PublicKey pub, byte[] data) throws GeneralSecurityException {
        Cipher c = Cipher.getInstance("RSA/ECB/OAEPPadding");
        c.init(Cipher.ENCRYPT_MODE, pub, oaep());
        return c.doFinal(data);
    }

    public static byte[] rsaDecrypt(PrivateKey priv, byte[] data) throws GeneralSecurityException {
        Cipher c = Cipher.getInstance("RSA/ECB/OAEPPadding");
        c.init(Cipher.DECRYPT_MODE, priv, oaep());
        return c.doFinal(data);
    }

    public static String publicKeyToBase64(PublicKey k) {
        return Base64.getEncoder().encodeToString(k.getEncoded());
    }

    public static PublicKey publicKeyFromBase64(String b64) throws GeneralSecurityException {
        byte[] der = Base64.getDecoder().decode(b64);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
    }

    // ---------- AES-GCM ----------

    public static SecretKey newAesKey() {
        try {
            KeyGenerator g = KeyGenerator.getInstance("AES");
            g.init(256);
            return g.generateKey();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public static SecretKey aesKeyFromBytes(byte[] raw) {
        if (raw.length != 32) throw new IllegalArgumentException("Khóa AES phải dài 32 byte");
        return new SecretKeySpec(raw, "AES");
    }

    /** Kết quả = IV (12 byte ngẫu nhiên, mỗi gói một IV mới) + bản mã + thẻ xác thực. */
    public static byte[] aesEncrypt(SecretKey key, byte[] plain) throws GeneralSecurityException {
        byte[] iv = new byte[IV_LEN];
        RANDOM.nextBytes(iv);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        byte[] ct = c.doFinal(plain);
        byte[] out = new byte[IV_LEN + ct.length];
        System.arraycopy(iv, 0, out, 0, IV_LEN);
        System.arraycopy(ct, 0, out, IV_LEN, ct.length);
        return out;
    }

    /** Ném AEADBadTagException nếu dữ liệu bị sửa hoặc sai khóa. */
    public static byte[] aesDecrypt(SecretKey key, byte[] data) throws GeneralSecurityException {
        if (data.length < IV_LEN + 16) throw new GeneralSecurityException("Gói mã hóa quá ngắn");
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, data, 0, IV_LEN));
        return c.doFinal(data, IV_LEN, data.length - IV_LEN);
    }
}
