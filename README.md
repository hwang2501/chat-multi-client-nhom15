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

## Kết quả kiểm thử

| Kịch bản | Cách chạy | Kết quả |
|---|---|---|
| 10 client đăng nhập cùng lúc, mỗi client gửi 1 tin nhóm | `bash scripts/multi-client-test.sh localhost 10` | Cả 10 client nhận đủ 10 tin. Mỗi phiên mở kênh AES-256-GCM riêng. Đạt |
| Gửi file 200 MB (209.715.200 byte) giữa hai client | Giao diện chat, cổng 5001 | Cả hai bên báo trạng thái OK, kích thước file nhận bằng file gốc. Đạt |
| Gói tin vượt giới hạn 1 MB | `/oversize` trong Console Client | Server ngắt kết nối, không bị sập. Đạt |
| Hai máy qua mạng Tailscale | Server ở máy nhóm trưởng, Client nhập IP 100.x.x.x | Khôi và Dương đăng nhập từ hai máy khác qua IP Tailscale 100.120.1.21; chat nhóm, chat riêng và gửi file 139 MB (145.716.664 byte) đều chạy tốt, mỗi kết nối dùng kênh AES-256-GCM riêng. Đạt |

Server vận hành bằng thread pool 50 luồng cho chat và 20 luồng cho truyền file. Khi truyền file, Server chỉ giữ 8 KB mỗi lần nên file 200 MB không làm đầy bộ nhớ.
