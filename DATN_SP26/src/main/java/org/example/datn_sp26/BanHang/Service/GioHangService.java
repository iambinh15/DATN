package org.example.datn_sp26.BanHang.Service;

import jakarta.transaction.Transactional;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.BanHang.Entity.GioHang;
import org.example.datn_sp26.BanHang.Entity.GioHangChiTiet;

import org.example.datn_sp26.BanHang.Repository.GioHangChiTietRepository;
import org.example.datn_sp26.BanHang.Repository.GioHangRepository;

import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class GioHangService {
    @Autowired private GioHangRepository gioHangRepo;
    @Autowired private GioHangChiTietRepository ghctRepo;
    @Autowired private SanPhamChiTietRepository spctRepo;


    /**
     * Hàm thêm sản phẩm vào giỏ hàng
     */
    @Transactional
    public void themVaoGio(Integer idKhachHang, Integer idSanPham) {
        GioHang gioHang = gioHangRepo.findByIdKhachHang_Id(idKhachHang)
                .orElseGet(() -> {
                    GioHang newGH = new GioHang();
                    KhachHang kh = new KhachHang();
                    kh.setId(idKhachHang);
                    newGH.setIdKhachHang(kh);
                    newGH.setNgayTao(Instant.now());
                    newGH.setTrangThai(1);
                    return gioHangRepo.save(newGH);
                });

        SanPhamChiTiet spct = spctRepo.findFirstByIdSanPham_IdAndTrangThai(idSanPham, 1)
                .orElseThrow(() -> new RuntimeException("Sản phẩm này hiện đang hết hàng hoặc ngừng kinh doanh!"));

        Optional<GioHangChiTiet> existingItem = ghctRepo.findByIdGioHang_IdAndIdSanPhamChiTiet_Id(gioHang.getId(), spct.getId());

        if (existingItem.isPresent()) {
            GioHangChiTiet item = existingItem.get();
            item.setSoLuong(item.getSoLuong() + 1);
            ghctRepo.save(item);
        } else {
            GioHangChiTiet newItem = new GioHangChiTiet();
            newItem.setIdGioHang(gioHang);
            newItem.setIdSanPhamChiTiet(spct);
            newItem.setSoLuong(1);

            ghctRepo.save(newItem);
        }
    }

    /**
     * Hàm tăng hoặc giảm số lượng sản phẩm trong giỏ (Dùng cho nút + và -)
     */
    @Transactional
    public void thayDoiSoLuong(Integer idGhct, Integer delta) {
        GioHangChiTiet item = ghctRepo.findById(idGhct)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        int soLuongMoi = item.getSoLuong() + delta;

        if (soLuongMoi > 0) {
            item.setSoLuong(soLuongMoi);
            ghctRepo.save(item);
        } else {
            ghctRepo.delete(item);
        }
    }

    /**
     * Hàm cập nhật số lượng cụ thể (Dùng khi khách nhập số từ bàn phím)
     * @param idGhct ID của bản ghi GioHangChiTiet
     * @param soLuongMoi Số lượng khách hàng nhập vào
     */
    @Transactional
    public void capNhatSoLuongTuyChinh(Integer idGhct, Integer soLuongMoi) {
        GioHangChiTiet item = ghctRepo.findById(idGhct)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        if (soLuongMoi > 0) {
            item.setSoLuong(soLuongMoi);
            ghctRepo.save(item);
        } else {
            // Nếu người dùng nhập 0 hoặc số âm, ta xóa sản phẩm khỏi giỏ
            ghctRepo.delete(item);
        }
    }

    /**
     * Hàm lấy danh sách tất cả sản phẩm trong giỏ của khách hàng
     */
    public List<GioHangChiTiet> layGioHangCuaKhach(Integer idKhachHang) {
        Optional<GioHang> gioHang = gioHangRepo.findByIdKhachHang_Id(idKhachHang);
        if (gioHang.isPresent()) {
            return ghctRepo.findByIdGioHang_Id(gioHang.get().getId());
        }
        return Collections.emptyList();
    }

    /**
     * Xóa 1 dòng sản phẩm
     */
    @Transactional
    public void xoaSanPhamKhoiGio(Integer idGioHangChiTiet) {
        if (ghctRepo.existsById(idGioHangChiTiet)) {
            ghctRepo.deleteById(idGioHangChiTiet);
        } else {
            throw new RuntimeException("Không tìm thấy sản phẩm này!");
        }
    }
    @Transactional
    public void xoaDanhSachSanPhamDaMua(List<Integer> idsGhct) {
        if (idsGhct != null && !idsGhct.isEmpty()) {
            // Sử dụng deleteAllById để xóa nhanh danh sách ID giỏ hàng chi tiết
            ghctRepo.deleteAllById(idsGhct);
        }
    }
    /**
     * Xóa sạch giỏ hàng
     */
    @Transactional
    public void xoaTatCaGioHang(Integer idKhachHang) {
        Optional<GioHang> gioHang = gioHangRepo.findByIdKhachHang_Id(idKhachHang);
        if (gioHang.isPresent()) {
            List<GioHangChiTiet> chiTiets = ghctRepo.findByIdGioHang_Id(gioHang.get().getId());
            ghctRepo.deleteAll(chiTiets);
        }
    }
}