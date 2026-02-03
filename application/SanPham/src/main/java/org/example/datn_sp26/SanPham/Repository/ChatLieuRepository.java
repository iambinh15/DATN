package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu, Integer> {
    List<ChatLieu> findByTrangThai(Integer trangThai);
}
