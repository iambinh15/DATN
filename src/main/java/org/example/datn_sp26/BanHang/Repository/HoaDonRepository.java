package org.example.datn_sp26.BanHang.Repository;

import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    List<HoaDon> findByIdKhachHang_Id(Integer idKhachHang);

    @Query("SELECT h FROM HoaDon h WHERE h.idKhachHang.id = :idKH AND h.idTrangThaiHoaDon.tenTrangThai <> 'test' ORDER BY h.ngayTao DESC")
    List<HoaDon> findByKhachHangExcludeTest(@Param("idKH") Integer idKH);

//    @Query("SELECT h FROM HoaDon h " +
//            "WHERE (:tenKH IS NULL OR h.idKhachHang.tenKhachHang LIKE %:tenKH%) " +
//            "AND (:trangThai IS NULL OR h.idTrangThaiHoaDon.tenTrangThai = :trangThai) " +
//            "AND (:tuNgay IS NULL OR h.ngayTao >= :tuNgay) " +
//            "AND (:denNgay IS NULL OR h.ngayTao <= :denNgay) " +
//            "ORDER BY h.ngayTao DESC")
@Query("SELECT h FROM HoaDon h " +
        "LEFT JOIN FETCH h.nhanVien " +
        "LEFT JOIN FETCH h.idKhachHang " +
        "LEFT JOIN FETCH h.idLoaiThanhToan " +
        "LEFT JOIN FETCH h.idTrangThaiHoaDon " +
        "WHERE (:tenKH IS NULL OR h.idKhachHang.tenKhachHang LIKE %:tenKH%) " +
        "AND (:trangThai IS NULL OR h.idTrangThaiHoaDon.tenTrangThai = :trangThai) " +
        "AND (:tuNgay IS NULL OR h.ngayTao >= :tuNgay) " +
        "AND (:denNgay IS NULL OR h.ngayTao <= :denNgay) " +
        "ORDER BY h.ngayTao DESC")

List<HoaDon> filterHoaDon(
            @Param("tenKH") String tenKH,
            @Param("trangThai") String trangThai,
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay);

    @Query("""
       SELECT COALESCE(SUM(h.tongThanhToan), 0)
       FROM HoaDon h
       WHERE h.idTrangThaiHoaDon.tenTrangThai = 'Hoàn thành'
       """)
    Double sumDoanhThuHoanThanh();

    @Query("""
       SELECT COUNT(h)
       FROM HoaDon h
       WHERE h.idTrangThaiHoaDon.tenTrangThai = 'Hoàn thành'
       """)
    Long countHoaDonHoanThanh();

    @Query("""
        SELECT COALESCE(SUM(h.tongThanhToan),0)
        FROM HoaDon h
        WHERE (:tu IS NULL OR h.ngayTao >= :tu)
        AND (:den IS NULL OR h.ngayTao <= :den)
        AND h.idTrangThaiHoaDon.id = 4
    """)
    BigDecimal tongDoanhThu(
            @Param("tu") Instant tu,
            @Param("den") Instant den
    );
}

