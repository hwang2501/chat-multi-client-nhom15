package com.nhom15.chat.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;

/** Đọc/ghi gói tin có tiền tố 4 byte độ dài, tránh dính/cắt gói TCP. Có thể mã hóa AES-GCM. */
public final class FramedIO {
    private static final int MAX_LEN = 1_000_000;

    private FramedIO() {}

    /** Gửi không mã hóa (dùng cho bước trao đổi khóa và cổng file). */
    public static void send(DataOutputStream out, Message m) throws IOException {
        send(out, m, null);
    }

    /** key == null: gửi thường; có key: mã hóa AES-256-GCM trước khi gửi. */
    public static void send(DataOutputStream out, Message m, SecretKey key) throws IOException {
        byte[] data = m.toJson().getBytes(StandardCharsets.UTF_8);
        if (key != null) {
            try {
                data = CryptoUtil.aesEncrypt(key, data);
            } catch (GeneralSecurityException e) {
                throw new IOException("Không mã hóa được gói tin", e);
            }
        }
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    public static Message receive(DataInputStream in) throws IOException {
        return receive(in, null);
    }

    public static Message receive(DataInputStream in, SecretKey key) throws IOException {
        int len = in.readInt();
        if (len <= 0 || len > MAX_LEN) {
            throw new IOException("Gói tin không hợp lệ: " + len);
        }
        byte[] data = new byte[len];
        in.readFully(data);
        if (key != null) {
            try {
                data = CryptoUtil.aesDecrypt(key, data);
            } catch (GeneralSecurityException e) {
                throw new IOException("Giải mã thất bại (gói bị sửa hoặc sai khóa)", e);
            }
        }
        return Message.fromJson(new String(data, StandardCharsets.UTF_8));
    }
}
