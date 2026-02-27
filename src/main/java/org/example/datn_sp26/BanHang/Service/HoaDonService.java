package org.example.datn_sp26.BanHang.Service;

import jakarta.transaction.Transactional;
import org.example.datn_sp26.BanHang.Entity.*;
import org.example.datn_sp26.BanHang.Repository.HoaDonChiTietRepository;
import org.example.datn_sp26.BanHang.Repository.HoaDonRepository;
import org.example.datn_sp26.BanHang.Repository.LoaiThanhToanRepository;
import org.example.datn_sp26.BanHang.Repository.TrangThaiHoaDonRepository;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.example.datn_sp26.SanPham.Service.SanPhamChiTietService; // Import service mới
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private GioHangService gioHangService;

    // 1. Tiêm thêm SanPhamChiTietService để dùng hàm kiểm tra trạng thái
    @Autowired
    private SanPhamChiTietService sanPhamChiTietService;

    // ============================================================
    // 🔥 HÀM TỔNG HỢP: LƯU CHI TIẾT + TRỪ KHO + XÓA GIỎ HÀNG
    // ============================================================
    public void xuLyHoanTatHoaDon(Integer idKhachHang, HoaDon hoaDon) {
        var listGioHang = gioHangService.layGioHangCuaKhach(idKhachHang);

        if (listGioHang == null || listGioHang.isEmpty()) {
            System.out.println(">>> CẢNH BÁO: Giỏ hàng trống, không có gì để trừ kho.");
            return;
        }

        for (var item : listGioHang) {
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setIdHoaDon(hoaDon);
            hdct.setIdSanPhamChiTiet(item.getIdSanPhamChiTiet());
            hdct.setSoLuong(item.getSoLuong());
            hdct.setDonGia(item.getIdSanPhamChiTiet().getDonGia());
            hoaDonChiTietRepository.save(hdct);

            SanPhamChiTiet spct = item.getIdSanPhamChiTiet();
            int soLuongHienTai = spct.getSoLuong();
            int soLuongMua = item.getSoLuong();

            if (soLuongHienTai < soLuongMua) {
                throw new RuntimeException("Sản phẩm ID " + spct.getId() + " không đủ số lượng tồn kho!");
            }

            spct.setSoLuong(soLuongHienTai - soLuongMua);
            sanPhamChiTietRepository.save(spct);

            // 2. Gọi hàm kiểm tra: Nếu hết sạch các size/màu thì ẩn sản phẩm cha
            sanPhamChiTietService.checkAndDisableSanPham(spct.getIdSanPham().getId());

            System.out.println(">>> Đã trừ SP ID: " + spct.getId() + " | Còn lại: " + spct.getSoLuong());
        }
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

            // 3. Gọi hàm kiểm tra tại đây để đảm bảo đồng bộ
            sanPhamChiTietService.checkAndDisableSanPham(spct.getIdSanPham().getId());

            System.out.println(">>> Đã trừ SP ID: " + spct.getId() + " còn: " + soLuongMoi);
        }
    }

    // ==========================================
    // CÁC HÀM CŨ (GIỮ NGUYÊN HOÀN TOÀN LOGIC)
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
    @Transactional
    public void thanhToan(HoaDon hoaDon, List<GioHangChiTiet> listGioHang) {
        // Lưu hóa đơn trước
        HoaDon hdonSaved = hoaDonRepository.save(hoaDon);

        for (GioHangChiTiet item : listGioHang) {
            // 1. Lấy thông tin SPCT mới nhất từ DB để kiểm tra tồn kho thực tế
            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(item.getIdSanPhamChiTiet().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

            // 2. KIỂM TRA TỒN KHO: Nếu số lượng mua > tồn kho thực tế
            if (item.getSoLuong() > spct.getSoLuong()) {
                throw new RuntimeException("Sản phẩm [" + spct.getIdSanPham().getTenSanPham() +
                        "] chỉ còn " + spct.getSoLuong() + " sản phẩm. Vui lòng cập nhật giỏ hàng!");
            }

            // 3. Trừ kho
            int soLuongConLai = spct.getSoLuong() - item.getSoLuong();
            spct.setSoLuong(soLuongConLai);

            // Tự động ngưng hoạt động nếu hết hàng
            if (soLuongConLai == 0) {
                spct.setTrangThai(0);
            }
            sanPhamChiTietRepository.save(spct);

            // 4. Lưu vào Hóa đơn chi tiết
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setIdHoaDon(hdonSaved);
            hdct.setIdSanPhamChiTiet(spct);
            hdct.setSoLuong(item.getSoLuong());
            hdct.setDonGia(spct.getDonGia());

            hoaDonChiTietRepository.save(hdct);
        }
    }
    private String taoMaHoaDon() {
        return "HD" + System.currentTimeMillis();
    }
    public Map<String, Object> thongKeHoanThanh() {

        Double tongDoanhThu = hoaDonRepository.sumDoanhThuHoanThanh();
        Long soHoaDon = hoaDonRepository.countHoaDonHoanThanh();

        Map<String, Object> result = new HashMap<>();
        result.put("tongDoanhThu", tongDoanhThu);
        result.put("soHoaDonHoanThanh", soHoaDon);

        return result;
    }
}