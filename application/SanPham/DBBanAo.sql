CREATE DATABASE BanAoKhoacNam;
GO
USE BanAoKhoacNam;
GO


CREATE TABLE VaiTro (
    id INT IDENTITY PRIMARY KEY,
    maVaiTro VARCHAR(20),
    tenVaiTro NVARCHAR(50),
    trangThai INT
);

INSERT INTO VaiTro VALUES
('ADMIN',N'Quản trị',1),('NV',N'Nhân viên',1),('KH',N'Khách hàng',1),
('SHIP',N'Giao hàng',1),('QL',N'Quản lý',1),('KT',N'Kế toán',1),
('CSKH',N'CSKH',1),('CTV',N'Cộng tác viên',1),('TEST',N'Test',0),('LOCK',N'Khoá',0);

CREATE TABLE TaiKhoan (
    id INT IDENTITY PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(100),
    idVaiTro INT,
    trangThai INT,
    FOREIGN KEY (idVaiTro) REFERENCES VaiTro(id)
);

INSERT INTO TaiKhoan VALUES
('admin','123',1,1),('nv01','123',2,1),('nv02','123',2,1),
('kh01','123',3,1),('kh02','123',3,1),('kh03','123',3,1),
('test01','123',9,0),('lock01','123',10,0),
('ship01','123',4,1),('ql01','123',5,1);

/* =========================
   NHÂN VIÊN – KHÁCH HÀNG
========================= */
CREATE TABLE NhanVien (
    id INT IDENTITY PRIMARY KEY,
    idTaiKhoan INT,
    maNhanVien VARCHAR(20),
    tenNhanVien NVARCHAR(100),
    sdt VARCHAR(15),
    email VARCHAR(100),
    diaChi NVARCHAR(255),
    trangThai INT,
    FOREIGN KEY (idTaiKhoan) REFERENCES TaiKhoan(id)
);

INSERT INTO NhanVien VALUES
(2,'NV01',N'Nguyễn Văn A','0901','a@gmail.com',N'HN',1),
(3,'NV02',N'Trần Văn B','0902','b@gmail.com',N'HCM',1),
(10,'QL01',N'Lê Văn C','0903','c@gmail.com',N'HN',1),
(NULL,'NV04',N'Phạm Văn D','0904','d@gmail.com',N'HCM',1),
(NULL,'NV05',N'Hoàng Văn E','0905','e@gmail.com',N'ĐN',1),
(NULL,'NV06',N'Vũ Văn F','0906','f@gmail.com',N'HP',1),
(NULL,'NV07',N'Đỗ Văn G','0907','g@gmail.com',N'CT',1),
(NULL,'NV08',N'Ngô Văn H','0908','h@gmail.com',N'HN',1),
(NULL,'NV09',N'Bùi Văn I','0909','i@gmail.com',N'HN',0),
(NULL,'NV10',N'Đinh Văn K','0910','k@gmail.com',N'HN',0);

CREATE TABLE KhachHang (
    id INT IDENTITY PRIMARY KEY,
    idTaiKhoan INT,
    maKhachHang VARCHAR(20),
    tenKhachHang NVARCHAR(100),
    sdt VARCHAR(15),
    email VARCHAR(100),
    gioiTinh BIT,
    ngayTao DATETIME,
    trangThai INT,
    FOREIGN KEY (idTaiKhoan) REFERENCES TaiKhoan(id)
);

INSERT INTO KhachHang VALUES
(4,'KH01',N'Nguyễn An','0911','an@gmail.com',1,GETDATE(),1),
(5,'KH02',N'Trần Bình','0912','binh@gmail.com',1,GETDATE(),1),
(6,'KH03',N'Lê Chi','0913','chi@gmail.com',0,GETDATE(),1),
(NULL,'KH04',N'Khách lẻ 1','0914',NULL,1,GETDATE(),1),
(NULL,'KH05',N'Khách lẻ 2','0915',NULL,1,GETDATE(),1),
(NULL,'KH06',N'Khách lẻ 3','0916',NULL,1,GETDATE(),1),
(NULL,'KH07',N'Khách lẻ 4','0917',NULL,1,GETDATE(),1),
(NULL,'KH08',N'Khách lẻ 5','0918',NULL,1,GETDATE(),1),
(NULL,'KH09',N'Khách lẻ 6','0919',NULL,1,GETDATE(),0),
(NULL,'KH10',N'Khách lẻ 7','0920',NULL,1,GETDATE(),0);

