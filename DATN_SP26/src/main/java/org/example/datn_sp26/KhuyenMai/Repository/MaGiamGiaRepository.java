package org.example.datn_sp26.KhuyenMai.Repository;

import org.example.datn_sp26.KhuyenMai.Entity.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Integer> {

    List<MaGiamGia> findByTrangThai(Integer trangThai);
}
