package org.example.datn_sp26.DangNhap.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.datn_sp26.DangNhap.repository.KhachHangDangNhapRepository;
import org.example.datn_sp26.DangNhap.repository.NhanVienDangNhapRepository;
import org.example.datn_sp26.DangNhap.repository.TaiKhoanDangNhapRepository;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.example.datn_sp26.NguoiDung.Entity.TaiKhoan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private TaiKhoanDangNhapRepository taiKhoanRepository;

    @Autowired
    private KhachHangDangNhapRepository khachHangRepository;

    @Autowired
    private NhanVienDangNhapRepository nhanVienRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        HttpSession session = request.getSession();

        // Lấy TaiKhoan từ database
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(username).orElse(null);

        if (taiKhoan != null) {
            // Lưu TaiKhoan vào session
            session.setAttribute("taiKhoan", taiKhoan);
            session.setAttribute("idTaiKhoan", taiKhoan.getId());

            String role = taiKhoan.getVaiTro().getMaVaiTro();

            if ("USER".equals(role)) {
                // Nếu là khách hàng, lưu thêm KhachHang
                KhachHang khachHang = khachHangRepository.findByIdTaiKhoan(taiKhoan).orElse(null);
                if (khachHang != null) {
                    session.setAttribute("khachHang", khachHang);
                    session.setAttribute("idKhachHang", khachHang.getId());
                    session.setAttribute("tenKhachHang", khachHang.getTenKhachHang());
                }
                // Redirect đến trang chủ khách hàng
                response.sendRedirect("/trang-chu");

            } else if ("ADMIN".equals(role) || "STAFF".equals(role)) {
                // Nếu là nhân viên/admin, lưu thêm NhanVien
                NhanVien nhanVien = nhanVienRepository.findByTaiKhoan(taiKhoan).orElse(null);
                if (nhanVien != null) {
                    session.setAttribute("nhanVien", nhanVien);
                    session.setAttribute("idNhanVien", nhanVien.getId());
                    session.setAttribute("tenNhanVien", nhanVien.getTen());
                }
                // Redirect đến trang admin
                response.sendRedirect("/admin");
            } else {
                response.sendRedirect("/home");
            }
        } else {
            response.sendRedirect("/login?error");
        }
    }
}
