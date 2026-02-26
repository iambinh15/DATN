package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {
    List<ThuongHieu> findByTrangThai(Integer trangThai);
}
