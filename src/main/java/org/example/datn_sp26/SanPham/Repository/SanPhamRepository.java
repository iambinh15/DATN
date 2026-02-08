package org.example.datn_sp26.SanPham.Repository;


import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    // Lấy danh sách sản phẩm theo trạng thái (thường là 1 cho Active)
    List<SanPham> findByTrangThai(int trangThai);

    // Tìm kiếm sản phẩm theo tên (hữu ích cho thanh tìm kiếm ở trang chủ)
    List<SanPham> findByTenSanPhamContainingIgnoreCaseAndTrangThai(String ten, int trangThai);

    // Truy vấn lấy sản phẩm kèm theo thông tin hãng hoặc loại (nếu bạn có quan hệ bảng)
    // Giúp tối ưu hóa tốc độ tải trang chủ (tránh lỗi N+1)
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = 1")
    List<SanPham> findAllActive();
}