package org.example.datn_sp26.BanHang.Service;

import jakarta.transaction.Transactional;
import org.example.datn_sp26.BanHang.Entity.*;
import org.example.datn_sp26.BanHang.Repository.*;
import org.example.datn_sp26.KhuyenMai.Entity.MaGiamGia;
import org.example.datn_sp26.KhuyenMai.Repository.MaGiamGiaRepository;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.example.datn_sp26.SanPham.Service.SanPhamChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
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
    @Autowired private MaGiamGiaRepository maGiamGiaRepository;

    private static final int ID_CHO_XAC_NHAN = 1;
    private static final int ID_CHO_THANH_TOAN = 11;
    private static final int ID_DA_THANH_TOAN = 14;

    // ============================================================
    // LOGIC XỬ LÝ VOUCHER (Đã thêm logic trừ tiền)
    // ============================================================
    private void xuLyVoucher(HoaDon hoaDon, String maVoucher) {
        // THÔNG BÁO KIỂM TRA ĐẦU VÀO
        System.out.println("===> Service nhận được mã Voucher: [" + maVoucher + "]");

        if (maVoucher != null && !maVoucher.isEmpty()) {
            MaGiamGia voucher = maGiamGiaRepository.findByMa(maVoucher.trim())
                    .orElse(null);

            if (voucher == null) {
                System.out.println("===> THÔNG BÁO: Mã [" + maVoucher + "] KHÔNG tồn tại trong Database!");
                return; // Thoát nếu không tìm thấy mã
            }

            // 1. Gán voucher vào hóa đơn
            hoaDon.setIdMaGiamGia(voucher);
            System.out.println("===> THÔNG BÁO: Đã gán Voucher [" + voucher.getMa() + "] vào Hóa đơn thành công.");

            // 2. Logic trừ tiền
            BigDecimal giaTriGiam = BigDecimal.valueOf(voucher.getGiaTri());
            BigDecimal tongHienTai = hoaDon.getTongThanhToan();
            BigDecimal soTienTru = BigDecimal.ZERO;

            if (voucher.getLoaiGiam() == 0) {
                soTienTru = giaTriGiam;
            } else {
                soTienTru = tongHienTai.multiply(giaTriGiam).divide(BigDecimal.valueOf(100));
            }

            BigDecimal tongSauGiam = tongHienTai.subtract(soTienTru);
            if (tongSauGiam.compareTo(BigDecimal.ZERO) < 0) tongSauGiam = BigDecimal.ZERO;

            hoaDon.setTongThanhToan(tongSauGiam);

            System.out.println("===> THÔNG BÁO: Số tiền giảm: " + soTienTru + " | Tổng cuối: " + tongSauGiam);

            // 3. Trừ số lượng
            voucher.setSoLuong(voucher.getSoLuong() - 1);
            maGiamGiaRepository.save(voucher);
        } else {
            System.out.println("===> THÔNG BÁO: Không có mã Voucher nào được sử dụng (maVoucher is null/empty).");
        }
    }
    // ============================================================
    // THANH TOÁN VNPAY
    // ============================================================

    // Tương thích PaymentController cũ (4 tham số)
    @Transactional
    public HoaDon taoHoaDonVNPay(KhachHang khachHang, BigDecimal tongThanhToan, String diaChi, BigDecimal phiShip) {
        return taoHoaDonVNPay(khachHang, tongThanhToan, diaChi, phiShip, null);
    }

    @Transactional
    public HoaDon taoHoaDonVNPay(KhachHang khachHang, BigDecimal tongThanhToan, String diaChi, BigDecimal phiShip, String maVoucher) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon("HD_VNP" + System.currentTimeMillis());
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setDiaChi(diaChi);

        // Gán tổng tiền (Online thường đã trừ ở Client, nhưng gọi xuLyVoucher để lưu quan hệ MaGiamGia)
        hoaDon.setTongThanhToan(tongThanhToan);

        xuLyVoucher(hoaDon, maVoucher);

        hoaDon.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(ID_CHO_XAC_NHAN).get());
        hoaDon.setIdLoaiThanhToan(loaiThanhToanRepository.findByTenLoai("CK").get());
        hoaDonRepository.save(hoaDon);

        luuChiTietVaTruKho(hoaDon, khachHang.getId(), true);
        return hoaDon;
    }

    // ============================================================
    // THANH TOÁN COD
    // ============================================================

    // Tương thích GHNController cũ (3 tham số)
    @Transactional
    public HoaDon taoHoaDonCODDayDu(KhachHang khachHang, BigDecimal tongThanhToan, String diaChi) {
        return taoHoaDonCODDayDu(khachHang, tongThanhToan, diaChi, null);
    }

    @Transactional
    public HoaDon taoHoaDonCODDayDu(KhachHang khachHang, BigDecimal tongThanhToan, String diaChi, String maVoucher) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon("HD_COD" + System.currentTimeMillis());
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setDiaChi(diaChi);

        // Quan trọng: Gán tiền TRƯỚC khi gọi xuLyVoucher
        hoaDon.setTongThanhToan(tongThanhToan);

        xuLyVoucher(hoaDon, maVoucher);

        hoaDon.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(ID_CHO_THANH_TOAN).get());
        hoaDon.setIdLoaiThanhToan(loaiThanhToanRepository.findByTenLoai("Tiền mặt").get());
        hoaDonRepository.saveAndFlush(hoaDon);

        luuChiTietVaTruKho(hoaDon, khachHang.getId(), false);
        return hoaDon;
    }

    // Logic dùng chung để lưu chi tiết và xử lý giỏ hàng
    private void luuChiTietVaTruKho(HoaDon hoaDon, Integer idKhachHang, boolean laThanhToanOnline) {
        var listGioHang = gioHangService.layGioHangCuaKhach(idKhachHang);
        for (var item : listGioHang) {
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setIdHoaDon(hoaDon);
            hdct.setIdSanPhamChiTiet(item.getIdSanPhamChiTiet());
            hdct.setSoLuong(item.getSoLuong());
            hdct.setDonGia(item.getIdSanPhamChiTiet().getDonGia());
            hoaDonChiTietRepository.save(hdct);

            // Thanh toán online trừ kho ngay, COD trừ khi xác nhận (theo logic cũ của bạn)
            if (laThanhToanOnline) {
                SanPhamChiTiet spct = item.getIdSanPhamChiTiet();
                spct.setSoLuong(spct.getSoLuong() - item.getSoLuong());
                sanPhamChiTietRepository.save(spct);
            }
        }
        gioHangService.xoaTatCaGioHang(idKhachHang);
    }

    // ============================================================
    // CÁC HÀM QUẢN TRỊ (Giữ nguyên logic cũ)
    // ============================================================

    public List<TrangThaiHoaDon> getAllTrangThai() {
        return trangThaiHoaDonRepository.findAll();
    }

    public List<HoaDon> filterHoaDon(String tenKH, String trangThai, String loaiTT, Instant tuNgay, Instant denNgay) {
        return hoaDonRepository.filterHoaDon(tenKH, trangThai, loaiTT, tuNgay, denNgay);
    }

    public void capNhatTrangThai(Integer hoaDonId, Integer trangThaiIdMoi) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElseThrow();
        if (hoaDon.getIdTrangThaiHoaDon().getId() == ID_CHO_THANH_TOAN && trangThaiIdMoi == ID_DA_THANH_TOAN) {
            truSoLuongTonKho(hoaDonId);
        }
        hoaDon.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(trangThaiIdMoi).get());
        hoaDonRepository.save(hoaDon);
    }


    public List<HoaDon> layDonHangCuaKhach(Integer idKhachHang) {
        return hoaDonRepository.findByIdKhachHang_IdOrderByNgayTaoDesc(idKhachHang);
    }

    public void truSoLuongTonKho(Integer hoaDonId) {
        List<HoaDonChiTiet> listChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        for (HoaDonChiTiet chiTiet : listChiTiet) {
            SanPhamChiTiet spct = chiTiet.getIdSanPhamChiTiet();
            if (spct.getSoLuong() < chiTiet.getSoLuong()) {
                throw new RuntimeException("Sản phẩm ID " + spct.getId() + " không đủ tồn kho!");
            }
            spct.setSoLuong(spct.getSoLuong() - chiTiet.getSoLuong());
            sanPhamChiTietRepository.save(spct);
        }
    }
    // 1. Hàm xác nhận đơn hàng (Dùng cho ID 13)
    @Transactional
    public void xacnhanDonHang(Integer idHoaDon) {
        HoaDon hd = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Chỉ trừ kho cho đơn Tiền mặt (COD)
        if ("Tiền mặt".equalsIgnoreCase(hd.getIdLoaiThanhToan().getTenLoai())) {
            for (HoaDonChiTiet ct : hd.getHoaDonChiTiets()) {
                SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
                if (spct != null) {
                    spct.setSoLuong(spct.getSoLuong() - ct.getSoLuong());
                    sanPhamChiTietRepository.save(spct);
                }
            }
        }
        hd.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(13).get());
        hoaDonRepository.save(hd);
    }

    // 2. Hàm hủy đơn và hoàn kho + Voucher (Dùng cho ID 5)
    @Transactional
    public void huyDonHangVaHoanKho(Integer idHoaDon) {
        HoaDon hd = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        int idTTTruocKhiHuy = hd.getIdTrangThaiHoaDon().getId();
        String loaiTT = hd.getIdLoaiThanhToan().getTenLoai();

        boolean laDonOnline = "CK".equalsIgnoreCase(loaiTT);
        boolean daTruKhoCOD = (idTTTruocKhiHuy == 13); // Đã xác nhận mới hoàn

        if (laDonOnline || daTruKhoCOD) {
            // Hoàn sản phẩm
            for (HoaDonChiTiet ct : hd.getHoaDonChiTiets()) {
                SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
                if (spct != null) {
                    spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                    sanPhamChiTietRepository.save(spct);
                }
            }
            // KHÔI PHỤC VOUCHER TẠI ĐÂY
            if (hd.getIdMaGiamGia() != null) {
                var voucher = hd.getIdMaGiamGia();
                voucher.setSoLuong(voucher.getSoLuong() + 1);
                // Lưu ý: Tên repository mã giảm giá của bạn phải đúng
                // maGiamGiaRepository.save(voucher);
            }
        }
        hd.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(5).get());
        hoaDonRepository.save(hd);
    }
    public List<HoaDon> findAll() {
        return hoaDonRepository.findAll(Sort.by(Sort.Direction.DESC, "ngayTao"));
    }
}