package com.nhom15.chat.client.ui;

import javax.swing.*;
import java.awt.*;

/** Cửa sổ đăng nhập: nhập tên người dùng, IP và cổng của server. */
public class LoginFrame extends JFrame {
    private final JTextField txtUser = new JTextField(15);
    private final JTextField txtHost = new JTextField("localhost", 15);
    private final JTextField txtPort = new JTextField("5000", 15);
    private final JButton btnLogin = new JButton("Đăng nhập");
    private final JLabel lblStatus = new JLabel(" ");

    public LoginFrame() {
        super("Chat - Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Tên người dùng:"));
        form.add(txtUser);
        form.add(new JLabel("IP Server:"));
        form.add(txtHost);
        form.add(new JLabel("Cổng:"));
        form.add(txtPort);

        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.add(btnLogin, BorderLayout.CENTER);
        bottom.add(lblStatus, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.add(form, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(btnLogin);
        pack();
        setLocationRelativeTo(null);

        btnLogin.addActionListener(e -> onLogin());
    }

    private void onLogin() {
        String user = txtUser.getText().trim();
        String host = txtHost.getText().trim();
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException ex) {
            lblStatus.setText("Cổng phải là số");
            return;
        }
        if (user.isEmpty() || host.isEmpty()) {
            lblStatus.setText("Vui lòng nhập đủ tên và IP");
            return;
        }
        // Kết nối thật tới server sẽ làm ở Issue #5 (ChatFrame + SocketListenerThread).
        lblStatus.setText("Sẵn sàng kết nối " + host + ":" + port + " với tên " + user);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
