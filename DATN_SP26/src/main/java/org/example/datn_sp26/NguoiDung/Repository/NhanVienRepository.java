package org.example.datn_sp26.NguoiDung.Repository;

import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    boolean existsByMaNhanVien(String maNhanVien);
    boolean existsByEmail(String email);
    boolean existsBySdt(String sdt);

    boolean existsByMaNhanVienAndIdNot(String maNhanVien, Integer id);
    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsBySdtAndIdNot(String sdt, Integer id);

    @Query("""
    SELECT nv FROM NhanVien nv
    WHERE
        nv.maNhanVien LIKE %:keyword%
        OR nv.tenNhanVien LIKE %:keyword%
        OR nv.sdt LIKE %:keyword%
        OR nv.email LIKE %:keyword%
""")
    List<NhanVien> search(@Param("keyword") String keyword);
}



