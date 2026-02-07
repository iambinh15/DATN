package org.example.datn_sp26.NguoiDung.Repository;

import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    List<KhachHang> findByTrangThai(Integer trangThai);
}
