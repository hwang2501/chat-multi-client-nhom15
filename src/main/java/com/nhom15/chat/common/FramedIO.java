package com.nhom15.chat.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Đọc/ghi gói tin có tiền tố 4 byte độ dài, tránh dính/cắt gói TCP. */
public final class FramedIO {
    private static final int MAX_LEN = 1_000_000;

    private FramedIO() {}

    public static void send(DataOutputStream out, Message m) throws IOException {
        byte[] data = m.toJson().getBytes(StandardCharsets.UTF_8);
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    public static Message receive(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len <= 0 || len > MAX_LEN) {
            throw new IOException("Gói tin không hợp lệ: " + len);
        }
        byte[] data = new byte[len];
        in.readFully(data);
        return Message.fromJson(new String(data, StandardCharsets.UTF_8));
    }
}
