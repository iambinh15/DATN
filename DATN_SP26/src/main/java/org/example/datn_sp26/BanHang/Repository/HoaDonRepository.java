package org.example.datn_sp26.BanHang.Repository;

import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

}
