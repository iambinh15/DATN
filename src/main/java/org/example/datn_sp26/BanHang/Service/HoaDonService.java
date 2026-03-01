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
import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

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
    @Autowired
    private SanPhamChiTietService sanPhamChiTietService;
    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;

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
            if (tongSauGiam.compareTo(BigDecimal.ZERO) < 0)
                tongSauGiam = BigDecimal.ZERO;

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
    public HoaDon taoHoaDonVNPay(KhachHang khachHang, BigDecimal tongThanhToan, String diaChi, BigDecimal phiShip,
            String maVoucher) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon("HD_VNP" + System.currentTimeMillis());
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setDiaChi(diaChi);

        // Gán tổng tiền (Online thường đã trừ ở Client, nhưng gọi xuLyVoucher để lưu
        // quan hệ MaGiamGia)
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
                trangThaiHoaDonRepository.findById(ID_CHO_THANH_TOAN).get()
        );

        return hoaDonRepository.save(hoaDon);
    }

    /**
     * Thêm sản phẩm chi tiết vào hóa đơn POS
     */
    @Transactional
    public void themSanPhamVaoHoaDon(Integer hoaDonId, Integer spctId, Integer soLuong) {

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(spctId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

        if (spct.getSoLuong() < soLuong) {
            throw new RuntimeException("Sản phẩm không đủ số lượng tồn kho!");
        }
        Optional<HoaDonChiTiet> existing =
                hoaDonChiTietRepository.findByIdHoaDonAndIdSanPhamChiTiet(hoaDon, spct);

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

        capNhatTongTien(hoaDonId);
    }

    /**
     * Xóa sản phẩm khỏi hóa đơn POS
     */

    @Transactional
    public void xoaSanPhamKhoiHoaDon(Integer hoaDonChiTietId) {

        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(hoaDonChiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));

        Integer hoaDonId = hdct.getIdHoaDon().getId();
        SanPhamChiTiet spct = hdct.getIdSanPhamChiTiet();
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
        if (spct.getSoLuong() < soLuong) {
            throw new RuntimeException("Sản phẩm không đủ số lượng tồn kho!");
        }

        hdct.setSoLuong(soLuong);
        hoaDonChiTietRepository.save(hdct);
        capNhatTongTien(hdct.getIdHoaDon().getId());
    }

    /**
     * Thanh toán hóa đơn tại quầy
     */
    @Transactional
    public void thanhToanTaiQuay(Integer hoaDonId, Integer khachHangId, Integer voucherId,
            String phuongThuc, BigDecimal tienKhachDua) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Gán khách hàng nếu có
        if (khachHangId != null) {
            var khachHangRepo = org.springframework.beans.factory.BeanFactoryUtils.class;
            // Sử dụng repository trực tiếp
        }

        // Gán phương thức thanh toán
        hoaDon.setIdLoaiThanhToan(loaiThanhToanRepository.findByTenLoai(phuongThuc).get());

        // Tính tổng tiền trước khi áp voucher
        capNhatTongTien(hoaDonId);
        hoaDon = hoaDonRepository.findById(hoaDonId).get(); // reload

        // Áp voucher nếu có
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

        // Trừ kho
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        for (HoaDonChiTiet ct : chiTietList) {
            SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
            if (spct.getSoLuong() < ct.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + spct.getId() + " không đủ tồn kho!");
            }
            spct.setSoLuong(spct.getSoLuong() - ct.getSoLuong());
            sanPhamChiTietRepository.save(spct);
        }

        // Chuyển trạng thái sang "Hoàn tất"
        hoaDon.setIdTrangThaiHoaDon(trangThaiHoaDonRepository.findById(ID_HOAN_TAT).get());
        hoaDonRepository.save(hoaDon);
    }

    /**
     * Lấy danh sách hóa đơn POS (trạng thái "Chờ thanh toán")
     */
    public List<HoaDon> layDsHoaDonTaiQuay() {
        return hoaDonRepository.findByIdTrangThaiHoaDon_IdOrderByNgayTaoDesc(ID_CHO_THANH_TOAN);
    }

    /**
     * Cập nhật tổng tiền của hóa đơn dựa trên chi tiết
     */
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

    /**
     * Lấy danh sách chi tiết hóa đơn cho POS
     */
    public List<HoaDonChiTiet> layChiTietHoaDon(Integer hoaDonId) {
        return hoaDonChiTietRepository.findByHoaDonIdWithDetails(hoaDonId);
    }
    @Scheduled(fixedRate = 60000) // chạy mỗi 60 giây
    @Transactional
    public void tuDongHuyHoaDonQuaHan() {

        Instant gioiHan = Instant.now().minusSeconds(60); // 10 phút

        List<HoaDon> danhSach = hoaDonRepository
                .findByIdTrangThaiHoaDon_IdAndNgayTaoBefore(
                        ID_CHO_THANH_TOAN,
                        gioiHan
                );

        for (HoaDon hd : danhSach) {

            hd.setIdTrangThaiHoaDon(
                    trangThaiHoaDonRepository.findById(5).get() // Đã hủy
            );

            hoaDonRepository.save(hd);

            System.out.println("===> Đã tự động hủy hóa đơn ID: " + hd.getId());
        }
    }
}