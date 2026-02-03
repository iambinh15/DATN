package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac, Integer> {
    List<MauSac> findByTrangThai(Integer trangThai);
}
