package org.example.datn_sp26.KhuyenMai.Service;

import lombok.RequiredArgsConstructor;
import org.example.datn_sp26.KhuyenMai.Entity.MaGiamGia;
import org.example.datn_sp26.KhuyenMai.Repository.MaGiamGiaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaGiamGiaService {

    private final MaGiamGiaRepository maGiamGiaRepository;

    /**
     * Tác vụ chạy ngầm: Tự động cập nhật trạng thái về 0 (Ngưng)
     * nếu hết số lượng HOẶC hết thời gian.
     * Chạy mỗi 60 giây (60000ms).
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void tuDongQuetTrangThaiVoucher() {
        LocalDateTime now = LocalDateTime.now();
        List<MaGiamGia> allVouchers = maGiamGiaRepository.findAll();

        for (MaGiamGia km : allVouchers) {
            boolean conHan = (km.getNgayKetThuc() == null || now.isBefore(km.getNgayKetThuc()));
            boolean conSoLuong = (km.getSoLuong() != null && km.getSoLuong() > 0);
            boolean daBatDau = (km.getNgayBatDau() == null || now.isAfter(km.getNgayBatDau()));

            // Nếu đủ điều kiện mà đang bị Ngưng (0) -> Tự động bật lại (1)
            if (conHan && conSoLuong && daBatDau && km.getTrangThai() == 0) {
                // Lưu ý: Chỉ tự bật nếu trước đó nó bị ngắt do hết hạn/hết số lượng
                // Nếu bạn muốn Admin có quyền khóa thủ công, cần thêm 1 cột phụ.
                // Ở đây ta cứ cho nó tự động bật lại cho tiện nhé.
                km.setTrangThai(1);
                maGiamGiaRepository.save(km);
            }
            // Nếu vi phạm điều kiện mà đang Hoạt động (1) -> Ngắt (0)
            else if ((!conHan || !conSoLuong) && km.getTrangThai() == 1) {
                km.setTrangThai(0);
                maGiamGiaRepository.save(km);
            }
        }
    }

    /**
     * Hàm này dùng để gọi từ Controller khi bạn nhấn nút "Lưu Voucher"
     */
    @Transactional
    public void saveVoucher(MaGiamGia km) {
        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra logic ngay lập tức khi lưu
        boolean hetHan = (km.getNgayKetThuc() != null && now.isAfter(km.getNgayKetThuc()));
        boolean hetSoLuong = (km.getSoLuong() != null && km.getSoLuong() <= 0);

        if (hetHan || hetSoLuong) {
            km.setTrangThai(0);
        } else {
            // Nếu còn hạn và còn số lượng, hệ thống tự hiểu là muốn hoạt động
            km.setTrangThai(1);
        }

        maGiamGiaRepository.save(km);
    }

    /**
     * Lấy danh sách mã đang hoạt động cho GHNController.
     * Vừa lọc theo trangThai trong DB, vừa lọc logic thời gian/số lượng để đảm bảo chính xác.
     */
    public List<MaGiamGia> layMaDangHoatDong() {
        LocalDateTime now = LocalDateTime.now();

        // Lấy từ Repo những mã có trangThai = 1 và lọc lại một lần nữa cho chắc chắn
        return maGiamGiaRepository.findByTrangThai(1).stream()
                .filter(km -> km.getSoLuong() != null && km.getSoLuong() > 0)
                .filter(km -> km.getNgayBatDau() == null || now.isAfter(km.getNgayBatDau()))
                .filter(km -> km.getNgayKetThuc() == null || now.isBefore(km.getNgayKetThuc()))
                .collect(Collectors.toList());
    }

    /**
     * Hàm tính tiền giảm giá.
     * Đã bổ sung kiểm tra chặt chẽ để không cho phép dùng mã khi đã hết hạn/số lượng.
     */
    @Transactional
    public double tinhSoTienGiam(String code, double tongTienDonHang) {
        MaGiamGia km = maGiamGiaRepository.findByMa(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại trên hệ thống"));

        LocalDateTime now = LocalDateTime.now();

        // 1. Kiểm tra trạng thái cứng trong DB
        if (km.getTrangThai() != 1) {
            throw new RuntimeException("Mã giảm giá này đã ngừng áp dụng");
        }

        // 2. Kiểm tra số lượng (nếu hết thì cập nhật DB ngay lập tức)
        if (km.getSoLuong() != null && km.getSoLuong() <= 0) {
            km.setTrangThai(0);
            maGiamGiaRepository.save(km);
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
        }

        // 3. Kiểm tra thời gian (nếu hết hạn thì cập nhật DB ngay lập tức)
        if (km.getNgayKetThuc() != null && now.isAfter(km.getNgayKetThuc())) {
            km.setTrangThai(0);
            maGiamGiaRepository.save(km);
            throw new RuntimeException("Mã giảm giá đã hết thời gian sử dụng");
        }

        if (km.getNgayBatDau() != null && now.isBefore(km.getNgayBatDau())) {
            throw new RuntimeException("Chương trình khuyến mãi chưa bắt đầu");
        }

        // 4. Kiểm tra điều kiện đơn tối thiểu
        if (km.getGiamToiThieu() != null && tongTienDonHang < km.getGiamToiThieu()) {
            throw new RuntimeException("Đơn hàng tối thiểu phải từ " + String.format("%,.0f", km.getGiamToiThieu()) + " VNĐ");
        }

        // 5. Logic tính toán tiền giảm
        double soTienGiam = 0;
        if (km.getLoaiGiam() != null && km.getLoaiGiam() == 1) { // Giảm theo %
            soTienGiam = tongTienDonHang * (km.getGiaTri() / 100);
            // Kiểm tra mức giảm tối đa nếu có
            if (km.getGiamToiDa() != null && soTienGiam > km.getGiamToiDa()) {
                soTienGiam = km.getGiamToiDa();
            }
        } else { // Giảm theo tiền mặt (VNĐ)
            soTienGiam = (km.getGiaTri() != null) ? km.getGiaTri() : 0;
        }

        // Không cho phép giảm quá tổng tiền đơn hàng
        return Math.min(soTienGiam, tongTienDonHang);
    }
}