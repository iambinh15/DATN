package org.example.datn_sp26.BanHang.Repository;

import org.example.datn_sp26.BanHang.Entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// Chi tiết các món trong giỏ
public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Integer> {
    // Tìm xem áo này đã có trong giỏ chưa để tăng số lượng
    Optional<GioHangChiTiet> findByIdGioHang_IdAndIdSanPhamChiTiet_Id(Integer idGioHang, Integer idSanPhamChiTiet);
    // Hàm dùng để lấy tất cả sản phẩm trong giỏ để hiển thị
    List<GioHangChiTiet> findByIdGioHang_Id(Integer idGioHang);

    @Query("""
            SELECT DISTINCT ghct
            FROM GioHangChiTiet ghct
            JOIN FETCH ghct.idSanPhamChiTiet spct
            JOIN FETCH spct.idSanPham sp
            LEFT JOIN FETCH sp.hinhAnhs
            LEFT JOIN FETCH spct.idMauSac
            LEFT JOIN FETCH spct.idSize
            LEFT JOIN FETCH spct.idChatLieu
            WHERE ghct.idGioHang.id = :idGioHang
            """)
    List<GioHangChiTiet> findByIdGioHang_IdWithDetails(Integer idGioHang);

    boolean existsByIdSanPhamChiTiet_Id(Integer idSanPhamChiTiet);

    @Transactional
    void deleteByIdSanPhamChiTiet_Id(Integer idSanPhamChiTiet);
}
