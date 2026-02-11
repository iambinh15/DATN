package org.example.datn_sp26.NguoiDung.Service;

import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.TaiKhoan;
import org.example.datn_sp26.NguoiDung.Repository.KhachHangRepository;
import org.example.datn_sp26.NguoiDung.Repository.TaiKhoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KhachHangService {

    private final KhachHangRepository repo;
    private final TaiKhoanRepository taiKhoanRepo;

    public KhachHangService(KhachHangRepository repo, TaiKhoanRepository taiKhoanRepo) {
        this.repo = repo;
        this.taiKhoanRepo = taiKhoanRepo;
    }

    public List<KhachHang> findAll() {
        return repo.findByTrangThai(1); // Chỉ hiển thị khách hàng đang hoạt động
    }

    public KhachHang findById(Integer id) {
        return repo.findById(id).orElse(null);
    }

    public void save(KhachHang kh) {
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
}
