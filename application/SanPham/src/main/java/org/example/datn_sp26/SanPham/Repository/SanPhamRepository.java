package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    
    List<SanPham> findByTrangThai(Integer trangThai);
    
    @Query("SELECT sp FROM SanPham sp WHERE sp.tenSanPham LIKE %?1% OR sp.maSanPham LIKE %?1%")
    List<SanPham> searchByKeyword(String keyword);
    
    SanPham findByMaSanPham(String maSanPham);
}
