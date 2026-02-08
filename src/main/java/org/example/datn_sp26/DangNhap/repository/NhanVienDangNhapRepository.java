package org.example.datn_sp26.DangNhap.repository;

import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.example.datn_sp26.NguoiDung.Entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhanVienDangNhapRepository extends JpaRepository<NhanVien, Long> {
    Optional<NhanVien> findByTaiKhoan(TaiKhoan taiKhoan);
}
