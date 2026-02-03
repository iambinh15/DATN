package org.example.datn_sp26.DangNhap.repository;

import org.example.datn_sp26.NguoiDung.Entity.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaiTroRepository extends JpaRepository<VaiTro, Long> {
    List<VaiTro> findByMaVaiTro(String maVaiTro);

    Optional<VaiTro> findFirstByMaVaiTro(String maVaiTro);
}
