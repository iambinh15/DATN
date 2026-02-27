package org.example.datn_sp26.BanHang.Service;

import jakarta.transaction.Transactional;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.BanHang.Entity.GioHang;
import org.example.datn_sp26.BanHang.Entity.GioHangChiTiet;

import org.example.datn_sp26.BanHang.Repository.GioHangChiTietRepository;
import org.example.datn_sp26.BanHang.Repository.GioHangRepository;

import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class GioHangService {
    @Autowired private GioHangRepository gioHangRepo;
    @Autowired private GioHangChiTietRepository ghctRepo;
    @Autowired private SanPhamChiTietRepository spctRepo;


    /**
     * Hàm thêm sản phẩm vào giỏ hàng
     */

    @Transactional
    public void themVaoGio(Integer idKhachHang, Integer idSPCT) {
        // 1. Lấy hoặc tạo Giỏ hàng (Giữ nguyên logic cũ)
        GioHang gioHang = gioHangRepo.findByIdKhachHang_Id(idKhachHang)
                .orElseGet(() -> {
                    GioHang newGH = new GioHang();
                    KhachHang kh = new KhachHang();
                    kh.setId(idKhachHang);
                    newGH.setIdKhachHang(kh);
                    newGH.setNgayTao(Instant.now());
                    newGH.setTrangThai(1);
                    return gioHangRepo.save(newGH);
                });

        // 2. Tìm ĐÍCH DANH biến thể mà khách hàng đã chọn (Size/Màu)
        SanPhamChiTiet spct = spctRepo.findById(idSPCT).orElse(null);

        if (spct != null) {
            // Tìm xem trong giỏ hàng đã có sản phẩm này chưa
            Optional<GioHangChiTiet> existingItem = ghctRepo.findByIdGioHang_IdAndIdSanPhamChiTiet_Id(
                    gioHang.getId(),
                    spct.getId()
            );

            // --- BẮT ĐẦU LOGIC KIỂM TRA TỒN KHO ---
            // Lấy số lượng hiện tại khách đã có trong giỏ (nếu chưa có thì là 0)
            int soLuongHienTaiTrongGio = existingItem.isPresent() ? existingItem.get().getSoLuong() : 0;

            // Tổng số lượng sau khi khách nhấn thêm 1 món
            int soLuongDuKien = soLuongHienTaiTrongGio + 1;

            // KIỂM TRA: Nếu tổng dự kiến > tồn kho thì chặn lại ngay
            if (soLuongDuKien > spct.getSoLuong()) {
                throw new RuntimeException("Không thể thêm! Trong giỏ đã có " + soLuongHienTaiTrongGio +
                        " sản phẩm, mà kho chỉ còn " + spct.getSoLuong() + " sản phẩm.");
            }
            // --- KẾT THÚC LOGIC KIỂM TRA TỒN KHO ---

            if (existingItem.isPresent()) {
                // Nếu đã có rồi thì tăng số lượng lên 1
                GioHangChiTiet item = existingItem.get();
                item.setSoLuong(soLuongDuKien); // Sử dụng biến đã cộng ở trên
                ghctRepo.save(item);
            } else {
                // Nếu chưa có thì thêm mới vào giỏ hàng
                GioHangChiTiet newItem = new GioHangChiTiet();
                newItem.setIdGioHang(gioHang);
                newItem.setIdSanPhamChiTiet(spct);
                newItem.setSoLuong(1);
                ghctRepo.save(newItem);
            }
        }
    }

    // Trong GioHangService
    public void updateSoLuong(Integer idGHCT, Integer soLuongMoi) {
        GioHangChiTiet ghct = ghctRepo.findById(idGHCT).get();
        int tonKho = ghct.getIdSanPhamChiTiet().getSoLuong();

        if (soLuongMoi > tonKho) {
            // Cập nhật lại giỏ hàng bằng đúng số lượng tồn kho còn lại
            ghct.setSoLuong(tonKho);
            ghctRepo.save(ghct);
            throw new RuntimeException("Số lượng yêu cầu vượt quá tồn kho hiện có (" + tonKho + ")");
        }

        ghct.setSoLuong(soLuongMoi);
        ghctRepo.save(ghct);
    }
    @Transactional
    public void thayDoiSoLuong(Integer idGhct, Integer delta) {
        // 1. Tìm dòng sản phẩm trong giỏ hàng
        GioHangChiTiet item = ghctRepo.findById(idGhct)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        // 2. Tính toán số lượng mới
        int soLuongMoi = item.getSoLuong() + delta;

        // 3. LOGIC MỚI: Kiểm tra tồn kho khi người dùng nhấn nút Tăng (+)
        if (delta > 0) {
            // Lấy số lượng thực tế đang có trong kho của biến thể này
            int tonKhoThucTe = item.getIdSanPhamChiTiet().getSoLuong();

            if (soLuongMoi > tonKhoThucTe) {
                throw new RuntimeException("Số lượng yêu cầu vượt quá tồn kho hiện có (" + tonKhoThucTe + ")");
            }
        }
        // 4. Giữ nguyên logic cũ: Cập nhật hoặc Xóa
        if (soLuongMoi > 0) {
            item.setSoLuong(soLuongMoi);
            ghctRepo.save(item);
        } else {
            ghctRepo.delete(item);
        }
    }

    @Transactional
    public void capNhatSoLuongTuyChinh(Integer idGhct, Integer soLuongMoi) {
        GioHangChiTiet item = ghctRepo.findById(idGhct)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        // BƯỚC THÊM MỚI: Lấy số lượng thực tế trong kho
        int tonKhoThucTe = item.getIdSanPhamChiTiet().getSoLuong();

        if (soLuongMoi > 0) {
            // KIỂM TRA: Nếu số lượng mới lớn hơn tồn kho thì chặn lại ngay
            if (soLuongMoi > tonKhoThucTe) {
                throw new RuntimeException("Rất tiếc, trong kho chỉ còn " + tonKhoThucTe + " sản phẩm!");
            }

            item.setSoLuong(soLuongMoi);
            ghctRepo.save(item);
        } else {
            ghctRepo.delete(item);
        }
    }

    /**
     * Hàm lấy danh sách tất cả sản phẩm trong giỏ của khách hàng
     */
    public List<GioHangChiTiet> layGioHangCuaKhach(Integer idKhachHang) {
        Optional<GioHang> gioHang = gioHangRepo.findByIdKhachHang_Id(idKhachHang);
        if (gioHang.isPresent()) {
            return ghctRepo.findByIdGioHang_Id(gioHang.get().getId());
        }
        return Collections.emptyList();
    }

    /**
     * Xóa 1 dòng sản phẩm
     */
    @Transactional
    public void xoaSanPhamKhoiGio(Integer idGioHangChiTiet) {
        if (ghctRepo.existsById(idGioHangChiTiet)) {
            ghctRepo.deleteById(idGioHangChiTiet);
        } else {
            throw new RuntimeException("Không tìm thấy sản phẩm này!");
        }
    }
    @Transactional
    public void xoaDanhSachSanPhamDaMua(List<Integer> idsGhct) {
        if (idsGhct != null && !idsGhct.isEmpty()) {
            // Sử dụng deleteAllById để xóa nhanh danh sách ID giỏ hàng chi tiết
            ghctRepo.deleteAllById(idsGhct);
        }
    }
    /**
     * Xóa sạch giỏ hàng
     */
    @Transactional
    public void xoaTatCaGioHang(Integer idKhachHang) {
        Optional<GioHang> gioHang = gioHangRepo.findByIdKhachHang_Id(idKhachHang);
        if (gioHang.isPresent()) {
            List<GioHangChiTiet> chiTiets = ghctRepo.findByIdGioHang_Id(gioHang.get().getId());
            ghctRepo.deleteAll(chiTiets);
        }
    }
}