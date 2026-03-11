package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.HinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HinhAnhRepository extends JpaRepository<HinhAnh, Integer> {

    List<HinhAnh> findByIdSanPham_Id(Integer idSanPham);

    // ✅ Cách chuẩn Spring Data (khuyên dùng)
    Optional<HinhAnh> findTopByIdSanPham_IdOrderByIdAsc(Integer idSanPham);

    @org.springframework.transaction.annotation.Transactional
    void deleteByIdSanPham_Id(Integer idSanPham);
}

