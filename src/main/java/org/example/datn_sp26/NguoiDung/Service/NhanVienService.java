package org.example.datn_sp26.NguoiDung.Service;

import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.example.datn_sp26.NguoiDung.Repository.NhanVienRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NhanVienService {

    private final NhanVienRepository repo;

    public NhanVienService(NhanVienRepository repo) {
        this.repo = repo;
    }

    public List<NhanVien> getAll() {
        return repo.findAll();
    }

    public NhanVien getById(Integer id) {
        return repo.findById(id).orElse(null);
    }

    public void save(NhanVien nv) {
        repo.save(nv);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}