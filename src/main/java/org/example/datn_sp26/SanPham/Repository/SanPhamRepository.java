package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    // 1. Lấy danh sách sản phẩm theo trạng thái (Giữ nguyên logic cũ)
    List<SanPham> findByTrangThai(int trangThai);

    // 2. Tìm kiếm sản phẩm theo tên (Giữ nguyên logic cũ)
    List<SanPham> findByTenSanPhamContainingIgnoreCaseAndTrangThai(String ten, int trangThai);

    // 3. Truy vấn lấy tất cả sản phẩm Active (Giữ nguyên logic cũ)
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = 1")
    List<SanPham> findAllActive();

    // 4. LOGIC MỚI: Chỉ hiển thị sản phẩm còn ít nhất 1 biến thể còn hàng (>0)
    // Dùng cho trang chủ để ẩn các sản phẩm như "Bomber" khi đã bán hết sạch các size
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = 1 AND EXISTS " +
            "(SELECT ct FROM SanPhamChiTiet ct WHERE ct.idSanPham.id = s.id " +
            "AND ct.soLuong > 0 AND ct.trangThai = 1)")
    List<SanPham> hienThiSanPhamTrenTrangChu();
}