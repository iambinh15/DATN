package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {
    List<ThuongHieu> findByTrangThai(Integer trangThai);
}
