# Hệ thống Chat Đa Client (Desktop) - Nhóm 15, Lớp 22DTHA3

Chat Client-Server bằng Java Socket TCP, giao thức JSON tùy biến, truyền file bằng
byte[] qua socket riêng, mã hóa AES.

## Thành viên và bảng đóng góp

| MSSV | Họ và Tên | Công việc đã thực hiện | Merge Requests |
|---|---|---|---|
| 2280601029 | Huỳnh Thanh Hoàng | Server đa luồng, định tuyến, quản lý online | |
| 2280601574 | Trần Đình Khôi | Giao diện Swing Client | |
| 2280600515 | Lê Toàn Dương | Framing, mã hóa, truyền file | |

## Cách chạy
(Cập nhật sau)

## Console Test Client

Dung de test Server khong can giao dien.

    java -cp "target/classes;%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar" com.nhom15.chat.consoleclient.ConsoleClient <host> <port> <ten>

Lenh: /all <nd>, /to <ten> <nd>, /raw <JSON>, /oversize (thu goi > 1 MB), /quit.
