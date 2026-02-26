package org.example.datn_sp26.BanHang.Repository;

import org.example.datn_sp26.BanHang.Entity.LoaiThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoaiThanhToanRepository  extends JpaRepository<LoaiThanhToan, Integer> {
    Optional<LoaiThanhToan> findByTenLoai(String tenLoai);
}
