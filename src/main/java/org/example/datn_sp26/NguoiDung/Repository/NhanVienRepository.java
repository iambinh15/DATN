package org.example.datn_sp26.NguoiDung.Repository;

import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {

    List<NhanVien> findByTenNhanVienContainingIgnoreCaseOrMaNhanVienContainingIgnoreCaseOrSdtContainingIgnoreCase(
            String tenNhanVien, String maNhanVien, String sdt
    );

    boolean existsByMaNhanVien(String maNhanVien);

    boolean existsByMaNhanVienAndIdNot(String maNhanVien, Long id);
}

