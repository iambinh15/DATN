package org.example.datn_sp26.BanHang.Service;

import jakarta.transaction.Transactional;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.example.datn_sp26.BanHang.Entity.HoaDonChiTiet;
import org.example.datn_sp26.BanHang.Entity.LoaiThanhToan;
import org.example.datn_sp26.BanHang.Entity.TrangThaiHoaDon;
import org.example.datn_sp26.BanHang.Repository.HoaDonChiTietRepository;
import org.example.datn_sp26.BanHang.Repository.HoaDonRepository;
import org.example.datn_sp26.BanHang.Repository.LoaiThanhToanRepository;
import org.example.datn_sp26.BanHang.Repository.TrangThaiHoaDonRepository;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private TrangThaiHoaDonRepository trangThaiHoaDonRepository;

    @Autowired
    private LoaiThanhToanRepository loaiThanhToanRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    // Tiêm thêm GioHangService để lấy dữ liệu sản phẩm khách đang mua
    @Autowired
    private GioHangService gioHangService;

    // ============================================================
    // 🔥 HÀM TỔNG HỢP: LƯU CHI TIẾT + TRỪ KHO + XÓA GIỎ HÀNG
    // ============================================================
    public void xuLyHoanTatHoaDon(Integer idKhachHang, HoaDon hoaDon) {
        // 1. Lấy danh sách sản phẩm trong giỏ hàng của khách
        var listGioHang = gioHangService.layGioHangCuaKhach(idKhachHang);

        if (listGioHang == null || listGioHang.isEmpty()) {
            System.out.println(">>> CẢNH BÁO: Giỏ hàng trống, không có gì để trừ kho.");
            return;
        }

        for (var item : listGioHang) {
            // 2. Lưu vào Hóa Đơn Chi Tiết
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setIdHoaDon(hoaDon);
            hdct.setIdSanPhamChiTiet(item.getIdSanPhamChiTiet());
            hdct.setSoLuong(item.getSoLuong());
            hdct.setDonGia(item.getIdSanPhamChiTiet().getDonGia());
            hoaDonChiTietRepository.save(hdct);

            // 3. Trừ số lượng tồn kho trong Sản Phẩm Chi Tiết
            SanPhamChiTiet spct = item.getIdSanPhamChiTiet();
            int soLuongHienTai = spct.getSoLuong();
            int soLuongMua = item.getSoLuong();

            if (soLuongHienTai < soLuongMua) {
                throw new RuntimeException("Sản phẩm ID " + spct.getId() + " không đủ số lượng tồn kho!");
            }

            spct.setSoLuong(soLuongHienTai - soLuongMua);
            sanPhamChiTietRepository.save(spct);

            System.out.println(">>> Đã trừ SP ID: " + spct.getId() + " | Còn lại: " + spct.getSoLuong());
        }

        // 4. (Tùy chọn) Xóa giỏ hàng của khách sau khi đã thanh toán thành công
        // gioHangService.xoaTatCaGioHangCuaKhach(idKhachHang);
    }

    // ==========================================
    // TRỪ SỐ LƯỢNG KHO (Dành cho logic cần gọi lẻ)
    // ==========================================
    public void truSoLuongTonKho(Integer hoaDonId) {
        List<HoaDonChiTiet> listChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        System.out.println(">>> Tìm thấy " + listChiTiet.size() + " sản phẩm để trừ kho");

        for (HoaDonChiTiet chiTiet : listChiTiet) {
            SanPhamChiTiet spct = chiTiet.getIdSanPhamChiTiet();
            int soLuongMoi = spct.getSoLuong() - chiTiet.getSoLuong();
            spct.setSoLuong(soLuongMoi);
            sanPhamChiTietRepository.save(spct);
            System.out.println(">>> Đã trừ SP ID: " + spct.getId() + " còn: " + soLuongMoi);
        }
    }

    // ==========================================
    // CÁC HÀM CŨ (GIỮ NGUYÊN LOGIC)
    // ==========================================
    public HoaDon taoHoaDonSauThanhToan(KhachHang khachHang, BigDecimal tongThanhToan, NhanVien nhanVienDangNhap) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(taoMaHoaDon());
        hoaDon.setNhanVien(nhanVienDangNhap);
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setTongThanhToan(tongThanhToan);

        TrangThaiHoaDon trangThai = trangThaiHoaDonRepository.findByTenTrangThai("Chờ Thanh Toán")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái"));
        hoaDon.setIdTrangThaiHoaDon(trangThai);

        LoaiThanhToan loaiThanhToan = loaiThanhToanRepository.findByTenLoai("CK")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại thanh toán"));
        hoaDon.setIdLoaiThanhToan(loaiThanhToan);

        return hoaDonRepository.save(hoaDon);
    }

    public HoaDon taoHoaDonSauThanhToan(KhachHang khachHang, BigDecimal tongTienHang, String diaChiGiaoHang, BigDecimal phiShip) {
        if (diaChiGiaoHang == null || diaChiGiaoHang.isBlank()) {
            throw new RuntimeException("❌ Địa chỉ giao hàng không hợp lệ");
        }

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(taoMaHoaDon());
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setDiaChi(diaChiGiaoHang);
        hoaDon.setTongThanhToan(tongTienHang);

        TrangThaiHoaDon trangThai = trangThaiHoaDonRepository.findByTenTrangThai("Chờ Thanh Toán")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái"));
        hoaDon.setIdTrangThaiHoaDon(trangThai);

        LoaiThanhToan loaiThanhToan = loaiThanhToanRepository.findByTenLoai("CK")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại thanh toán"));
        hoaDon.setIdLoaiThanhToan(loaiThanhToan);

        return hoaDonRepository.saveAndFlush(hoaDon);
    }

    public List<HoaDon> layDonHangCuaKhach(Integer idKhachHang) {
        return hoaDonRepository.findByKhachHangExcludeTest(idKhachHang);
    }

    public List<HoaDon> findAll() {
        return hoaDonRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "ngayTao"));
    }

    public List<HoaDon> filterHoaDon(String tenKH, String trangThai, Instant tuNgay, Instant denNgay) {
        return hoaDonRepository.filterHoaDon(tenKH, trangThai, tuNgay, denNgay);
    }

    public List<TrangThaiHoaDon> getAllTrangThai() {
        return trangThaiHoaDonRepository.findAll();
    }

    public void capNhatTrangThai(Integer hoaDonId, Integer trangThaiId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn #" + hoaDonId));
        TrangThaiHoaDon trangThai = trangThaiHoaDonRepository.findById(trangThaiId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái #" + trangThaiId));
        hoaDon.setIdTrangThaiHoaDon(trangThai);
        hoaDonRepository.save(hoaDon);
    }

    private String taoMaHoaDon() {
        return "HD" + System.currentTimeMillis();
    }
}