/* =========================
   ĐỊA CHỈ
========================= */
CREATE TABLE DiaChi (
    id INT IDENTITY PRIMARY KEY,
    idKhachHang INT,
    diaChi NVARCHAR(255),
    trangThai INT,
    FOREIGN KEY (idKhachHang) REFERENCES KhachHang(id)
);

INSERT INTO DiaChi VALUES
(1,N'Hà Nội',1),(2,N'HCM',1),(3,N'Đà Nẵng',1),(4,N'Hà Nội',1),
(5,N'HCM',1),(6,N'Hải Phòng',1),(7,N'Cần Thơ',1),(8,N'HN',1),
(9,N'HN',0),(10,N'HCM',0);

/* =========================
   SẢN PHẨM – DANH MỤC
========================= */
CREATE TABLE ThuongHieu (
    id INT IDENTITY PRIMARY KEY,
    tenThuongHieu NVARCHAR(100),
    trangThai INT
);
INSERT INTO ThuongHieu VALUES
(N'Nike',1),(N'Adidas',1),(N'Puma',1),(N'Zara',1),(N'Uniqlo',1),
(N'H&M',1),(N'Local Brand',1),(N'NoBrand',1),(N'Test',0),(N'Old',0);

CREATE TABLE ChatLieu (
    id INT IDENTITY PRIMARY KEY,
    tenChatLieu NVARCHAR(50),
    trangThai INT
);
INSERT INTO ChatLieu VALUES
(N'Da',1),(N'Vải',1),(N'Jean',1),(N'Kaki',1),(N'Nỉ',1),
(N'Poly',1),(N'Cotton',1),(N'Len',1),(N'Test',0),(N'Old',0);

CREATE TABLE MauSac (
    id INT IDENTITY PRIMARY KEY,
    tenMau NVARCHAR(50),
    trangThai INT
);
INSERT INTO MauSac VALUES
(N'Đen',1),(N'Trắng',1),(N'Xanh',1),(N'Đỏ',1),(N'Vàng',1),
(N'Nâu',1),(N'Xám',1),(N'Cam',1),(N'Test',0),(N'Old',0);

CREATE TABLE Size (
    id INT IDENTITY PRIMARY KEY,
    tenSize VARCHAR(10),
    trangThai INT
);
INSERT INTO Size VALUES
('S',1),('M',1),('L',1),('XL',1),('XXL',1),
('XS',1),('3XL',1),('4XL',1),('Test',0),('Old',0);

/* =========================
   SẢN PHẨM – CHI TIẾT – ẢNH
========================= */
CREATE TABLE SanPham (
    id INT IDENTITY PRIMARY KEY,
    maSanPham VARCHAR(20),
    tenSanPham NVARCHAR(150),
    moTa NVARCHAR(255),
    idThuongHieu INT,
    trangThai INT,
    FOREIGN KEY (idThuongHieu) REFERENCES ThuongHieu(id)
);

INSERT INTO SanPham VALUES
('SP01',N'Áo khoác da',N'Mẫu hot',1,1),
('SP02',N'Áo khoác jean',N'Bền đẹp',2,1),
('SP03',N'Áo khoác nỉ',N'Mùa đông',3,1),
('SP04',N'Áo khoác kaki',N'Đi làm',4,1),
('SP05',N'Áo khoác gió',N'Chống nước',5,1),
('SP06',N'Áo khoác dù',N'Nhẹ',6,1),
('SP07',N'Áo khoác len',N'Ấm',7,1),
('SP08',N'Áo khoác bomber',N'Fashion',8,1),
('SP09',N'Test',N'Test',9,0),
('SP10',N'Old',N'Old',10,0);

CREATE TABLE SanPhamChiTiet (
    id INT IDENTITY PRIMARY KEY,
    idSanPham INT,
    idMauSac INT,
    idSize INT,
    idChatLieu INT,
    donGia DECIMAL(18,2),
    soLuong INT,
    trangThai INT,
    FOREIGN KEY (idSanPham) REFERENCES SanPham(id),
    FOREIGN KEY (idMauSac) REFERENCES MauSac(id),
    FOREIGN KEY (idSize) REFERENCES Size(id),
    FOREIGN KEY (idChatLieu) REFERENCES ChatLieu(id)
);

