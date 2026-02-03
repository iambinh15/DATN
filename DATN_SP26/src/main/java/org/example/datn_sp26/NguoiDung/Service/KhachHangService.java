package org.example.datn_sp26.NguoiDung.Service;

import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Repository.KhachHangRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KhachHangService {

    private final KhachHangRepository repo;

    public KhachHangService(KhachHangRepository repo) {
        this.repo = repo;
    }

    public List<KhachHang> findAll() {
        return repo.findAll();
    }

    public KhachHang findById(Integer id) {
        return repo.findById(id).orElse(null);
    }

    public void save(KhachHang kh) {
        repo.save(kh);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}
