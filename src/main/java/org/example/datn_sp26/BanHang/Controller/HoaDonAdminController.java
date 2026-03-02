package org.example.datn_sp26.BanHang.Controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.example.datn_sp26.BanHang.Entity.HoaDonChiTiet;
import org.example.datn_sp26.BanHang.Entity.TrangThaiHoaDon;
import org.example.datn_sp26.BanHang.Repository.HoaDonChiTietRepository;
import org.example.datn_sp26.BanHang.Service.HoaDonExcelExporter;
import org.example.datn_sp26.BanHang.Service.HoaDonService;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/hoa-don")
public class HoaDonAdminController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private HoaDonExcelExporter excelExporter;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @GetMapping
    public String list(
            @RequestParam(required = false) String tenKH,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String loaiTT,
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay,
            Model model) {

        List<HoaDon> danhSach = getDanhSachFiltered(tenKH, trangThai, loaiTT, tuNgay, denNgay);

        // Load dropdown options
        List<TrangThaiHoaDon> dsTrangThai = hoaDonService.getAllTrangThai();

        model.addAttribute("list", danhSach);
        model.addAttribute("dsTrangThai", dsTrangThai);

        // Preserve filter values in form
        model.addAttribute("tenKH", tenKH);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("loaiTT", loaiTT);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);

        return "HoaDon/list";
    }

    @GetMapping("/xuat-excel")
    public void xuatExcel(
            @RequestParam(required = false) String tenKH,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String loaiTT,
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay,
            HttpServletResponse response) throws IOException {

        List<HoaDon> danhSach = getDanhSachFiltered(tenKH, trangThai, loaiTT, tuNgay, denNgay);
        excelExporter.export(danhSach, response);
    }

    // ===== API: Lấy chi tiết hóa đơn (JSON) =====
    @GetMapping("/chi-tiet/{id}")
    @ResponseBody
    public ResponseEntity<?> getChiTietHoaDon(@PathVariable Integer id) {
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonIdWithDetails(id);

        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (HoaDonChiTiet ct : chiTietList) {
            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
            SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
            item.put("mauSac", spct != null && spct.getIdMauSac() != null ? spct.getIdMauSac().getTenMau() : "—");
            item.put("size", spct != null && spct.getIdSize() != null ? spct.getIdSize().getTenSize() : "—");
            item.put("chatLieu",
                    spct != null && spct.getIdChatLieu() != null ? spct.getIdChatLieu().getTenChatLieu() : "—");
            item.put("donGia", ct.getDonGia());
            item.put("soLuong", ct.getSoLuong());
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cap-nhat-trang-thai")
    public String capNhatTrangThai(
            @RequestParam Integer hoaDonId,
            @RequestParam Integer trangThaiId,
            RedirectAttributes redirectAttributes) {
        try {
            if (trangThaiId == 13) {
                // Chỉ khi chọn "Đã xác nhận" mới chạy logic trừ kho COD
                hoaDonService.xacnhanDonHang(hoaDonId);
                redirectAttributes.addFlashAttribute("successMessage", "Xác nhận đơn hàng thành công!");
            }
            else if (trangThaiId == 5) {
                // Chỉ khi chọn "Đã hủy" mới chạy logic hoàn kho
                hoaDonService.huyDonHangVaHoanKho(hoaDonId);
                redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn và hoàn kho thành công!");
            }
            else {
                // Các trạng thái khác (Giao hàng, Hoàn thành...) CHỈ đổi trạng thái, KHÔNG đụng vào kho
                hoaDonService.capNhatTrangThai(hoaDonId, trangThaiId);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/hoa-don";
    }
    // ===== Helper: tái sử dụng logic lọc =====
    private List<HoaDon> getDanhSachFiltered(String tenKH, String trangThai,
            String loaiTT, String tuNgay, String denNgay) {
        Instant tuNgayInstant = null;
        Instant denNgayInstant = null;

        if (tuNgay != null && !tuNgay.isBlank()) {
            tuNgayInstant = LocalDate.parse(tuNgay)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
        }

        if (denNgay != null && !denNgay.isBlank()) {
            denNgayInstant = LocalDate.parse(denNgay)
                    .plusDays(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
        }

        boolean hasFilter = (tenKH != null && !tenKH.isBlank())
                || (trangThai != null && !trangThai.isBlank())
                || (loaiTT != null && !loaiTT.isBlank())
                || tuNgayInstant != null
                || denNgayInstant != null;

        if (hasFilter) {
            String tenKHParam = (tenKH != null && !tenKH.isBlank()) ? tenKH : null;
            String trangThaiParam = (trangThai != null && !trangThai.isBlank()) ? trangThai : null;
            String loaiTTParam = (loaiTT != null && !loaiTT.isBlank()) ? loaiTT : null;
            return hoaDonService.filterHoaDon(tenKHParam, trangThaiParam, loaiTTParam,
                    tuNgayInstant, denNgayInstant);
        } else {
            return hoaDonService.findAll();
        }
    }
}
