package com.nhom15.chat.client.ui;

import com.nhom15.chat.client.ChatClient;
import com.nhom15.chat.client.SocketListenerThread;
import com.nhom15.chat.common.Message;
import java.awt.*;
import java.io.IOException;
import javax.swing.*;

public class ChatFrame extends JFrame implements SocketListenerThread.Listener {
    private static final String ALL = "Tất cả (chat nhóm)";

    private final ChatClient client;
    private final JTextArea txtChat = new JTextArea();
    private final DefaultListModel<String> userModel = new DefaultListModel<>();
    private final JList<String> lstUsers = new JList<>(userModel);
    private final JTextField txtInput = new JTextField();
    private final JButton btnSend = new JButton("Gửi");

    public ChatFrame(ChatClient client) {
        this.client = client;
        setTitle("Chat - " + client.getUsername());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(720, 480);
        setLocationRelativeTo(null);

        txtChat.setEditable(false);
        txtChat.setLineWrap(true);
        txtChat.setWrapStyleWord(true);
        userModel.addElement(ALL);
        lstUsers.setSelectedIndex(0);
        lstUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane right = new JScrollPane(lstUsers);
        right.setPreferredSize(new Dimension(180, 0));
        right.setBorder(BorderFactory.createTitledBorder("Đang online"));

        JPanel bottom = new JPanel(new BorderLayout(5, 0));
        bottom.add(txtInput, BorderLayout.CENTER);
        bottom.add(btnSend, BorderLayout.EAST);

        setLayout(new BorderLayout(5, 5));
        add(new JScrollPane(txtChat), BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        btnSend.addActionListener(e -> sendMessage());
        txtInput.addActionListener(e -> sendMessage());
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { logoutAndExit(); }
        });

        new SocketListenerThread(client, this).start();
    }

    private void sendMessage() {
        String text = txtInput.getText().trim();
        if (text.isEmpty()) return;
        String target = lstUsers.getSelectedValue();
        try {
            if (target == null || ALL.equals(target)) {
                client.send(new Message("MSG_GROUP", client.getUsername(), "", text));
            } else {
                client.send(new Message("MSG_PRIVATE", client.getUsername(), target, text));
            }
            txtInput.setText("");
        } catch (IOException ex) {
            append("[Lỗi] Không gửi được: " + ex.getMessage());
        }
    }

    @Override
    public void onMessage(Message m) {
        String me = client.getUsername();
        switch (m.action) {
            case "MSG_GROUP":
                append(m.from + ": " + m.content);
                break;
            case "MSG_PRIVATE":
                if (me.equals(m.from)) append("Tôi → " + m.to + " (riêng): " + m.content);
                else append(m.from + " (riêng): " + m.content);
                break;
            case "USER_LIST":
                updateUsers(m.content);
                break;
            case "ERROR":
                append("[Lỗi] " + m.content);
                break;
            default:
                break;
        }
    }

    private void updateUsers(String content) {
        String selected = lstUsers.getSelectedValue();
        userModel.clear();
        userModel.addElement(ALL);
        if (content != null && !content.isEmpty()) {
            for (String u : content.split(",")) {
                if (!u.equals(client.getUsername())) userModel.addElement(u);
            }
        }
        int idx = userModel.indexOf(selected);
        lstUsers.setSelectedIndex(idx >= 0 ? idx : 0);
    }

    private void append(String line) {
        txtChat.append(line + "\n");
        txtChat.setCaretPosition(txtChat.getDocument().getLength());
    }

    @Override
    public void onDisconnected() {
        append("[Hệ thống] Mất kết nối tới Server.");
        txtInput.setEnabled(false);
        btnSend.setEnabled(false);
    }

    private void logoutAndExit() {
        try { client.send(new Message("LOGOUT", client.getUsername(), "", "")); } catch (IOException ignored) { }
        client.close();
        dispose();
        System.exit(0);
    }
}
