package org.example.datn_sp26.NguoiDung.Service;

import org.example.datn_sp26.NguoiDung.Entity.DiaChi;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.TaiKhoan;
import org.example.datn_sp26.NguoiDung.Repository.KhachHangRepository;
import org.example.datn_sp26.NguoiDung.Repository.TaiKhoanRepository;
import org.example.datn_sp26.NguoiDung.Repository.DiaChiRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import org.example.datn_sp26.NguoiDung.Repository.KhachHangRepository;
@Service
public class KhachHangService {
    @Autowired
    private DiaChiRepository diaChiRepository;
    private final KhachHangRepository repo;
    private final TaiKhoanRepository taiKhoanRepo;
    @Autowired
    private KhachHangRepository khachHangRepository;
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
    @Transactional
    public KhachHang themNhanh(String ten, String sdt, String diaChiText) {

        if (ten == null || ten.isBlank()) {
            throw new RuntimeException("Tên khách hàng không được để trống");
        }

        if (sdt == null || sdt.isBlank()) {
            throw new RuntimeException("Số điện thoại không được để trống");
        }

        if (khachHangRepository.findBySdt(sdt).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
        KhachHang kh = new KhachHang();
        kh.setTenKhachHang(ten);
        kh.setSdt(sdt);
        kh.setNgayTao(Instant.now());
        kh.setTrangThai(1);
        kh = khachHangRepository.save(kh); // save trước để có ID
        if (diaChiText != null && !diaChiText.isBlank()) {
            DiaChi dc = new DiaChi();
            dc.setDiaChi(diaChiText);      // phải tồn tại field này trong DiaChi
            dc.setIdKhachHang(kh);         // liên kết với khách hàng
            diaChiRepository.save(dc);
        }
        return kh;
    }
}
