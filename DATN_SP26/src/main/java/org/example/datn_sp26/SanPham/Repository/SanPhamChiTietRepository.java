package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    // ✅ FIX: join đúng tên FIELD trong Entity
    @Query("""
        SELECT s
        FROM SanPhamChiTiet s
        LEFT JOIN FETCH s.idSanPham
        LEFT JOIN FETCH s.idMauSac
        LEFT JOIN FETCH s.idSize
        WHERE s.idSanPham.id = :id
    """)
    List<SanPhamChiTiet> findAllWithDetailsBySanPhamId(@Param("id") Integer id);

    // ✅ method này OK, giữ nguyên
    Optional<SanPhamChiTiet> findFirstByIdSanPham_IdAndTrangThai(
            Integer idSanPham,
            Integer trangThai
    );
}
