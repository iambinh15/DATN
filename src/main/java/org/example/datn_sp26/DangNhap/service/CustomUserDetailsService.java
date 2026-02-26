package org.example.datn_sp26.DangNhap.service;

import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.TaiKhoan;
import org.example.datn_sp26.DangNhap.repository.KhachHangDangNhapRepository;
import org.example.datn_sp26.DangNhap.repository.TaiKhoanDangNhapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private TaiKhoanDangNhapRepository taiKhoanDangNhapRepository;

    @Autowired
    private KhachHangDangNhapRepository khachHangDangNhapRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Dang kiem tra dang nhap cho user: " + username);

        TaiKhoan taiKhoan = taiKhoanDangNhapRepository.findByTenDangNhap(username)
                .orElseThrow(() -> {
                    System.out.println("Khong tim thay user: " + username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        System.out.println("Tim thay user: " + taiKhoan.getTenDangNhap());
        System.out.println("Mat khau trong DB: " + taiKhoan.getMatKhau());
        System.out.println("Ma vai tro: " + taiKhoan.getVaiTro().getMaVaiTro());

        String roleCode = taiKhoan.getVaiTro().getMaVaiTro();

        // Nếu là USER, kiểm tra trangThai từ bảng KhachHang
        if ("USER".equals(roleCode)) {
            KhachHang khachHang = khachHangDangNhapRepository.findByIdTaiKhoan(taiKhoan).orElse(null);
            if (khachHang == null || khachHang.getTrangThai() == null || khachHang.getTrangThai() == 0) {
                System.out.println("Tai khoan khach hang bi vo hieu hoa hoac khong ton tai: " + username);
                throw new UsernameNotFoundException("Tài khoản không tồn tại");
            }
            System.out.println("KhachHang trangThai: " + khachHang.getTrangThai() + " - Cho phep dang nhap");
        }

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleCode);

        return new User(taiKhoan.getTenDangNhap(), taiKhoan.getMatKhau(), Collections.singletonList(authority));
    }
}
