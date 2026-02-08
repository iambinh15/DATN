package org.example.datn_sp26.BanHang.Repository;

import org.example.datn_sp26.BanHang.Entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Chi tiết các món trong giỏ
public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Integer> {
    // Tìm xem áo này đã có trong giỏ chưa để tăng số lượng
    Optional<GioHangChiTiet> findByIdGioHang_IdAndIdSanPhamChiTiet_Id(Integer idGioHang, Integer idSanPhamChiTiet);
    // Hàm dùng để lấy tất cả sản phẩm trong giỏ để hiển thị
    List<GioHangChiTiet> findByIdGioHang_Id(Integer idGioHang);
}
