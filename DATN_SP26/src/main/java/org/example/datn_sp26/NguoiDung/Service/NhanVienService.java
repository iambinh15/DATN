package org.example.datn_sp26.NguoiDung.Service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.example.datn_sp26.NguoiDung.Repository.NhanVienRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NhanVienService {

    private final NhanVienRepository repo;

    public List<NhanVien> getAll() {
        return repo.findAll();
    }

    public NhanVien getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
    }

    public void save(NhanVien nv) {

        if (nv.getId() == null) {
            if (repo.existsByMaNhanVien(nv.getMaNhanVien()))
                throw new ValidationException("Mã nhân viên đã tồn tại");

            if (repo.existsByEmail(nv.getEmail()))
                throw new ValidationException("Email đã tồn tại");

            if (repo.existsBySdt(nv.getSdt()))
                throw new ValidationException("Số điện thoại đã tồn tại");
        } else {
            if (repo.existsByMaNhanVienAndIdNot(nv.getMaNhanVien(), nv.getId()))
                throw new ValidationException("Mã nhân viên đã tồn tại");

            if (repo.existsByEmailAndIdNot(nv.getEmail(), nv.getId()))
                throw new ValidationException("Email đã tồn tại");

            if (repo.existsBySdtAndIdNot(nv.getSdt(), nv.getId()))
                throw new ValidationException("Số điện thoại đã tồn tại");
        }

        repo.save(nv);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }

    public List<NhanVien> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repo.findAll();
        }
        return repo.search(keyword.trim());
    }
}
