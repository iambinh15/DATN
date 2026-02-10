package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MauSacRepository extends JpaRepository<MauSac, Integer> {
    List<MauSac> findByTrangThai(Integer trangThai);
}
