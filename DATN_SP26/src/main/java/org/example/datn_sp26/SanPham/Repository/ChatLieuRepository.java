package org.example.datn_sp26.SanPham.Repository;

import org.example.datn_sp26.SanPham.Entity.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatLieuRepository extends JpaRepository<ChatLieu, Integer> {
    ChatLieu findByTenChatLieu(String tenChatLieu);
}
