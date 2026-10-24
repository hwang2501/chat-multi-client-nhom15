package com.nhom15.chat.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nhom15.chat.common.FramedIO;
import com.nhom15.chat.common.Message;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

/** Test thủ công: A gửi file 5 MB cho B qua cổng 5001, so sánh SHA-256. Cần ChatServer đang chạy. */
public class FileRelayManualTest {
    static Message waitFor(DataInputStream in, String action) throws IOException {
        while (true) {
            Message m = FramedIO.receive(in);
            if (action.equals(m.action)) return m;
        }
    }

    public static void main(String[] args) throws Exception {
        byte[] data = new byte[5_000_000];
        new Random(1).nextBytes(data);
        byte[] expected = MessageDigest.getInstance("SHA-256").digest(data);

        try (Socket sa = new Socket("localhost", 5000); Socket sb = new Socket("localhost", 5000)) {
            DataOutputStream oa = new DataOutputStream(sa.getOutputStream());
            DataInputStream ia = new DataInputStream(sa.getInputStream());
            DataOutputStream ob = new DataOutputStream(sb.getOutputStream());
            DataInputStream ib = new DataInputStream(sb.getInputStream());

            FramedIO.send(oa, new Message("LOGIN", "A", "", ""));
            waitFor(ia, "LOGIN_OK");
            FramedIO.send(ob, new Message("LOGIN", "B", "", ""));
            waitFor(ib, "LOGIN_OK");

            JsonObject offer = new JsonObject();
            offer.addProperty("fileName", "test.bin");
            offer.addProperty("size", data.length);
            FramedIO.send(oa, new Message("FILE_OFFER", "A", "B", offer.toString()));

            Message got = waitFor(ib, "FILE_OFFER");
            String id = JsonParser.parseString(got.content).getAsJsonObject().get("transferId").getAsString();
            FramedIO.send(ob, new Message("FILE_ACCEPT", "B", "A", id));
            waitFor(ia, "FILE_ACCEPT");

            ExecutorService ex = Executors.newSingleThreadExecutor();
            Future<byte[]> recv = ex.submit(() -> {
                try (Socket r = new Socket("localhost", 5001)) {
                    FramedIO.send(new DataOutputStream(r.getOutputStream()), new Message("RECV", "B", "", id));
                    byte[] buf = new byte[data.length];
                    new DataInputStream(r.getInputStream()).readFully(buf);
                    return MessageDigest.getInstance("SHA-256").digest(buf);
                }
            });

            try (Socket s = new Socket("localhost", 5001)) {
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                FramedIO.send(out, new Message("SEND", "A", "", id));
                for (int off = 0; off < data.length; off += 8192) {
                    out.write(data, off, Math.min(8192, data.length - off));
                }
                out.flush();
                System.out.println("A: FILE_DONE = " + waitFor(ia, "FILE_DONE").content);
            }
            System.out.println("B: FILE_DONE = " + waitFor(ib, "FILE_DONE").content);
            boolean same = Arrays.equals(expected, recv.get(30, TimeUnit.SECONDS));
            System.out.println(same ? "KET QUA: DAT - file nhan giong file goc" : "KET QUA: SAI");
            ex.shutdown();
        }
    }
}
