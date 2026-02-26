package org.example.datn_sp26.ThongKe.Repository;

import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;

public interface ThongKeRepository extends JpaRepository<HoaDon, Integer> {

    @Query("""
           SELECT COALESCE(SUM(h.tongThanhToan), 0)
           FROM HoaDon h
           WHERE h.idTrangThaiHoaDon.tenTrangThai = 'Hoàn thành'
           AND (:tuNgay IS NULL OR h.ngayTao >= :tuNgay)
           AND (:denNgay IS NULL OR h.ngayTao <= :denNgay)
           """)
    BigDecimal tinhDoanhThu(
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay
    );

    @Query("""
           SELECT COUNT(h)
           FROM HoaDon h
           WHERE h.idTrangThaiHoaDon.tenTrangThai = 'Hoàn thành'
           AND (:tuNgay IS NULL OR h.ngayTao >= :tuNgay)
           AND (:denNgay IS NULL OR h.ngayTao <= :denNgay)
           """)
    Long demHoaDon(
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay
    );
}