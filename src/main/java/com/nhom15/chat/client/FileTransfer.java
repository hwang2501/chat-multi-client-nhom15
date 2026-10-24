package com.nhom15.chat.client;

import com.nhom15.chat.common.FramedIO;
import com.nhom15.chat.common.Message;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Gửi/nhận byte[] của file qua cổng 5001 (Server chuyển tiếp từng khối 8 KB). */
public final class FileTransfer {
    public static final int FILE_PORT = 5001;
    private static final int BLOCK = 8192;

    public interface Progress {
        void onProgress(long done, long total);
    }

    private FileTransfer() {}

    private static Socket open(String host) throws IOException {
        Socket s = new Socket();
        s.connect(new InetSocketAddress(host, FILE_PORT), 5000);
        return s;
    }

    /** Người gửi: gửi khung SEND rồi đẩy toàn bộ byte của file. */
    public static void send(String host, String user, String transferId, File file, Progress p) throws IOException {
        try (Socket s = open(host)) {
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            FramedIO.send(out, new Message("SEND", user, "", transferId));
            long total = file.length();
            long done = 0;
            byte[] buf = new byte[BLOCK];
            try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
                int n;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                    done += n;
                    p.onProgress(done, total);
                }
            }
            out.flush();
        }
    }

    /** Người nhận: gửi khung RECV rồi đọc đúng size byte, ghi vào file đích. */
    public static void receive(String host, String user, String transferId, File dest, long size, Progress p) throws IOException {
        try (Socket s = open(host)) {
            s.setSoTimeout(60000);
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            FramedIO.send(out, new Message("RECV", user, "", transferId));
            long done = 0;
            byte[] buf = new byte[BLOCK];
            try (OutputStream fo = new BufferedOutputStream(new FileOutputStream(dest))) {
                while (done < size) {
                    int n = in.read(buf, 0, (int) Math.min(buf.length, size - done));
                    if (n < 0) throw new EOFException("Kết nối bị ngắt khi nhận file");
                    fo.write(buf, 0, n);
                    done += n;
                    p.onProgress(done, size);
                }
            }
        }
    }
}
