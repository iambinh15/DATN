package org.example.datn_sp26.SanPham.Service;

import org.example.datn_sp26.SanPham.Entity.ChatLieu;
import org.example.datn_sp26.SanPham.Repository.ChatLieuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatLieuService {

    private final ChatLieuRepository repository;

    public ChatLieuService(ChatLieuRepository repository) {
        this.repository = repository;
    }

    public List<ChatLieu> findAll() {
        return repository.findAll();
    }

    public ChatLieu findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public void save(ChatLieu chatLieu) {
        repository.save(chatLieu);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
