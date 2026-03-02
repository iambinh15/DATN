package org.example.datn_sp26.BanHang.Repository;

import org.example.datn_sp26.BanHang.Entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    // Cách 1: Dùng Query Method (Spring Boot tự hiểu)
    // Giả sử trong Entity HoaDonChiTiet, trường liên kết với HoaDon có tên là
    // "idHoaDon"
    List<HoaDonChiTiet> findByIdHoaDon_Id(Integer idHoaDon);

    // Cách 2: Nếu cách trên không chạy (do đặt tên biến phức tạp), hãy dùng @Query
    // chính xác sau:
    @Query("SELECT h FROM HoaDonChiTiet h WHERE h.idHoaDon.id = ?1")
    List<HoaDonChiTiet> findByHoaDonId(Integer idHoaDon);

    // Cách 3: JOIN FETCH để load tất cả quan hệ (tránh lazy loading)
    @Query("SELECT h FROM HoaDonChiTiet h " +
            "LEFT JOIN FETCH h.idSanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.idMauSac " +
            "LEFT JOIN FETCH spct.idSize " +
            "LEFT JOIN FETCH spct.idChatLieu " +
            "WHERE h.idHoaDon.id = ?1")
    List<HoaDonChiTiet> findByHoaDonIdWithDetails(Integer idHoaDon);

    @Query("""
           SELECT hdc.idSanPhamChiTiet.idSanPham.tenSanPham,
                  SUM(hdc.soLuong)
           FROM HoaDonChiTiet hdc
           WHERE hdc.idHoaDon.idTrangThaiHoaDon.tenTrangThai = 'Hoàn thành'
           AND (:tuNgay IS NULL OR hdc.idHoaDon.ngayTao >= :tuNgay)
           AND (:denNgay IS NULL OR hdc.idHoaDon.ngayTao <= :denNgay)
           GROUP BY hdc.idSanPhamChiTiet.idSanPham.tenSanPham
           ORDER BY SUM(hdc.soLuong) DESC
           """)
    List<Object[]> topSanPhamBanChay(
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay
    );
}
