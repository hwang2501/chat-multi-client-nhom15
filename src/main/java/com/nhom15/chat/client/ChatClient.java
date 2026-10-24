package com.nhom15.chat.client;

import com.nhom15.chat.common.FramedIO;
import com.nhom15.chat.common.Message;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public void connect(String host, int port) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 5000);
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    /** Trả về null nếu đăng nhập thành công, ngược lại trả về lý do thất bại. */
    public String login(String name) throws IOException {
        username = name;
        send(new Message("LOGIN", name, "", ""));
        while (true) {
            Message m = receive();
            if ("LOGIN_OK".equals(m.action)) return null;
            if ("LOGIN_FAIL".equals(m.action)) return m.content;
        }
    }

    public synchronized void send(Message m) throws IOException {
        FramedIO.send(out, m);
    }

    public Message receive() throws IOException {
        return FramedIO.receive(in);
    }

    public String getUsername() { return username; }

    public void close() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) { }
    }
}
