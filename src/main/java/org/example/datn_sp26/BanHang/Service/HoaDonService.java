package org.example.datn_sp26.BanHang.Service;

import jakarta.transaction.Transactional;
import org.example.datn_sp26.BanHang.Entity.*;
import org.example.datn_sp26.BanHang.Repository.*;
import org.example.datn_sp26.KhuyenMai.Entity.MaGiamGia;
import org.example.datn_sp26.KhuyenMai.Repository.MaGiamGiaRepository;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Repository.KhachHangRepository;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.example.datn_sp26.SanPham.Service.SanPhamChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class HoaDonService {
    @Autowired
    private KhachHangRepository khachHangRepository;
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
    @Autowired
    private SanPhamChiTietService sanPhamChiTietService;
    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;
    private static final int ID_DANG_GIAO = 3;
    private static final int ID_CHO_XAC_NHAN = 1;
    private static final int ID_CHO_THANH_TOAN = 11;
    private static final int ID_DA_THANH_TOAN = 14;
    private static final int ID_GIAO_THAT_BAI = 15;
    // Thông tin ngân hàng cho QR Code chuyển khoản tại quầy
    private static final String BANK_ID = "MB";
    private static final String BANK_ACCOUNT = "0946073693";
    private static final String BANK_ACCOUNT_NAME = "Truong Van Thien";

    // ============================================================
    // LOGIC XỬ LÝ VOUCHER (Đã thêm logic trừ tiền)
    // ============================================================
    private void xuLyVoucher(HoaDon hoaDon, String maVoucher, BigDecimal phiShip) {
        if (maVoucher == null || maVoucher.trim().isEmpty()) return;

        MaGiamGia voucher = maGiamGiaRepository.findByMa(maVoucher.trim()).orElse(null);
        if (voucher == null || voucher.getSoLuong() <= 0) return;

        // 1. Lấy Tổng thanh toán hiện tại (đã bao gồm ship)
        BigDecimal tongHienTai = hoaDon.getTongThanhToan();
        BigDecimal tienHang = tongHienTai.subtract(phiShip);
        if (tienHang.compareTo(BigDecimal.ZERO) < 0) tienHang = BigDecimal.ZERO;

        // 2. Chuyển đổi giá trị Voucher an toàn (từ Double sang BigDecimal)
        BigDecimal giaTriVoucher = (voucher.getGiaTri() != null)
                ? BigDecimal.valueOf(voucher.getGiaTri())
                : BigDecimal.ZERO;

        // 3. ÉP KIỂU LOAI GIAM (Quan trọng nhất)
        // Nếu loaiGiam là null, mặc định về 0 (Tiền mặt)
        int loaiGiamThucTe = (voucher.getLoaiGiam() != null) ? voucher.getLoaiGiam() : 0;

        BigDecimal soTienGiam = BigDecimal.ZERO;

        // LOG ĐỂ BẮT BỆNH
        System.out.println("---------- DEBUG VOUCHER ----------");
        System.out.println("Ma Voucher: " + voucher.getMa());
        System.out.println("Loai Giam (Thực tế): " + loaiGiamThucTe);
        System.out.println("Gia Tri Voucher: " + giaTriVoucher);
        System.out.println("Tiền hàng gốc: " + tienHang);

        if (loaiGiamThucTe == 0) {
            // LOGIC GIẢM TIỀN MẶT
            soTienGiam = giaTriVoucher;
            System.out.println("=> Đang áp dụng: GIẢM TIỀN MẶT");
        } else {
            // LOGIC GIẢM %
            soTienGiam = tienHang.multiply(giaTriVoucher)
                    .divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);
            System.out.println("=> Đang áp dụng: GIẢM PHẦN TRĂM");
        }

        // 4. Ràng buộc: Không giảm quá tiền hàng
        if (soTienGiam.compareTo(tienHang) > 0) {
            soTienGiam = tienHang;
        }

        // 5. Tính tổng cuối cùng
        BigDecimal tongCuoi = tienHang.subtract(soTienGiam).add(phiShip);

        // Cập nhật lại hóa đơn
        hoaDon.setIdMaGiamGia(voucher);
        hoaDon.setTongThanhToan(tongCuoi);

        // Lưu voucher (giảm số lượng)
        voucher.setSoLuong(voucher.getSoLuong() - 1);
        maGiamGiaRepository.save(voucher);

        System.out.println("Số tiền được giảm: " + soTienGiam);
        System.out.println("Tổng thanh toán mới: " + tongCuoi);
        System.out.println("------------------------------------");
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
    public HoaDon taoHoaDonVNPay(KhachHang khachHang, BigDecimal tongThanhToan, String diaChi, BigDecimal phiShip,
                                 String maVoucher) {

        HoaDon hoaDon = new HoaDon();
        // Tạo mã hóa đơn duy nhất kèm theo timestamp của VNPay
        hoaDon.setMaHoaDon("HD_VNP" + System.currentTimeMillis());
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setDiaChi(diaChi);
        // 1️⃣ Gán THẲNG tổng tiền từ VNPay trả về
        // Con số này đã được Controller tính toán (TienHang - Voucher + PhiShip)
        // Nên tuyệt đối không gọi hàm xuLyVoucher() ở đây nữa.
        hoaDon.setTongThanhToan(tongThanhToan);

        // 2️⃣ Lưu vết Mã giảm giá để hiển thị lên giao diện "Đơn hàng của tôi"
        if (maVoucher != null && !maVoucher.isEmpty()) {
            maGiamGiaRepository.findByMa(maVoucher).ifPresent(voucher -> {
                // Chỉ set đối tượng để lưu quan hệ database (Foreign Key)
                // Việc gán này không làm thay đổi tongThanhToan đã set ở trên
                hoaDon.setIdMaGiamGia(voucher);
            });
        }

        // 3️⃣ Gán trạng thái và loại thanh toán
        hoaDon.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(ID_CHO_XAC_NHAN).get());
        hoaDon.setIdLoaiThanhToan(loaiThanhToanRepository.findByTenLoai("CK").get());

        // 4️⃣ Lưu hóa đơn
        hoaDonRepository.save(hoaDon);

        // 5️⃣ Lưu chi tiết đơn & trừ kho ngay (vì thanh toán online thành công mới vào đây)
        luuChiTietVaTruKho(hoaDon, khachHang.getId(), true);

        return hoaDon;
    }

    // ============================================================
    // THANH TOÁN CODtao
    // ============================================================


    @Transactional
    public HoaDon taoHoaDonCODDayDu(KhachHang khachHang,
                                    BigDecimal tongThanhToan,
                                    String diaChi,
                                    String maVoucher,
                                    BigDecimal phiShip) {

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon("HD_COD" + System.currentTimeMillis());
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setDiaChi(diaChi);

        // 1. Thiết lập trạng thái ban đầu: "Chờ xác nhận" (ID = 1)
        hoaDon.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(ID_CHO_XAC_NHAN)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy trạng thái ID " + ID_CHO_XAC_NHAN)));

        // 2. Thiết lập loại thanh toán: "Tiền mặt"
        hoaDon.setIdLoaiThanhToan(loaiThanhToanRepository.findByTenLoai("Tiền mặt")
                .orElseThrow(() -> new RuntimeException("Lỗi: Chưa cấu hình phương thức Tiền mặt")));

        // 3. QUAN TRỌNG: Gán số tiền tạm tính vào Hóa đơn trước khi xử lý Voucher
        // tongThanhToan này là giá trị truyền từ FE (gồm Tiền hàng + Ship)
        hoaDon.setTongThanhToan(tongThanhToan);

        // 4. Áp dụng Voucher (Sử dụng hàm logic chuẩn ở trên)
        xuLyVoucher(hoaDon, maVoucher, phiShip);

        // 5. Lưu hóa đơn để lấy ID (dùng saveAndFlush để đảm bảo ID có ngay)
        HoaDon savedHoaDon = hoaDonRepository.saveAndFlush(hoaDon);

        // 6. Lưu chi tiết hóa đơn (HoaDonChiTiet) và Xử lý giỏ hàng
        // Đơn COD: false (không trừ kho ngay, sẽ trừ khi chuyển sang trạng thái "Xác nhận")
        luuChiTietVaTruKho(savedHoaDon, khachHang.getId(), false);

        return savedHoaDon;
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

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        Integer trangThaiHienTai = hoaDon.getIdTrangThaiHoaDon().getId();

        // ✅ Nếu đơn COD đang "Chờ xác nhận" và chuyển sang "Đã xác nhận"
        if (trangThaiHienTai.equals(ID_CHO_XAC_NHAN)
                && trangThaiIdMoi.equals(13)) { // 13 = Đã xác nhận

            // Chỉ trừ kho nếu là Tiền mặt (COD)
            if ("Tiền mặt".equalsIgnoreCase(hoaDon.getIdLoaiThanhToan().getTenLoai())) {
                truSoLuongTonKho(hoaDonId);
            }
        }

        // ✅ THÊM ĐOẠN NÀY: Nếu chuyển sang Giao thất bại
        if (trangThaiIdMoi.equals(15)) { // 15 = Giao thất bại

            // Hoàn lại số lượng sản phẩm
            for (HoaDonChiTiet ct : hoaDon.getHoaDonChiTiets()) {
                SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
                if (spct != null) {
                    spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                    sanPhamChiTietRepository.save(spct);
                }
            }

            // Hoàn lại voucher
            if (hoaDon.getIdMaGiamGia() != null) {
                MaGiamGia voucher = hoaDon.getIdMaGiamGia();
                voucher.setSoLuong(voucher.getSoLuong() + 1);
                maGiamGiaRepository.save(voucher);
            }
        }

        // Cập nhật trạng thái
        hoaDon.setIdTrangThaiHoaDon(
                trangThaiHoaDonRepository.findById(trangThaiIdMoi).get());

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

    // 2. Hàm hủy đơn và hoàn kho + Voucher
    @Transactional
    public void huyDonHangVaHoanKho(Integer idHoaDon) {
        huyDonHangVaHoanKho(idHoaDon, null);
    }

    // Overload: hủy đơn với lý do hủy (dùng cho khách hàng)
    @Transactional
    public void huyDonHangVaHoanKho(Integer idHoaDon, String lyDoHuy) {
        HoaDon hd = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        int idTTTruocKhiHuy = hd.getIdTrangThaiHoaDon().getId();
        String loaiTT = hd.getIdLoaiThanhToan().getTenLoai();

        boolean laDonOnline = "CK".equalsIgnoreCase(loaiTT);
        boolean daTruKhoCOD = (idTTTruocKhiHuy == 13);

        if (laDonOnline || daTruKhoCOD) {
            // Hoàn sản phẩm
            for (HoaDonChiTiet ct : hd.getHoaDonChiTiets()) {
                SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
                if (spct != null) {
                    spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                    sanPhamChiTietRepository.save(spct);
                }
            }
            // 1. Kiểm tra nếu hóa đơn có gắn mã giảm giá
            if (hd.getIdMaGiamGia() != null) {
                // Lấy đối tượng Voucher từ hóa đơn
                var voucher = hd.getIdMaGiamGia();

                // Tăng số lượng lên 1
                int soLuongMoi = voucher.getSoLuong() + 1;
                voucher.setSoLuong(soLuongMoi);

                // QUAN TRỌNG: Bạn phải gọi repository của Mã Giảm Giá để lưu
                // Thay 'maGiamGiaRepo' bằng tên biến Repository mã giảm giá của bạn
                maGiamGiaRepository.save(voucher);

                System.out.println("Đã hoàn voucher: " + voucher.getMa() + " - Số lượng mới: " + soLuongMoi);
            }
        }
        hd.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(5).get());
        if (lyDoHuy != null && !lyDoHuy.trim().isEmpty()) {
            hd.setLyDoHuy(lyDoHuy.trim());
        }
        hoaDonRepository.save(hd);
    }

    public List<HoaDon> findAll() {
        return hoaDonRepository.findAll(Sort.by(Sort.Direction.DESC, "ngayTao"));
    }

    // ============================================================
    // BÁN HÀNG TẠI QUẦY (POS)
    // ============================================================

    private static final int ID_HOAN_TAT = 4;

    /**
     * Tạo hóa đơn trống tại quầy với trạng thái "Chờ thanh toán"
     */
    @Transactional
    public HoaDon taoHoaDonTaiQuay(NhanVien nhanVien) {

        // Đếm số hóa đơn đang chờ thanh toán
        long soLuongCho = hoaDonRepository
                .countByIdTrangThaiHoaDon_Id(ID_CHO_THANH_TOAN);

        if (soLuongCho >= 10) {
            throw new RuntimeException("Chỉ được tối đa 10 hóa đơn chờ");
        }

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon("HD_POS" + System.currentTimeMillis());
        hoaDon.setNhanVien(nhanVien);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setTongThanhToan(BigDecimal.ZERO);
        hoaDon.setIdTrangThaiHoaDon(
                trangThaiHoaDonRepository.findById(ID_CHO_THANH_TOAN).get());

        return hoaDonRepository.save(hoaDon);
    }
    @Transactional
    public void themSanPhamVaoHoaDon(Integer hoaDonId, Integer spctId, Integer soLuong) {

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(spctId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

        if (spct.getSoLuong() < soLuong) {
            throw new RuntimeException("Sản phẩm không đủ số lượng tồn kho!");
        }
        Optional<HoaDonChiTiet> existing = hoaDonChiTietRepository.findByIdHoaDonAndIdSanPhamChiTiet(hoaDon, spct);

        if (existing.isPresent()) {
            HoaDonChiTiet hdct = existing.get();
            hdct.setSoLuong(hdct.getSoLuong() + soLuong);
            hoaDonChiTietRepository.save(hdct);
        } else {
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setIdHoaDon(hoaDon);
            hdct.setIdSanPhamChiTiet(spct);
            hdct.setSoLuong(soLuong);
            hdct.setDonGia(spct.getDonGia());
            hoaDonChiTietRepository.save(hdct);
        }

        // Trừ kho NGAY khi thêm vào giỏ hàng POS
        spct.setSoLuong(spct.getSoLuong() - soLuong);
        sanPhamChiTietRepository.save(spct);

        capNhatTongTien(hoaDonId);
    }
    @Transactional
    public void xoaSanPhamKhoiHoaDon(Integer hoaDonChiTietId) {

        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(hoaDonChiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));

        Integer hoaDonId = hdct.getIdHoaDon().getId();
        SanPhamChiTiet spct = hdct.getIdSanPhamChiTiet();

        // Hoàn trả lại kho khi xóa sản phẩm khỏi giỏ POS
        spct.setSoLuong(spct.getSoLuong() + hdct.getSoLuong());
        sanPhamChiTietRepository.save(spct);

        hoaDonChiTietRepository.delete(hdct);
        capNhatTongTien(hoaDonId);
    }

    @Transactional
    public void capNhatSoLuongChiTiet(Integer hdctId, Integer soLuong) {
        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(hdctId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));

        if (soLuong <= 0) {
            xoaSanPhamKhoiHoaDon(hdctId);
            return;
        }

        SanPhamChiTiet spct = hdct.getIdSanPhamChiTiet();
        int soLuongCu = hdct.getSoLuong();
        int chenhLech = soLuong - soLuongCu; // dương = tăng thêm, âm = giảm bớt

        // Nếu tăng số lượng → kiểm tra tồn kho đủ không
        if (chenhLech > 0 && spct.getSoLuong() < chenhLech) {
            throw new RuntimeException(
                    "Sản phẩm không đủ số lượng tồn kho! Chỉ còn " + spct.getSoLuong() + " sản phẩm.");
        }

        // Cập nhật số lượng trong giỏ
        hdct.setSoLuong(soLuong);
        hoaDonChiTietRepository.save(hdct);

        // Điều chỉnh kho: trừ nếu tăng, hoàn nếu giảm
        spct.setSoLuong(spct.getSoLuong() - chenhLech);
        sanPhamChiTietRepository.save(spct);

        capNhatTongTien(hdct.getIdHoaDon().getId());
    }
    @Transactional
    public void thanhToanTaiQuay(Integer hoaDonId, Integer khachHangId, Integer voucherId,
            String phuongThuc, BigDecimal tienKhachDua) {

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        if (khachHangId != null) {
            KhachHang kh = khachHangRepository.findById(khachHangId).orElse(null);
            if (kh != null) {
                hoaDon.setIdKhachHang(kh);
                // Lấy địa chỉ đầu tiên của khách hàng (nếu có)
                if (kh.getDanhSachDiaChi() != null && !kh.getDanhSachDiaChi().isEmpty()) {
                    hoaDon.setDiaChi(kh.getDanhSachDiaChi().get(0).getDiaChi());
                }
            }
        }
        hoaDon.setIdLoaiThanhToan(loaiThanhToanRepository.findByTenLoai(phuongThuc).get());
        capNhatTongTien(hoaDonId);
        hoaDon = hoaDonRepository.findById(hoaDonId).get(); // reload
        if (voucherId != null) {
            MaGiamGia voucher = maGiamGiaRepository.findById(voucherId).orElse(null);
            if (voucher != null) {
                hoaDon.setIdMaGiamGia(voucher);
                BigDecimal giaTriGiam = BigDecimal.valueOf(voucher.getGiaTri());
                BigDecimal tongHienTai = hoaDon.getTongThanhToan();
                BigDecimal soTienTru;

                if (voucher.getLoaiGiam() == 0) {
                    soTienTru = giaTriGiam;
                } else {
                    soTienTru = tongHienTai.multiply(giaTriGiam).divide(BigDecimal.valueOf(100));
                }

                BigDecimal tongSauGiam = tongHienTai.subtract(soTienTru);
                if (tongSauGiam.compareTo(BigDecimal.ZERO) < 0)
                    tongSauGiam = BigDecimal.ZERO;
                hoaDon.setTongThanhToan(tongSauGiam);

                // Trừ số lượng voucher
                voucher.setSoLuong(voucher.getSoLuong() - 1);
                maGiamGiaRepository.save(voucher);
            }
        }
        if ("Tiền mặt".equalsIgnoreCase(phuongThuc)) {
            hoaDon.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(ID_HOAN_TAT).get());
        } else {
        }

        hoaDonRepository.save(hoaDon);
    }
    @Transactional
    public void xacNhanChuyenKhoan(Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        if (hoaDon.getIdTrangThaiHoaDon().getId() != ID_CHO_THANH_TOAN) {
            throw new RuntimeException("Hóa đơn không ở trạng thái chờ xác nhận chuyển khoản!");
        }
        hoaDon.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(ID_HOAN_TAT).get());
        hoaDonRepository.save(hoaDon);
    }


    private void truKhoTaiQuay(Integer hoaDonId) {
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        for (HoaDonChiTiet ct : chiTietList) {
            SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
            if (spct.getSoLuong() < ct.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + spct.getId() + " không đủ tồn kho!");
            }
            spct.setSoLuong(spct.getSoLuong() - ct.getSoLuong());
            sanPhamChiTietRepository.save(spct);
        }
    }


    public String taoQRUrl(Integer hoaDonId, Integer voucherId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Tính tổng tiền
        capNhatTongTien(hoaDonId);
        hoaDon = hoaDonRepository.findById(hoaDonId).get();
        BigDecimal tongTien = hoaDon.getTongThanhToan();

        // Áp voucher vào tính toán (chỉ preview, chưa lưu)
        if (voucherId != null) {
            MaGiamGia voucher = maGiamGiaRepository.findById(voucherId).orElse(null);
            if (voucher != null) {
                BigDecimal giaTriGiam = BigDecimal.valueOf(voucher.getGiaTri());
                BigDecimal soTienTru;
                if (voucher.getLoaiGiam() == 0) {
                    soTienTru = giaTriGiam;
                } else {
                    soTienTru = tongTien.multiply(giaTriGiam).divide(BigDecimal.valueOf(100));
                }
                tongTien = tongTien.subtract(soTienTru);
                if (tongTien.compareTo(BigDecimal.ZERO) < 0)
                    tongTien = BigDecimal.ZERO;
            }
        }

        String amount = tongTien.toBigInteger().toString();
        String addInfo = URLEncoder.encode("Thanh toan " + hoaDon.getMaHoaDon(), StandardCharsets.UTF_8);

        return "https://img.vietqr.io/image/" + BANK_ID + "-" + BANK_ACCOUNT
                + "-compact.png?amount=" + amount
                + "&addInfo=" + addInfo
                + "&accountName=" + URLEncoder.encode(BANK_ACCOUNT_NAME, StandardCharsets.UTF_8);
    }


    public List<HoaDon> layDsHoaDonTaiQuay() {
        return hoaDonRepository.findByIdTrangThaiHoaDon_IdOrderByNgayTaoDesc(ID_CHO_THANH_TOAN);
    }
    private void capNhatTongTien(Integer hoaDonId) {
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        BigDecimal tongTien = BigDecimal.ZERO;
        for (HoaDonChiTiet ct : chiTietList) {
            tongTien = tongTien.add(ct.getDonGia().multiply(BigDecimal.valueOf(ct.getSoLuong())));
        }
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).get();
        hoaDon.setTongThanhToan(tongTien);
        hoaDonRepository.save(hoaDon);
    }
    public List<HoaDonChiTiet> layChiTietHoaDon(Integer hoaDonId) {
        return hoaDonChiTietRepository.findByHoaDonIdWithDetails(hoaDonId);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void tuDongHuyHoaDonQuaHan() {

        Instant gioiHan = Instant.now().minusSeconds(600);

        List<HoaDon> danhSach = hoaDonRepository
                .findByIdTrangThaiHoaDon_IdAndNgayTaoBefore(
                        ID_CHO_THANH_TOAN,
                        gioiHan);   

        for (HoaDon hd : danhSach) {
            List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(hd.getId());
            for (HoaDonChiTiet ct : chiTietList) {
                SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
                spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                sanPhamChiTietRepository.save(spct);
            }

            hd.setIdTrangThaiHoaDon(
                    trangThaiHoaDonRepository.findById(5).get()
            );

            hoaDonRepository.save(hd);

            System.out.println("===> Đã tự động hủy hóa đơn ID: " + hd.getId() + " và hoàn trả kho.");
        }
    }
    @Transactional
    public void giaoThatBai(Integer idHoaDon) {
        HoaDon hd = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        int idTTTruocKhiHuy = hd.getIdTrangThaiHoaDon().getId();
        String loaiTT = hd.getIdLoaiThanhToan().getTenLoai();

        boolean laDonOnline = "CK".equalsIgnoreCase(loaiTT);
        boolean daTruKhoCOD = (idTTTruocKhiHuy == 3);

        if (laDonOnline || daTruKhoCOD) {
            // Hoàn sản phẩm
            for (HoaDonChiTiet ct : hd.getHoaDonChiTiets()) {
                SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
                if (spct != null) {
                    spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                    sanPhamChiTietRepository.save(spct);
                }
            }
            // 1. Kiểm tra nếu hóa đơn có gắn mã giảm giá
            if (hd.getIdMaGiamGia() != null) {
                // Lấy đối tượng Voucher từ hóa đơn
                var voucher = hd.getIdMaGiamGia();

                // Tăng số lượng lên 1
                int soLuongMoi = voucher.getSoLuong() + 1;
                voucher.setSoLuong(soLuongMoi);

                // QUAN TRỌNG: Bạn phải gọi repository của Mã Giảm Giá để lưu
                // Thay 'maGiamGiaRepo' bằng tên biến Repository mã giảm giá của bạn
                maGiamGiaRepository.save(voucher);

                System.out.println("Đã hoàn voucher: " + voucher.getMa() + " - Số lượng mới: " + soLuongMoi);
            }
        }
        hd.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(15).get());
        hoaDonRepository.save(hd);
    }
    public BigDecimal tinhTongThanhToanThucTe(Integer idKhachHang, String maVoucher, BigDecimal phiShip) {
        // 1. Tính Tiền hàng (Subtotal) từ giỏ hàng thực tế trong DB
        var listGioHang = gioHangService.layGioHangCuaKhach(idKhachHang);
        BigDecimal tienHang = BigDecimal.ZERO;
        for (var item : listGioHang) {
            BigDecimal gia = item.getIdSanPhamChiTiet().getDonGia();
            tienHang = tienHang.add(gia.multiply(BigDecimal.valueOf(item.getSoLuong())));
        }

        // 2. Tính số tiền giảm từ Voucher
        BigDecimal soTienGiam = BigDecimal.ZERO;
        if (maVoucher != null && !maVoucher.trim().isEmpty()) {
            MaGiamGia voucher = maGiamGiaRepository.findByMa(maVoucher.trim()).orElse(null);
            if (voucher != null && voucher.getSoLuong() > 0) {
                BigDecimal giaTriVoucher = BigDecimal.valueOf(voucher.getGiaTri());
                // loaiGiam = 0 là Tiền mặt, loaiGiam = 1 là %
                if (voucher.getLoaiGiam() == 0) {
                    soTienGiam = giaTriVoucher;
                } else {
                    soTienGiam = tienHang.multiply(giaTriVoucher)
                            .divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);
                }
            }
        }

        // Ràng buộc: Voucher không được giảm quá tiền hàng
        if (soTienGiam.compareTo(tienHang) > 0) soTienGiam = tienHang;

        // 3. Công thức: (Tiền hàng - Voucher) + Phí ship
        return tienHang.subtract(soTienGiam).add(phiShip != null ? phiShip : BigDecimal.ZERO);
    }
}