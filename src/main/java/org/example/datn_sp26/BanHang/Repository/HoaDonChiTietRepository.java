package org.example.datn_sp26.BanHang.Repository;

import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.example.datn_sp26.BanHang.Entity.HoaDonChiTiet;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    // Cách 1: Dùng Query Method (Spring Boot tự hiểu)
    List<HoaDonChiTiet> findByIdHoaDon_Id(Integer idHoaDon);

    // Cách 2: @Query chính xác
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
           WHERE hdc.idHoaDon.idTrangThaiHoaDon.id = 4
           AND (:tuNgay IS NULL OR hdc.idHoaDon.ngayTao >= :tuNgay)
           AND (:denNgay IS NULL OR hdc.idHoaDon.ngayTao <= :denNgay)
           GROUP BY hdc.idSanPhamChiTiet.idSanPham.tenSanPham
           ORDER BY SUM(hdc.soLuong) DESC
           """)
    List<Object[]> topSanPhamBanChay(
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay,
            Pageable pageable
    );
    // POS: Tìm chi tiết hóa đơn theo hóa đơn + sản phẩm chi tiết (để gộp số lượng)
    Optional<HoaDonChiTiet> findByIdHoaDonAndIdSanPhamChiTiet(HoaDon hoaDon, SanPhamChiTiet spct);

    // POS: JOIN FETCH kèm tên sản phẩm
    @Query("SELECT h FROM HoaDonChiTiet h " +
            "LEFT JOIN FETCH h.idSanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.idSanPham " +
            "LEFT JOIN FETCH spct.idMauSac " +
            "LEFT JOIN FETCH spct.idSize " +
            "LEFT JOIN FETCH spct.idChatLieu " +
            "WHERE h.idHoaDon.id = ?1")
    List<HoaDonChiTiet> findByHoaDonIdWithFullDetails(Integer idHoaDon);

    @Transactional
    void deleteByIdSanPhamChiTiet_IdIn(List<Integer> sanPhamChiTietIds);
}
