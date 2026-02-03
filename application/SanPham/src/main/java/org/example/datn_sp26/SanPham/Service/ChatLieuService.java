package org.example.datn_sp26.SanPham.Service;

import org.example.datn_sp26.SanPham.Entity.ChatLieu;
import org.example.datn_sp26.SanPham.Repository.ChatLieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatLieuService {
    
    @Autowired
    private ChatLieuRepository chatLieuRepository;
    
    public List<ChatLieu> getAllChatLieu() {
        return chatLieuRepository.findAll();
    }
    
    public List<ChatLieu> getChatLieuActive() {
        return chatLieuRepository.findByTrangThai(1);
    }
}
