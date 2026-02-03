package org.example.datn_sp26.DangNhap.service;

import org.example.datn_sp26.DangNhap.dto.RegisterRequest;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.TaiKhoan;
import org.example.datn_sp26.NguoiDung.Entity.VaiTro;
import org.example.datn_sp26.DangNhap.repository.KhachHangRepository;
import org.example.datn_sp26.DangNhap.repository.TaiKhoanRepository;
import org.example.datn_sp26.DangNhap.repository.VaiTroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private EmailService emailService;

    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) throws Exception {
        try {
            System.out.println("Bắt đầu đăng ký cho user: " + request.getUsername());
            System.out.println("Email: " + request.getEmail());

            // 1. Validate mật khẩu
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new Exception("Mật khẩu xác nhận không khớp!");
            }

            // 2. Kiểm tra Username đã tồn tại chưa
            if (taiKhoanRepository.findByTenDangNhap(request.getUsername()).isPresent()) {
                throw new Exception("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại!");
            }

            // 2.1. Kiểm tra Email đã tồn tại chưa
            if (khachHangRepository.findFirstByEmail(request.getEmail()).isPresent()) {
                throw new Exception("Email '" + request.getEmail() + "' đã được sử dụng!");
            }

            // 3. Lấy Role USER (Khách hàng)
            VaiTro roleUser = vaiTroRepository.findFirstByMaVaiTro("USER")
                    .orElseThrow(() -> {
                        System.out.println("Lỗi: Không tìm thấy role USER trong DB");
                        return new Exception("Lỗi hệ thống: Không tìm thấy quyền Khách hàng (USER)");
                    });
            System.out.println("Tìm thấy Role: " + roleUser.getTenVaiTro() + " (ID: " + roleUser.getId() + ")");

            // 4. Tạo Tài Khoản
            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setTenDangNhap(request.getUsername());
            taiKhoan.setMatKhau(request.getPassword()); 
            taiKhoan.setVaiTro(roleUser);
            taiKhoan.setTrangThai(1); 
            
            TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);
            System.out.println("Đã lưu Tài khoản ID: " + savedTaiKhoan.getId());

            // 5. Tạo Khách Hàng
            KhachHang khachHang = new KhachHang();
            khachHang.setTen(request.getHoTen());
            khachHang.setEmail(request.getEmail());
            khachHang.setSdt(request.getSdt());
            khachHang.setTaiKhoan(savedTaiKhoan);
            
            khachHang.setNgayTao(new Date());
            khachHang.setTrangThai(1); 
            khachHang.setGioiTinh(true); 
            
            // Tạo mã khách hàng tự động
            khachHang.setMaKhachHang("KH" + System.currentTimeMillis()); 

            KhachHang savedKhachHang = khachHangRepository.save(khachHang);
            System.out.println("Đã lưu Khách hàng ID: " + savedKhachHang.getId());
            
        } catch (Exception e) {
            System.err.println("LỖI TRONG QUÁ TRÌNH ĐĂNG KÝ:");
            e.printStackTrace(); 
            throw e; 
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void forgotPassword(String email) throws Exception {
        try {
            System.out.println("Bắt đầu xử lý quên mật khẩu cho email: " + email);
            
            // 1. Tìm Khách Hàng theo Email
            KhachHang khachHang = khachHangRepository.findFirstByEmail(email)
                    .orElseThrow(() -> {
                        System.out.println("Không tìm thấy khách hàng với email: " + email);
                        return new Exception("Email này chưa được đăng ký!");
                    });
            System.out.println("Tìm thấy khách hàng: " + khachHang.getTen());

            TaiKhoan taiKhoan = khachHang.getTaiKhoan();
            if (taiKhoan == null) {
                System.out.println("Lỗi: Khách hàng không có tài khoản liên kết");
                throw new Exception("Lỗi dữ liệu: Khách hàng không có tài khoản liên kết!");
            }

            // 2. Tạo mật khẩu mới ngẫu nhiên
            String newPassword = generateRandomPassword(8);
            System.out.println("Mật khẩu mới đã tạo: " + newPassword);

            // 3. Cập nhật mật khẩu vào DB
            taiKhoan.setMatKhau(newPassword);
            taiKhoanRepository.save(taiKhoan);
            System.out.println("Đã cập nhật mật khẩu mới vào DB");

            // 4. Gửi email
            System.out.println("Đang gửi email...");
            emailService.sendNewPasswordEmail(email, newPassword);
            System.out.println("Gửi email thành công!");
            
        } catch (Exception e) {
            System.err.println("LỖI TRONG QUÁ TRÌNH QUÊN MẬT KHẨU:");
            e.printStackTrace();
            throw e;
        }
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