INSERT INTO SanPhamChiTiet VALUES
(1,1,2,1,1200000,10,1),
(2,2,3,2,900000,10,1),
(3,3,4,3,700000,10,1),
(4,4,5,4,800000,10,1),
(5,5,6,5,600000,10,1),
(6,6,7,6,500000,10,1),
(7,7,8,7,1000000,10,1),
(8,8,1,1,1100000,10,1),
(9,1,1,1,0,0,0),
(10,2,2,2,0,0,0);

CREATE TABLE HinhAnh (
    id INT IDENTITY PRIMARY KEY,
    idSanPham INT,
    hinhAnh NVARCHAR(255),
    trangThai INT,
    FOREIGN KEY (idSanPham) REFERENCES SanPham(id)
);

INSERT INTO HinhAnh VALUES
(1,'sp01.jpg',1),(2,'sp02.jpg',1),(3,'sp03.jpg',1),(4,'sp04.jpg',1),
(5,'sp05.jpg',1),(6,'sp06.jpg',1),(7,'sp07.jpg',1),(8,'sp08.jpg',1),
(9,'test.jpg',0),(10,'old.jpg',0);

/* =========================
   GIỎ HÀNG – HOÁ ĐƠN – HỖ TRỢ
========================= */
CREATE TABLE GioHang (
    id INT IDENTITY PRIMARY KEY,
    idKhachHang INT,
    ngayTao DATETIME,
    trangThai INT,
    FOREIGN KEY (idKhachHang) REFERENCES KhachHang(id)
);

CREATE TABLE GioHangChiTiet (
    id INT IDENTITY PRIMARY KEY,
    idGioHang INT,
    idSanPhamChiTiet INT,
    soLuong INT,
    FOREIGN KEY (idGioHang) REFERENCES GioHang(id),
    FOREIGN KEY (idSanPhamChiTiet) REFERENCES SanPhamChiTiet(id)
);

CREATE TABLE TrangThaiHoaDon (
    id INT IDENTITY PRIMARY KEY,
    tenTrangThai NVARCHAR(50)
);
INSERT INTO TrangThaiHoaDon VALUES
(N'Chờ xử lý'),(N'Đang giao'),(N'Hoàn thành'),(N'Huỷ'),(N'Trả hàng');

CREATE TABLE LoaiThanhToan (
    id INT IDENTITY PRIMARY KEY,
    tenLoai NVARCHAR(50)
);
INSERT INTO LoaiThanhToan VALUES
(N'Tiền mặt'),(N'Chuyển khoản'),(N'Ví điện tử');

CREATE TABLE MaGiamGia (
    id INT IDENTITY PRIMARY KEY,
    ma VARCHAR(20),
    giaTri DECIMAL(18,2),
    trangThai INT
);
INSERT INTO MaGiamGia VALUES
('SALE10',100000,1),('SALE20',200000,1),('TEST',0,0);

CREATE TABLE HoaDon (
    id INT IDENTITY PRIMARY KEY,
    maHoaDon VARCHAR(20),
    idNhanVien INT,
    idKhachHang INT,
    idMaGiamGia INT,
    idLoaiThanhToan INT,
    idTrangThaiHoaDon INT,
    diaChi NVARCHAR(255),
    ngayTao DATETIME,
    tongThanhToan DECIMAL(18,2),
    FOREIGN KEY (idNhanVien) REFERENCES NhanVien(id),
    FOREIGN KEY (idKhachHang) REFERENCES KhachHang(id),
    FOREIGN KEY (idMaGiamGia) REFERENCES MaGiamGia(id),
    FOREIGN KEY (idLoaiThanhToan) REFERENCES LoaiThanhToan(id),
    FOREIGN KEY (idTrangThaiHoaDon) REFERENCES TrangThaiHoaDon(id)
);

CREATE TABLE HoaDonChiTiet (
    id INT IDENTITY PRIMARY KEY,
    idHoaDon INT,
    idSanPhamChiTiet INT,
    soLuong INT,
    donGia DECIMAL(18,2),
    FOREIGN KEY (idHoaDon) REFERENCES HoaDon(id),
    FOREIGN KEY (idSanPhamChiTiet) REFERENCES SanPhamChiTiet(id)
);

CREATE TABLE HoTroKhachHang (
    id INT IDENTITY PRIMARY KEY,
    idKhachHang INT,
    noiDung NVARCHAR(255),
    thoiGian DATETIME,
    trangThai INT,
    idNhanVienTraLoi INT,
    FOREIGN KEY (idKhachHang) REFERENCES KhachHang(id),
    FOREIGN KEY (idNhanVienTraLoi) REFERENCES NhanVien(id)
);
