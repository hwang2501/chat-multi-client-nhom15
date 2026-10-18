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
                if (!handle(m)) break;
            }
        } catch (IOException e) {
            System.out.println("[SERVER] Mất kết nối: " + username);
        } finally {
            disconnect();
        }
    }

    /** Trả về false khi client muốn thoát. */
    private boolean handle(Message m) throws IOException {
        if (m.action == null) return true;
        switch (m.action) {
            case "LOGIN":
                login(m);
                return true;
            case "LOGOUT":
                return false;
            default:
                // MSG_PRIVATE, MSG_GROUP: làm ở Issue #6.
                return true;
        }
    }

    private void login(Message m) throws IOException {
        String name = m.from == null ? "" : m.from.trim();
        if (username != null) {
            send(new Message("LOGIN_FAIL", "server", name, "Bạn đã đăng nhập rồi"));
            return;
        }
        if (name.isEmpty() || name.length() > 20 || name.contains(",")) {
            send(new Message("LOGIN_FAIL", "server", name, "Tên không hợp lệ (1-20 ký tự, không chứa dấu phẩy)"));
            return;
        }
        // putIfAbsent là thao tác nguyên tử: hai người trùng tên cùng lúc thì chỉ một người thành công.
        if (online.putIfAbsent(name, this) != null) {
            send(new Message("LOGIN_FAIL", "server", name, "Tên đã được sử dụng"));
            return;
        }
        username = name;
        System.out.println("[SERVER] Đăng nhập: " + username + " | Online: " + online.size());
        send(new Message("LOGIN_OK", "server", username, "Đăng nhập thành công"));
        broadcastUserList();
    }

    private void disconnect() {
        if (username != null) {
            online.remove(username);
            System.out.println("[SERVER] Thoát: " + username + " | Online: " + online.size());
            username = null;
            broadcastUserList();
        }
        try { socket.close(); } catch (IOException ignored) {}
    }

    /** Gửi danh sách người đang online (cách nhau bằng dấu phẩy) cho mọi client. */
    private void broadcastUserList() {
        String list = String.join(",", online.keySet());
        for (ClientHandler h : online.values()) {
            try {
                h.send(new Message("USER_LIST", "server", null, list));
            } catch (IOException e) {
                System.out.println("[SERVER] Không gửi được cho một client, bỏ qua");
            }
        }
    }
}
