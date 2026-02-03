package org.example.datn_sp26.BanHang.Service;

import org.example.datn_sp26.BanHang.Entity.LoaiThanhToan;
import org.example.datn_sp26.BanHang.Entity.TrangThaiHoaDon;
import org.example.datn_sp26.BanHang.Repository.HoaDonRepository;
import org.example.datn_sp26.BanHang.Repository.LoaiThanhToanRepository;
import org.example.datn_sp26.BanHang.Repository.TrangThaiHoaDonRepository;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.datn_sp26.BanHang.Entity.HoaDon;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class HoaDonService {
    @Autowired
    private LoaiThanhToanRepository loaiThanhToanRepository;

    private final HoaDonRepository hoaDonRepository;
    private final TrangThaiHoaDonRepository trangThaiHoaDonRepository;

    public HoaDonService(HoaDonRepository hoaDonRepository,
                         TrangThaiHoaDonRepository trangThaiHoaDonRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.trangThaiHoaDonRepository = trangThaiHoaDonRepository;
    }

    public HoaDon taoHoaDonSauThanhToan(KhachHang khachHang,
                                        BigDecimal tongThanhToan) {

        HoaDon hoaDon = new HoaDon();

        hoaDon.setMaHoaDon(taoMaHoaDon());
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setTongThanhToan(tongThanhToan);

        // 🔥 TRẠNG THÁI MẶC ĐỊNH: "MỚI TẠO"
        TrangThaiHoaDon trangThai =
                trangThaiHoaDonRepository.findByTenTrangThai("Chờ Thanh Toán")
                        .orElseThrow(() ->
                                new RuntimeException("Không tìm thấy trạng thái Chờ Thanh Toán"));

        hoaDon.setIdTrangThaiHoaDon(trangThai);
        // ===== LOẠI THANH TOÁN: CHUYỂN KHOẢN =====
        LoaiThanhToan loaiThanhToan =
                loaiThanhToanRepository.findByTenLoai("CK")
                        .orElseThrow(() ->
                                new RuntimeException("Không tìm thấy loại thanh toán Chuyển khoản"));


        hoaDon.setIdLoaiThanhToan(loaiThanhToan);
        return hoaDonRepository.save(hoaDon);
    }
        private String taoMaHoaDon () {
            return "HD" + System.currentTimeMillis();
        }
    }


