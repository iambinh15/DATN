package org.example.datn_sp26.BanHang.Service;

import jakarta.transaction.Transactional;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.example.datn_sp26.BanHang.Entity.LoaiThanhToan;
import org.example.datn_sp26.BanHang.Entity.TrangThaiHoaDon;
import org.example.datn_sp26.BanHang.Repository.HoaDonRepository;
import org.example.datn_sp26.BanHang.Repository.LoaiThanhToanRepository;
import org.example.datn_sp26.BanHang.Repository.TrangThaiHoaDonRepository;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class HoaDonService {

        @Autowired
        private HoaDonRepository hoaDonRepository;

        @Autowired
        private TrangThaiHoaDonRepository trangThaiHoaDonRepository;

        @Autowired
        private LoaiThanhToanRepository loaiThanhToanRepository;

        // ===============================
        // HÀM CŨ (GIỮ NGUYÊN)
        // ===============================
        public HoaDon taoHoaDonSauThanhToan(
                KhachHang khachHang,
                BigDecimal tongThanhToan,
                NhanVien nhanVienDangNhap ) {

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

                // ❗ phiShip KHÔNG cộng – chỉ dùng để hiển thị / thu COD
                // (nếu có cột phi_ship thì set riêng, còn không thì bỏ)

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
                return trangThaiHoaDonRepository.findAll();
        }

        private String taoMaHoaDon() {
                return "HD" + System.currentTimeMillis();
        }
}
