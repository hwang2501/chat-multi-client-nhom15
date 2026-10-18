package com.nhom15.chat.server;

import com.nhom15.chat.common.FramedIO;
import com.nhom15.chat.common.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ConcurrentHashMap<String, ClientHandler> online;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public ClientHandler(Socket socket, ConcurrentHashMap<String, ClientHandler> online) {
        this.socket = socket;
        this.online = online;
    }

    // synchronized: tránh hai luồng ghi chen nhau làm hỏng gói tin.
    public synchronized void send(Message m) throws IOException {
        FramedIO.send(out, m);
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            while (true) {
                Message m = FramedIO.receive(in);
                System.out.println("[SERVER] Nhận: " + m.toJson());
                handle(m);
            }
        } catch (IOException e) {
            System.out.println("[SERVER] Ngắt kết nối: " + username);
        } finally {
            if (username != null) {
                online.remove(username);
            }
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void handle(Message m) throws IOException {
        if ("LOGIN".equals(m.action)) {
            if (m.from == null || m.from.isBlank() || online.containsKey(m.from)) {
                send(new Message("LOGIN_FAIL", "server", m.from, "Tên không hợp lệ hoặc đã được dùng"));
                return;
            }
            username = m.from;
            online.put(username, this);
            send(new Message("LOGIN_OK", "server", username, "Đăng nhập thành công"));
        }
        // Các action khác (MSG_PRIVATE, MSG_GROUP, USER_LIST...) làm ở Issue #3 và #6.
    }
}
