package org.example.datn_sp26.NguoiDung.Service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Repository.KhachHangRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhachHangService {

    private final KhachHangRepository repo;

    public List<KhachHang> getAll() {
        return repo.findAll();
    }

    public KhachHang getById(Integer id) {
        return repo.findById(id).orElse(null);
    }

    public void save(KhachHang kh) {

        if (kh.getNgayTao() == null) {
            kh.setNgayTao(new Date().toInstant());
        }

        if (kh.getId() == null) {
            if (repo.existsByMaKhachHang(kh.getMaKhachHang()))
                throw new ValidationException("Mã khách hàng đã tồn tại");

            if (repo.existsByEmail(kh.getEmail()))
                throw new ValidationException("Email đã tồn tại");

            if (repo.existsBySdt(kh.getSdt()))
                throw new ValidationException("Số điện thoại đã tồn tại");
        }
        else {
            if (repo.existsByMaKhachHangAndIdNot(kh.getMaKhachHang(), kh.getId()))
                throw new ValidationException("Mã khách hàng đã tồn tại");

            if (repo.existsByEmailAndIdNot(kh.getEmail(), kh.getId()))
                throw new ValidationException("Email đã tồn tại");

            if (repo.existsBySdtAndIdNot(kh.getSdt(), kh.getId()))
                throw new ValidationException("Số điện thoại đã tồn tại");
        }

        repo.save(kh);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }

    public List<KhachHang> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repo.findAll();
        }
        return repo.search(keyword);
    }
}