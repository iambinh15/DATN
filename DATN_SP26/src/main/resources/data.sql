-- Chèn dữ liệu mẫu cho bảng VaiTro nếu chưa có
IF NOT EXISTS (SELECT * FROM VaiTro WHERE maVaiTro = 'ADMIN')
BEGIN
    INSERT INTO VaiTro (maVaiTro, tenVaiTro, trangThai) VALUES ('ADMIN', N'Quản trị viên', 1);
END

IF NOT EXISTS (SELECT * FROM VaiTro WHERE maVaiTro = 'STAFF')
BEGIN
    INSERT INTO VaiTro (maVaiTro, tenVaiTro, trangThai) VALUES ('STAFF', N'Nhân viên', 1);
END

IF NOT EXISTS (SELECT * FROM VaiTro WHERE maVaiTro = 'USER')
BEGIN
    INSERT INTO VaiTro (maVaiTro, tenVaiTro, trangThai) VALUES ('USER', N'Khách hàng', 1);
END
