package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SizeRepository extends JpaRepository<Size, Integer> {
    List<Size> findByTrangThai(Integer trangThai);
}
