package com.nhom15.chat.common;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Arrays;
import javax.crypto.SecretKey;

/** Kiểm tra thủ công CryptoUtil. Chạy main, cuối cùng phải in "KET QUA: DAT". */
public class CryptoManualTest {
    public static void main(String[] args) throws Exception {
        boolean ok = true;

        // 1. RSA: mã hóa khóa AES bằng khóa công khai, giải mã bằng khóa riêng
        KeyPair rsa = CryptoUtil.generateRsa();
        SecretKey aes = CryptoUtil.newAesKey();
        byte[] enc = CryptoUtil.rsaEncrypt(rsa.getPublic(), aes.getEncoded());
        byte[] dec = CryptoUtil.rsaDecrypt(rsa.getPrivate(), enc);
        boolean t1 = Arrays.equals(aes.getEncoded(), dec);
        System.out.println("1. RSA-2048 OAEP: giai ma ra dung khoa AES -> " + t1);
        ok &= t1;

        // 2. AES-GCM: mã hóa rồi giải mã ra đúng nội dung
        byte[] plain = "Xin chao, day la tin nhan bi mat".getBytes(StandardCharsets.UTF_8);
        byte[] c1 = CryptoUtil.aesEncrypt(aes, plain);
        boolean t2 = Arrays.equals(plain, CryptoUtil.aesDecrypt(aes, c1));
        System.out.println("2. AES-256-GCM: giai ma ra dung noi dung -> " + t2);
        ok &= t2;

        // 3. Cùng nội dung, mã hóa hai lần phải ra hai bản mã khác nhau (IV ngẫu nhiên)
        byte[] c2 = CryptoUtil.aesEncrypt(aes, plain);
        boolean t3 = !Arrays.equals(c1, c2);
        System.out.println("3. IV ngau nhien: hai ban ma khac nhau -> " + t3);
        ok &= t3;

        // 4. Sửa 1 byte của bản mã thì giải mã phải thất bại (GCM phát hiện bị sửa)
        byte[] bad = c1.clone();
        bad[bad.length - 1] ^= 1;
        boolean t4;
        try {
            CryptoUtil.aesDecrypt(aes, bad);
            t4 = false;
        } catch (GeneralSecurityException e) {
            t4 = true;
        }
        System.out.println("4. Sua 1 byte -> giai ma that bai (phat hien gia mao) -> " + t4);
        ok &= t4;

        // 5. Sai khóa thì giải mã thất bại
        boolean t5;
        try {
            CryptoUtil.aesDecrypt(CryptoUtil.newAesKey(), c1);
            t5 = false;
        } catch (GeneralSecurityException e) {
            t5 = true;
        }
        System.out.println("5. Sai khoa -> giai ma that bai -> " + t5);
        ok &= t5;

        System.out.println(ok ? "KET QUA: DAT" : "KET QUA: KHONG DAT");
    }
}
