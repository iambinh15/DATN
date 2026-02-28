package org.example.datn_sp26.BanHang.Service;

import jakarta.transaction.Transactional;
import org.example.datn_sp26.BanHang.Entity.*;
import org.example.datn_sp26.BanHang.Repository.*;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.example.datn_sp26.SanPham.Service.SanPhamChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class HoaDonService {

    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private TrangThaiHoaDonRepository trangThaiHoaDonRepository;
    @Autowired private LoaiThanhToanRepository loaiThanhToanRepository;
    @Autowired private SanPhamChiTietRepository sanPhamChiTietRepository;
    @Autowired private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired private GioHangService gioHangService;
    @Autowired private SanPhamChiTietService sanPhamChiTietService;

    // Hằng số ID trạng thái
    private static final int ID_CHO_XAC_NHAN = 1;
    private static final int ID_DA_XAC_NHAN = 13;
    private static final int ID_CHO_THANH_TOAN = 11;
    private static final int ID_DA_THANH_TOAN = 14;
    private static final int ID_HUY = 5;

    // ============================================================
    // 1. FIX LỖI Ở HoaDonAdminController (image_22a1dc.jpg, image_229ddc.jpg)
    // ============================================================

    public List<TrangThaiHoaDon> getAllTrangThai() {
        return trangThaiHoaDonRepository.findAll();
    }

    public List<HoaDon> filterHoaDon(String tenKH, String trangThai, String loaiTT, Instant tuNgay, Instant denNgay) {
        return hoaDonRepository.filterHoaDon(tenKH, trangThai, loaiTT, tuNgay, denNgay);
    }

    public void capNhatTrangThai(Integer hoaDonId, Integer trangThaiIdMoi) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElseThrow();
        int trangThaiCu = hoaDon.getIdTrangThaiHoaDon().getId();
        if (trangThaiCu == ID_CHO_THANH_TOAN && trangThaiIdMoi == ID_DA_THANH_TOAN) {
            truSoLuongTonKho(hoaDonId);
        }
        TrangThaiHoaDon tt = trangThaiHoaDonRepository.findById(trangThaiIdMoi).get();
        hoaDon.setIdTrangThaiHoaDon(tt);
        hoaDonRepository.save(hoaDon);
    }

    @Transactional
    public void huyDonHangVaHoanKho(Integer idHoaDon) {
        HoaDon hd = hoaDonRepository.findById(idHoaDon).orElseThrow();
        for (HoaDonChiTiet ct : hd.getHoaDonChiTiets()) {
            SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
            if (spct != null) {
                spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                sanPhamChiTietRepository.save(spct);
            }
        }
    }

    // ============================================================
    // 2. FIX LỖI Ở PaymentController (image_229e96.jpg)
    // ============================================================

    @Transactional
    public HoaDon taoHoaDonVNPay(KhachHang khachHang, BigDecimal tongThanhToan, String diaChi, BigDecimal phiShip) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon("HD_VNP" + System.currentTimeMillis());
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setDiaChi(diaChi);
        hoaDon.setTongThanhToan(tongThanhToan);
        hoaDon.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(ID_CHO_XAC_NHAN).get());
        hoaDon.setIdLoaiThanhToan(loaiThanhToanRepository.findByTenLoai("CK").get());
        hoaDonRepository.save(hoaDon);

        var listGioHang = gioHangService.layGioHangCuaKhach(khachHang.getId());
        for (var item : listGioHang) {
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setIdHoaDon(hoaDon);
            hdct.setIdSanPhamChiTiet(item.getIdSanPhamChiTiet());
            hdct.setSoLuong(item.getSoLuong());
            hdct.setDonGia(item.getIdSanPhamChiTiet().getDonGia());
            hoaDonChiTietRepository.save(hdct);

            // Online trừ kho luôn
            SanPhamChiTiet spct = item.getIdSanPhamChiTiet();
            spct.setSoLuong(spct.getSoLuong() - item.getSoLuong());
            sanPhamChiTietRepository.save(spct);
        }
        gioHangService.xoaTatCaGioHang(khachHang.getId());
        return hoaDon;
    }

    // ============================================================
    // 3. FIX LỖI Ở DonHangController (image_229a7e.jpg)
    // ============================================================

    public List<HoaDon> layDonHangCuaKhach(Integer idKhachHang) {
        return hoaDonRepository.findByIdKhachHang_IdOrderByNgayTaoDesc(idKhachHang);
    }

    // ============================================================
    // 4. LOGIC HỖ TRỢ CHUNG (Fix getMaSPCT -> getId)
    // ============================================================

    public void truSoLuongTonKho(Integer hoaDonId) {
        List<HoaDonChiTiet> listChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        for (HoaDonChiTiet chiTiet : listChiTiet) {
            SanPhamChiTiet spct = chiTiet.getIdSanPhamChiTiet();
            if (spct.getSoLuong() < chiTiet.getSoLuong()) {
                // Sửa thành getId() vì Entity của bạn không có maSPCT
                throw new RuntimeException("Sản phẩm ID " + spct.getId() + " không đủ tồn kho!");
            }
            spct.setSoLuong(spct.getSoLuong() - chiTiet.getSoLuong());
            sanPhamChiTietRepository.save(spct);
            sanPhamChiTietService.checkAndDisableSanPham(spct.getIdSanPham().getId());
        }
    }

    public List<HoaDon> findAll() {
        return hoaDonRepository.findAll(Sort.by(Sort.Direction.DESC, "ngayTao"));
    }

    @Transactional
    public HoaDon taoHoaDonCODDayDu(KhachHang khachHang, BigDecimal tongThanhToan, String diaChiGiaoHang) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon("HD_COD" + System.currentTimeMillis());
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setDiaChi(diaChiGiaoHang);
        hoaDon.setTongThanhToan(tongThanhToan);
        hoaDon.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(ID_CHO_THANH_TOAN).get());
        hoaDon.setIdLoaiThanhToan(loaiThanhToanRepository.findByTenLoai("Tiền mặt").get());
        hoaDonRepository.saveAndFlush(hoaDon);

        var listGioHang = gioHangService.layGioHangCuaKhach(khachHang.getId());
        for (var item : listGioHang) {
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setIdHoaDon(hoaDon);
            hdct.setIdSanPhamChiTiet(item.getIdSanPhamChiTiet());
            hdct.setSoLuong(item.getSoLuong());
            hdct.setDonGia(item.getIdSanPhamChiTiet().getDonGia());
            hoaDonChiTietRepository.save(hdct);
        }
        gioHangService.xoaTatCaGioHang(khachHang.getId());
        return hoaDon;
    }
}