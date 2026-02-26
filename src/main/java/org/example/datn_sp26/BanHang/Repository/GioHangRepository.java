//package org.example.datn_sp26.NguoiDung.Entity.KhachHang.Repository;
package org.example.datn_sp26.BanHang.Repository;

import org.example.datn_sp26.BanHang.Entity.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Giỏ hàng của khách
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    // Thêm _Id để trỏ vào thuộc tính id bên trong đối tượng idKhachHang
    Optional<GioHang> findByIdKhachHang_Id(Integer idKhachHang);

}