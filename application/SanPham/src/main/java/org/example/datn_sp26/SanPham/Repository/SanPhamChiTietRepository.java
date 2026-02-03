package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {
    
    List<SanPhamChiTiet> findByTrangThai(Integer trangThai);
    
    List<SanPhamChiTiet> findBySanPhamId(Integer sanPhamId);
    
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.sanPham.id = ?1 AND spct.trangThai = 1")
    List<SanPhamChiTiet> findActiveBySanPhamId(Integer sanPhamId);
}
