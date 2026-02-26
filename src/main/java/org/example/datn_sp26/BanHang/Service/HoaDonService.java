package org.example.datn_sp26.BanHang.Service;

import jakarta.transaction.Transactional;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.example.datn_sp26.BanHang.Entity.HoaDonChiTiet;
import org.example.datn_sp26.BanHang.Entity.LoaiThanhToan;
import org.example.datn_sp26.BanHang.Entity.TrangThaiHoaDon;
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

        // ===============================
        // HÀM CŨ (GIỮ NGUYÊN)
        // ===============================
        public HoaDon taoHoaDonSauThanhToan(
                        KhachHang khachHang,
                        BigDecimal tongThanhToan,
                        NhanVien nhanVienDangNhap) {

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

        // ===============================
        // 🔥 HÀM MỚI – KHÔNG CỘNG PHÍ SHIP
        // ===============================
        public HoaDon taoHoaDonSauThanhToan(
                        KhachHang khachHang,
                        BigDecimal tongTienHang,
                        String diaChiGiaoHang,
                        BigDecimal phiShip) {

                if (diaChiGiaoHang == null || diaChiGiaoHang.isBlank()) {
                        throw new RuntimeException("❌ Địa chỉ giao hàng không hợp lệ");
                }

                HoaDon hoaDon = new HoaDon();
                hoaDon.setMaHoaDon(taoMaHoaDon());
                hoaDon.setIdKhachHang(khachHang);
                hoaDon.setNgayTao(Instant.now());

                // ✅ LƯU ĐỊA CHỈ
                hoaDon.setDiaChi(diaChiGiaoHang);

                // ✅ CHỈ LƯU TIỀN HÀNG (KHÔNG CỘNG SHIP)
                hoaDon.setTongThanhToan(tongTienHang);

                TrangThaiHoaDon trangThai = trangThaiHoaDonRepository.findByTenTrangThai("Chờ Thanh Toán")
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái"));

                hoaDon.setIdTrangThaiHoaDon(trangThai);

                LoaiThanhToan loaiThanhToan = loaiThanhToanRepository.findByTenLoai("CK")
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại thanh toán"));

                hoaDon.setIdLoaiThanhToan(loaiThanhToan);

                return hoaDonRepository.saveAndFlush(hoaDon);
        }

        // ===============================
        // LẤY DANH SÁCH ĐƠN HÀNG (BỎ TRẠNG THÁI TEST)
        // ===============================
        public List<HoaDon> layDonHangCuaKhach(Integer idKhachHang) {
                return hoaDonRepository.findByKhachHangExcludeTest(idKhachHang);
        }

        // ===============================
        // ADMIN – DANH SÁCH + LỌC HÓA ĐƠN
        // ===============================
        public List<HoaDon> findAll() {
                return hoaDonRepository.findAll(
                                org.springframework.data.domain.Sort.by(
                                                org.springframework.data.domain.Sort.Direction.DESC, "ngayTao"));
        }

        public List<HoaDon> filterHoaDon(String tenKH, String trangThai,
                        Instant tuNgay, Instant denNgay) {
                return hoaDonRepository.filterHoaDon(tenKH, trangThai, tuNgay, denNgay);
        }

        public List<TrangThaiHoaDon> getAllTrangThai() {
                return trangThaiHoaDonRepository.findByTenTrangThaiNot("test");
        }

        // ===============================================
        // LUỒNG TRẠNG THÁI THEO LOẠI THANH TOÁN
        // ===============================================

        // --- COD (Tiền mặt) ---
        // Chờ xác nhận(1) → Đã xác nhận(13) [TRỪ KHO] → Đang xử lý(2) → Đang giao(3) →
        // Hoàn tất(4) [THỐNG KÊ]
        private static final Map<Integer, Integer> THU_TU_COD = Map.of(
                        1, 0, // Chờ xác nhận
                        13, 1, // Đã xác nhận (TRỪ KHO)
                        2, 2, // Đang xử lý
                        3, 3, // Đang giao
                        4, 4 // Hoàn tất (THỐNG KÊ)
        );

        // --- VN Pay (CK) ---
        // Chờ thanh toán(12) → Đã thanh toán(14) [TRỪ KHO + THỐNG KÊ] → Đang xử lý(2) →
        // Đang giao(3) → Hoàn tất(4)
        // ID 11 (Chờ Thanh Toán cũ) cũng được hỗ trợ cho đơn hàng cũ
        private static final Map<Integer, Integer> THU_TU_VNPAY = Map.of(
                        11, 0, // Chờ Thanh Toán (CŨ - hỗ trợ đơn cũ)
                        12, 0, // Chờ thanh toán
                        14, 1, // Đã thanh toán (TRỪ KHO + THỐNG KÊ)
                        2, 2, // Đang xử lý
                        3, 3, // Đang giao
                        4, 4 // Hoàn tất
        );

        private static final int ID_HUY = 5;

        // Bước bắt đầu trừ kho: COD = Đã xác nhận (13), VN Pay = Đã thanh toán (14)
        private static final int ID_DA_XAC_NHAN = 13;
        private static final int ID_DA_THANH_TOAN = 14;

        // NV hủy được khi đang ở các trạng thái này
        private static final Set<Integer> COD_HUY_DUOC = Set.of(1, 13, 2);
        private static final Set<Integer> VNPAY_HUY_DUOC = Set.of(11, 12, 14, 2);

        // Các trạng thái đã qua bước trừ kho (cần rollback nếu hủy)
        private static final Set<Integer> COD_DA_TRU_KHO = Set.of(13, 2, 3);
        private static final Set<Integer> VNPAY_DA_TRU_KHO = Set.of(14, 2, 3);

        // ===============================
        // ADMIN – CẬP NHẬT TRẠNG THÁI HÓA ĐƠN
        // ===============================
        public void capNhatTrangThai(Integer hoaDonId, Integer trangThaiId) {
                HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn #" + hoaDonId));
                TrangThaiHoaDon trangThaiMoi = trangThaiHoaDonRepository.findById(trangThaiId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái #" + trangThaiId));

                int idHienTai = hoaDon.getIdTrangThaiHoaDon().getId();

                // Xác định loại thanh toán → chọn đúng luồng
                String loaiTT = hoaDon.getIdLoaiThanhToan() != null
                                ? hoaDon.getIdLoaiThanhToan().getTenLoai()
                                : "";
                boolean isCOD = "Tiền mặt".equalsIgnoreCase(loaiTT);
                Map<Integer, Integer> thuTu = isCOD ? THU_TU_COD : THU_TU_VNPAY;

                // === XỬ LÝ HỦY ===
                if (trangThaiId == ID_HUY) {
                        Set<Integer> huyDuoc = isCOD ? COD_HUY_DUOC : VNPAY_HUY_DUOC;
                        if (!huyDuoc.contains(idHienTai)) {
                                throw new RuntimeException("Không thể hủy đơn ở trạng thái hiện tại!");
                        }
                        // Rollback kho nếu đã trừ
                        Set<Integer> daTruKho = isCOD ? COD_DA_TRU_KHO : VNPAY_DA_TRU_KHO;
                        if (daTruKho.contains(idHienTai)) {
                                rollbackKho(hoaDon);
                        }
                        hoaDon.setIdTrangThaiHoaDon(trangThaiMoi);
                        hoaDonRepository.save(hoaDon);
                        return;
                }

                // === XỬ LÝ CHUYỂN TIẾN ===
                Integer orderHienTai = thuTu.get(idHienTai);
                Integer orderMoi = thuTu.get(trangThaiId);

                if (orderHienTai == null || orderMoi == null) {
                        throw new RuntimeException("Trạng thái không hợp lệ cho loại thanh toán này!");
                }
                if (orderMoi <= orderHienTai) {
                        throw new RuntimeException("Không thể chuyển trạng thái lùi lại!");
                }

                // Trừ kho khi chuyển sang bước TRỪ KHO
                if ((isCOD && trangThaiId == ID_DA_XAC_NHAN)
                                || (!isCOD && trangThaiId == ID_DA_THANH_TOAN)) {
                        truKho(hoaDon);
                }

                hoaDon.setIdTrangThaiHoaDon(trangThaiMoi);
                hoaDonRepository.save(hoaDon);
        }

        // ===============================
        // TRỪ KHO – Trừ số lượng SanPhamChiTiet
        // ===============================
        private void truKho(HoaDon hoaDon) {
                for (HoaDonChiTiet ct : hoaDon.getHoaDonChiTiets()) {
                        SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
                        int soLuongMua = ct.getSoLuong();
                        if (spct.getSoLuong() < soLuongMua) {
                                throw new RuntimeException("Không đủ số lượng tồn kho cho sản phẩm #" + spct.getId());
                        }
                        spct.setSoLuong(spct.getSoLuong() - soLuongMua);
                        sanPhamChiTietRepository.save(spct);
                }
        }

        // ===============================
        // ROLLBACK KHO – Cộng lại số lượng khi hủy
        // ===============================
        private void rollbackKho(HoaDon hoaDon) {
                for (HoaDonChiTiet ct : hoaDon.getHoaDonChiTiets()) {
                        SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
                        spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                        sanPhamChiTietRepository.save(spct);
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
