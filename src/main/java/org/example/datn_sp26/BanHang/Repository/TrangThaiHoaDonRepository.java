package org.example.datn_sp26.BanHang.Repository;

import org.example.datn_sp26.BanHang.Entity.TrangThaiHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrangThaiHoaDonRepository
        extends JpaRepository<TrangThaiHoaDon, Integer> {

    Optional<TrangThaiHoaDon> findByTenTrangThai(String tenTrangThai);

    List<TrangThaiHoaDon> findByTenTrangThaiNot(String tenTrangThai);
}
