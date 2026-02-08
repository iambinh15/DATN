package org.example.datn_sp26.DangNhap.repository;

import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangDangNhapRepository extends JpaRepository<KhachHang, Integer> {
    Optional<KhachHang> findByIdTaiKhoan(TaiKhoan taiKhoan);

    Optional<KhachHang> findFirstByEmail(String email);
}
