package org.example.datn_sp26.NguoiDung.Service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.TaiKhoan;
import org.example.datn_sp26.NguoiDung.Repository.KhachHangRepository;
import org.example.datn_sp26.NguoiDung.Repository.TaiKhoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhachHangService {

    private final KhachHangRepository repo;
    private final TaiKhoanRepository taiKhoanRepo;

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

    /**
     * Soft Delete - Chỉ đổi trạng thái, không xóa thật
     * trangThai = 0: Đã khóa/Đã xóa
     * trangThai = 1: Hoạt động
     */
    @Transactional
    public void delete(Integer id) {
        KhachHang kh = repo.findById(id).orElse(null);
        if (kh == null)
            return;

        // 1. Đổi trạng thái khách hàng thành "Đã khóa"
        kh.setTrangThai(0);
        repo.save(kh);

        // 2. Đổi trạng thái tài khoản đăng nhập thành "Đã khóa"
        TaiKhoan taiKhoan = kh.getIdTaiKhoan();
        if (taiKhoan != null) {
            taiKhoan.setTrangThai(0);
            taiKhoanRepo.save(taiKhoan);
        }
    }

    public List<KhachHang> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repo.findAll();
        }
        return repo.search(keyword);
    }
}