package org.example.datn_sp26.NguoiDung.Repository;

import org.example.datn_sp26.NguoiDung.Entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaChiRepository extends JpaRepository<DiaChi, Integer> {

    List<DiaChi> findByIdKhachHang_Id(Integer idKhachHang);

    Optional<DiaChi> findByIdKhachHang_IdAndTrangThai(Integer idKhachHang, Integer trangThai);
}
