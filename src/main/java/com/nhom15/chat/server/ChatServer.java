package com.nhom15.chat.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    public static final int PORT = 5000;

    // Tên người dùng -> handler đang kết nối. ConcurrentHashMap an toàn khi nhiều luồng dùng chung.
    private final ConcurrentHashMap<String, ClientHandler> online = new ConcurrentHashMap<>();
    // Thread pool giới hạn 50 luồng, thay vì new Thread() tùy tiện.
    private final ExecutorService pool = Executors.newFixedThreadPool(50);

    public void start() throws Exception {
        FileRelayServer.start(online);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Lắng nghe cổng " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[SERVER] Kết nối mới: " + socket.getRemoteSocketAddress());
                pool.submit(new ClientHandler(socket, online));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new ChatServer().start();
    }
}
