package org.example.datn_sp26.KhuyenMai.Repository;

import org.example.datn_sp26.KhuyenMai.Entity.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Integer> {

    List<MaGiamGia> findByTrangThai(Integer trangThai);
    // Tìm kiếm mã giảm giá theo mã code (ví dụ: 'SALE10')
    // Thêm điều kiện trangThai = 1 để chỉ lấy mã còn hoạt động
    @Query("SELECT k FROM MaGiamGia k WHERE k.ma = ?1 AND k.trangThai = 1")
    Optional<MaGiamGia> findActiveVoucherByMa(String ma);

    // Bạn cũng có thể dùng phương thức tự sinh của Spring Data JPA:
    Optional<MaGiamGia> findByMaAndTrangThai(String ma, Integer trangThai);
}
