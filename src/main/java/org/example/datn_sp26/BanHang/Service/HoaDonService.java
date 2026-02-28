package org.example.datn_sp26.BanHang.Service;

import jakarta.transaction.Transactional;
import org.example.datn_sp26.BanHang.Entity.*;
import org.example.datn_sp26.BanHang.Repository.HoaDonChiTietRepository;
import java.util.Arrays;
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
import java.util.stream.Collectors;

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
    // 🔥 HÀM TỔNG HỢP: LƯU CHI TIẾ́T + TRỪ KHO (CHỈ CHO SẢN PHẨM ĐƯỢC CHỌN)
    // ============================================================
    public void xuLyHoanTatHoaDon(Integer idKhachHang, HoaDon hoaDon, List<Integer> selectedIds) {
        var listGioHang = gioHangService.layGioHangCuaKhach(idKhachHang);

        if (listGioHang == null || listGioHang.isEmpty()) {
            System.out.println(">>> CẢNH BÁO: Giỏ hàng trống, không có gì để trừ kho.");
            return;
        }

        // Lọc chỉ lấy các sản phẩm được chọn
        var selectedItems = listGioHang.stream()
                .filter(i -> selectedIds == null || selectedIds.contains(i.getId()))
                .collect(Collectors.toList());

        for (var item : selectedItems) {
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

            // Gọi hàm kiểm tra: Nếu hết sạch các size/màu thì ẩn sản phẩm cha
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

        TrangThaiHoaDon trangThai = trangThaiHoaDonRepository.findByTenTrangThai("Chờ xác nhận")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái"));
        hoaDon.setIdTrangThaiHoaDon(trangThai);

        LoaiThanhToan loaiThanhToan = loaiThanhToanRepository.findByTenLoai("CK")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại thanh toán"));
        hoaDon.setIdLoaiThanhToan(loaiThanhToan);

        return hoaDonRepository.save(hoaDon);
    }

    public HoaDon taoHoaDonSauThanhToan(KhachHang khachHang, BigDecimal tongTienHang, String diaChiGiaoHang,
            BigDecimal phiShip) {
        if (diaChiGiaoHang == null || diaChiGiaoHang.isBlank()) {
            throw new RuntimeException("❌ Địa chỉ giao hàng không hợp lệ");
        }

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(taoMaHoaDon());
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setDiaChi(diaChiGiaoHang);
        hoaDon.setTongThanhToan(tongTienHang);

        TrangThaiHoaDon trangThai = trangThaiHoaDonRepository.findByTenTrangThai("Chờ xác nhận")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái"));
        hoaDon.setIdTrangThaiHoaDon(trangThai);

        LoaiThanhToan loaiThanhToan = loaiThanhToanRepository.findByTenLoai("CK")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại thanh toán"));
        hoaDon.setIdLoaiThanhToan(loaiThanhToan);

        return hoaDonRepository.saveAndFlush(hoaDon);
    }

    // ============================================================
    // 🔥 TẠO HÓA ĐƠN COD (Tiền mặt)
    // ============================================================
    public HoaDon taoHoaDonCOD(KhachHang khachHang, BigDecimal tongThanhToan, String diaChiGiaoHang) {
        if (diaChiGiaoHang == null || diaChiGiaoHang.isBlank()) {
            throw new RuntimeException("❌ Địa chỉ giao hàng không hợp lệ");
        }

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(taoMaHoaDon());
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setDiaChi(diaChiGiaoHang);
        hoaDon.setTongThanhToan(tongThanhToan);

        // Trạng thái: Chờ Thanh Toán (ID 11) — dành cho Tiền mặt
        TrangThaiHoaDon trangThai = trangThaiHoaDonRepository.findByTenTrangThai("Chờ Thanh Toán")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái Chờ Thanh Toán"));
        hoaDon.setIdTrangThaiHoaDon(trangThai);

        // Loại thanh toán: Tiền mặt
        LoaiThanhToan loaiThanhToan = loaiThanhToanRepository.findByTenLoai("Tiền mặt")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại thanh toán Tiền mặt"));
        hoaDon.setIdLoaiThanhToan(loaiThanhToan);

        return hoaDonRepository.saveAndFlush(hoaDon);
    }

    // ============================================================
    // 🔥 METHOD: TẠO HÓA ĐƠN VNPAY (ATOMIC - 1 TRANSACTION)
    // Gộp: tạo header + chi tiết + trừ kho + xóa sản phẩm đã chọn
    // ============================================================
    @Transactional
    public HoaDon taoHoaDonVNPay(KhachHang khachHang, BigDecimal tongThanhToan,
            String diaChiGiaoHang, BigDecimal phiShip, List<Integer> selectedIds) {
        // 1. Tạo HoaDon header
        HoaDon hoaDon = taoHoaDonSauThanhToan(khachHang, tongThanhToan, diaChiGiaoHang, phiShip);

        // 2. Tạo chi tiết + trừ kho (chỉ cho sản phẩm được chọn)
        xuLyHoanTatHoaDon(khachHang.getId(), hoaDon, selectedIds);

        // 3. Xóa CHỈ các sản phẩm đã mua khỏi giỏ hàng
        if (selectedIds != null && !selectedIds.isEmpty()) {
            gioHangService.xoaDanhSachSanPhamDaMua(selectedIds);
        } else {
            gioHangService.xoaTatCaGioHang(khachHang.getId());
        }

        return hoaDon;
    }

    // ============================================================
    // 🔥 METHOD: TẠO HÓA ĐƠN COD (ATOMIC - 1 TRANSACTION)
    // Gộp: tạo header + chi tiết + trừ kho + xóa sản phẩm đã chọn
    // ============================================================
    @Transactional
    public HoaDon taoHoaDonCODDayDu(KhachHang khachHang, BigDecimal tongThanhToan,
            String diaChiGiaoHang, List<Integer> selectedIds) {
        // 1. Tạo HoaDon header
        HoaDon hoaDon = taoHoaDonCOD(khachHang, tongThanhToan, diaChiGiaoHang);

        // 2. Tạo chi tiết + trừ kho (chỉ cho sản phẩm được chọn)
        xuLyHoanTatHoaDon(khachHang.getId(), hoaDon, selectedIds);

        // 3. Xóa CHỈ các sản phẩm đã mua khỏi giỏ hàng
        if (selectedIds != null && !selectedIds.isEmpty()) {
            gioHangService.xoaDanhSachSanPhamDaMua(selectedIds);
        } else {
            gioHangService.xoaTatCaGioHang(khachHang.getId());
        }

        return hoaDon;
    }

    public List<HoaDon> layDonHangCuaKhach(Integer idKhachHang) {
        return hoaDonRepository.findByKhachHangExcludeTest(idKhachHang);
    }

    public List<HoaDon> findAll() {
        return hoaDonRepository.findAll(org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "ngayTao"));
    }

    public List<HoaDon> filterHoaDon(String tenKH, String trangThai, String loaiTT, Instant tuNgay, Instant denNgay) {
        return hoaDonRepository.filterHoaDon(tenKH, trangThai, loaiTT, tuNgay, denNgay);
    }

    public List<TrangThaiHoaDon> getAllTrangThai() {
        return trangThaiHoaDonRepository.findAll();
    }

    // ===== Luồng trạng thái =====
    // CK / Ví: 1(Chờ xác nhận) → 13(Đã xác nhận) → 2(Đang xử lý) → 3(Đang giao) →
    // 4(Hoàn tất)
    // Tiền mặt: 11(Chờ Thanh Toán) → 14(Đã Thanh Toán) → 2(Đang xử lý) → 3(Đang
    // giao) → 4(Hoàn tất)
    // Đã hủy (5): chỉ được chọn khi đang ở 3 trạng thái đầu của mỗi luồng

    private static final List<Integer> CK_VI_FLOW = Arrays.asList(1, 13, 2, 3, 4);
    private static final List<Integer> TIEN_MAT_FLOW = Arrays.asList(11, 13, 2, 3, 4);
    private static final int ID_HUY = 5;
    private static final int ID_HOAN_TAT = 4;

    public void capNhatTrangThai(Integer hoaDonId, Integer trangThaiId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn #" + hoaDonId));
        TrangThaiHoaDon trangThaiMoi = trangThaiHoaDonRepository.findById(trangThaiId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái #" + trangThaiId));

        int currentId = hoaDon.getIdTrangThaiHoaDon().getId();
        int newId = trangThaiMoi.getId();

        // Đã hủy hoặc Hoàn tất → không cho đổi
        if (currentId == ID_HUY) {
            throw new RuntimeException("Hóa đơn đã hủy, không thể đổi trạng thái!");
        }
        if (currentId == ID_HOAN_TAT) {
            throw new RuntimeException("Hóa đơn đã hoàn tất, không thể đổi trạng thái!");
        }

        // Xác định luồng theo loại thanh toán
        String loaiTT = hoaDon.getIdLoaiThanhToan() != null
                ? hoaDon.getIdLoaiThanhToan().getTenLoai()
                : "";
        boolean isTienMat = "Tiền mặt".equalsIgnoreCase(loaiTT);
        List<Integer> flow = isTienMat ? TIEN_MAT_FLOW : CK_VI_FLOW;

        int currentIndex = flow.indexOf(currentId);

        // Nếu chọn Hủy
        if (newId == ID_HUY) {
            // Chỉ cho hủy khi đang ở 3 trạng thái đầu
            if (currentIndex >= 0 && currentIndex <= 2) {
                hoaDon.setIdTrangThaiHoaDon(trangThaiMoi);
                hoaDonRepository.save(hoaDon);
                return;
            } else {
                throw new RuntimeException("Không thể hủy hóa đơn ở trạng thái hiện tại!");
            }
        }

        // Kiểm tra trạng thái mới có trong luồng không
        int newIndex = flow.indexOf(newId);
        if (newIndex == -1) {
            throw new RuntimeException("Trạng thái không hợp lệ cho loại thanh toán này!");
        }

        // Chỉ cho tiến 1 bước
        if (currentIndex == -1 || newIndex != currentIndex + 1) {
            throw new RuntimeException("Chỉ được chuyển sang trạng thái tiếp theo (1 bước)!");
        }

        hoaDon.setIdTrangThaiHoaDon(trangThaiMoi);
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