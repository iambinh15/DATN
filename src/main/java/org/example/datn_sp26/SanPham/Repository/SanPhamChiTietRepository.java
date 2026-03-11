package org.example.datn_sp26.SanPham.Repository;

import jakarta.transaction.Transactional;
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

    // Tìm biến thể đầu tiên của sản phẩm mà còn hàng (soLuong > 0)
    Optional<SanPhamChiTiet> findFirstByIdSanPham_IdAndTrangThaiAndSoLuongGreaterThan(
            Integer idSanPham,
            Integer trangThai,
            Integer soLuong
    );
    // Lọc bỏ các bản ghi có số lượng bằng 0
    @Query("SELECT s FROM SanPhamChiTiet s WHERE s.soLuong > 0")
    List<SanPhamChiTiet> hienThiSPCTConHang();
    // Thêm dòng này vào để hết lỗi đỏ ở Service
    List<SanPhamChiTiet> findAllByIdSanPham_Id(Integer idSanPham);


    List<SanPhamChiTiet> findByIdSanPham_Id(Integer sanPhamId);

    // Kiểm tra trùng biến thể
    Optional<SanPhamChiTiet>
    findByIdSanPham_IdAndIdMauSac_IdAndIdSize_Id(
            Integer sanPhamId,
            Integer mauSacId,
            Integer sizeId
    );

    @Query("""
    SELECT COALESCE(SUM(s.soLuong),0)
    FROM SanPhamChiTiet s
    WHERE s.idSanPham.id = :sanPhamId
""")
    Integer tongSoLuongBySanPhamId(@Param("sanPhamId") Integer sanPhamId);

    @Transactional
    void deleteByIdSanPham_Id(Integer id);

    @Query("""
    SELECT spct
    FROM SanPhamChiTiet spct
    JOIN FETCH spct.idSanPham
    LEFT JOIN FETCH spct.idMauSac
    LEFT JOIN FETCH spct.idSize
    LEFT JOIN FETCH spct.idChatLieu
    WHERE spct.soLuong >= 1 AND spct.soLuong < :nguong
    ORDER BY spct.soLuong ASC
""")
    List<SanPhamChiTiet> findSanPhamSapHetHang(@Param("nguong") Integer nguong);

    long countBySoLuongGreaterThanEqualAndSoLuongLessThan(Integer minInclusive, Integer maxExclusive);
}
