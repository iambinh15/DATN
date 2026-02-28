package org.example.datn_sp26.KhuyenMai.Service;

import lombok.RequiredArgsConstructor;
import org.example.datn_sp26.KhuyenMai.Entity.MaGiamGia;
import org.example.datn_sp26.KhuyenMai.Repository.MaGiamGiaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaGiamGiaService {

    private final MaGiamGiaRepository maGiamGiaRepository;

    // Giữ nguyên tên hàm để GHNController không bị lỗi đỏ
    public List<MaGiamGia> layMaDangHoatDong() {
        LocalDateTime now = LocalDateTime.now();

        // Lấy từ Repo những mã có trangThai = 1
        return maGiamGiaRepository.findByTrangThai(1).stream()
                .filter(km -> km.getSoLuong() != null && km.getSoLuong() > 0) // Lọc mã còn lượt dùng
                .filter(km -> (km.getNgayBatDau() == null || now.isAfter(km.getNgayBatDau()))) // Lọc mã đã đến ngày bắt đầu
                .filter(km -> (km.getNgayKetThuc() == null || now.isBefore(km.getNgayKetThuc()))) // Lọc mã chưa hết hạn
                .collect(Collectors.toList());
    }

    // Hàm tính toán số tiền giảm (giữ nguyên logic bạn đã viết)
    public double tinhSoTienGiam(String code, double tongTienDonHang) {
        MaGiamGia km = maGiamGiaRepository.findByMa(code)
                .orElseThrow(() -> new RuntimeException("Mã không tồn tại"));

        if (km.getTrangThai() != 1) throw new RuntimeException("Mã bị khóa");

        // Kiểm tra điều kiện đơn tối thiểu từ bảng SQL
        if (km.getGiamToiThieu() != null && tongTienDonHang < km.getGiamToiThieu()) {
            throw new RuntimeException("Đơn hàng chưa đủ điều kiện tối thiểu");
        }

        double soTienGiam = 0;
        if (km.getLoaiGiam() != null && km.getLoaiGiam() == 1) { // Giảm %
            soTienGiam = tongTienDonHang * (km.getGiaTri() / 100);
            if (km.getGiamToiDa() != null && soTienGiam > km.getGiamToiDa()) {
                soTienGiam = km.getGiamToiDa();
            }
        } else { // Giảm tiền mặt
            soTienGiam = (km.getGiaTri() != null) ? km.getGiaTri() : 0;
        }

        return Math.min(soTienGiam, tongTienDonHang);
    }
}