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

    public NhanVien getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public void save(NhanVien nv) {
        repo.save(nv);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public List<NhanVien> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }
        String kw = keyword.trim();
        return repo.findByTenNhanVienContainingIgnoreCaseOrMaNhanVienContainingIgnoreCaseOrSdtContainingIgnoreCase(
                kw, kw, kw
        );
    }

    public boolean isMaNhanVienTrung(String ma, Long currentId) {
        if (ma == null || ma.trim().isEmpty()) return false;
        if (currentId == null) {
            return repo.existsByMaNhanVien(ma.trim());
        }
        return repo.existsByMaNhanVienAndIdNot(ma.trim(), currentId);
    }
}