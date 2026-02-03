package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {
    List<Size> findByTrangThai(Integer trangThai);
}
