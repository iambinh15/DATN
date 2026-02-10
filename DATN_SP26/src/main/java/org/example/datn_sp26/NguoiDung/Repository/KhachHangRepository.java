package org.example.datn_sp26.NguoiDung.Repository;

import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.List;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    boolean existsByMaKhachHang(String maKhachHang);
    boolean existsByEmail(String email);
    boolean existsBySdt(String sdt);

    boolean existsByMaKhachHangAndIdNot(String maKhachHang, Integer id);
    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsBySdtAndIdNot(String sdt, Integer id);

    @Query("""
        SELECT kh FROM KhachHang kh
        WHERE
            kh.maKhachHang LIKE %:keyword%
         OR kh.tenKhachHang LIKE %:keyword%
         OR kh.sdt LIKE %:keyword%
         OR kh.email LIKE %:keyword%
    """)
    List<KhachHang> search(@Param("keyword") String keyword);
}
