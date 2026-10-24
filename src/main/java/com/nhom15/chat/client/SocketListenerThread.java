package com.nhom15.chat.client;

import com.nhom15.chat.common.Message;
import java.io.IOException;
import javax.swing.SwingUtilities;

public class SocketListenerThread extends Thread {
    public interface Listener {
        void onMessage(Message m);
        void onDisconnected();
    }

    private final ChatClient client;
    private final Listener listener;

    public SocketListenerThread(ChatClient client, Listener listener) {
        this.client = client;
        this.listener = listener;
        setDaemon(true);
        setName("SocketListener");
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Message m = client.receive();
                SwingUtilities.invokeLater(() -> listener.onMessage(m));
            }
        } catch (IOException e) {
            // mất kết nối hoặc đã đóng socket
        } finally {
            SwingUtilities.invokeLater(listener::onDisconnected);
        }
    }
}
