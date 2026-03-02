package org.example.datn_sp26.ThongKe.Service;

import org.example.datn_sp26.BanHang.Repository.HoaDonChiTietRepository;
import org.example.datn_sp26.BanHang.Repository.HoaDonRepository;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ThongKeService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    public ThongKeService(HoaDonRepository hoaDonRepository,
                          HoaDonChiTietRepository hoaDonChiTietRepository,
                          SanPhamChiTietRepository sanPhamChiTietRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
    }

    public BigDecimal getDoanhThu(Instant tu, Instant den) {
        return hoaDonRepository.tongDoanhThu(tu, den);
    }

    // 🔥 12 tháng trong năm
    public List<BigDecimal> getDoanhThuTheoThang(int year) {

        List<BigDecimal> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {

            LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);

            LocalDateTime end = start.withDayOfMonth(
                            start.toLocalDate().lengthOfMonth())
                    .withHour(23).withMinute(59).withSecond(59);

            Instant tu = start.atZone(ZoneId.systemDefault()).toInstant();
            Instant den = end.atZone(ZoneId.systemDefault()).toInstant();

            BigDecimal doanhThu =
                    hoaDonRepository.tongDoanhThu(tu, den);

            result.add(doanhThu != null ? doanhThu : BigDecimal.ZERO);
        }

        return result;
    }
    public List<Object[]> getTopSanPhamBanChay(Instant tu, Instant den) {
        List<Object[]> list =
                hoaDonChiTietRepository.topSanPhamBanChay(tu, den);

        if (list == null) {
            return new ArrayList<>();
        }

        return list.stream().limit(5).toList();
    }

    public List<SanPhamChiTiet> getSanPhamSapHetHang() {
        return sanPhamChiTietRepository.findSanPhamSapHetHang(10);
    }
    public long countSanPhamSapHetHang() {
        return sanPhamChiTietRepository.countBySoLuongLessThan(10);
    }
}