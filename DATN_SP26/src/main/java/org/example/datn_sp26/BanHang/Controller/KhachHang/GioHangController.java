package org.example.datn_sp26.BanHang.Controller.KhachHang;

import org.example.datn_sp26.BanHang.Entity.GioHangChiTiet;
import org.example.datn_sp26.BanHang.Service.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/khach-hang/gio-hang")
public class GioHangController {

    @Autowired
    private GioHangService gioHangService;

    // 1. HIỂN THỊ GIỎ HÀNG
    @GetMapping
    public String xemGioHang(Model model) {
        // Giả định ID khách hàng cố định là 1 để kiểm thử
        Integer idKhachHang = 1;
        List<GioHangChiTiet> danhSachTrongGio = gioHangService.layGioHangCuaKhach(idKhachHang);

        model.addAttribute("items", danhSachTrongGio);
        return "KhachHang/gio-hang"; // Trả về view trong thư mục KhachHang
    }

    // 2. THÊM SẢN PHẨM VÀO GIỎ
    @PostMapping("/add")
    public String themVaoGio(@RequestParam("productId") Integer productId,
                             @RequestParam(value = "soLuong", defaultValue = "1") Integer soLuong,
                             RedirectAttributes ra) {
        try {
            Integer idKhachHang = 1;
            // Gọi Service xử lý logic thêm mới hoặc cộng dồn số lượng
            gioHangService.themVaoGio(idKhachHang, productId);
            ra.addFlashAttribute("message", "Đã thêm vào giỏ hàng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("message", "Lỗi khi thêm: " + e.getMessage());
        }
        return "redirect:/khach-hang/gio-hang"; // Chuyển hướng để cập nhật dữ liệu mới nhất
    }

    // 3. TĂNG / GIẢM SỐ LƯỢNG NHANH (+1 hoặc -1)
    @GetMapping("/update/{id}/{action}")
    public String updateSoLuong(@PathVariable("id") Integer id,
                                @PathVariable("action") String action,
                                RedirectAttributes ra) {
        try {
            int thayDoi = "increase".equals(action) ? 1 : -1;
            gioHangService.thayDoiSoLuong(id, thayDoi);
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/khach-hang/gio-hang";
    }

    // 4. CẬP NHẬT SỐ LƯỢNG TÙY CHỈNH (Nhập trực tiếp từ ô Input)
    @PostMapping("/update-quantity")
    public String updateQuantity(@RequestParam("id") Integer id,
                                 @RequestParam("quantity") Integer quantity,
                                 RedirectAttributes ra) {
        try {
            if (quantity == null || quantity <= 0) {
                gioHangService.xoaSanPhamKhoiGio(id);
                ra.addFlashAttribute("message", "Đã xóa sản phẩm!");
            } else {
                gioHangService.capNhatSoLuongTuyChinh(id, quantity);
                ra.addFlashAttribute("message", "Đã cập nhật số lượng!");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Lỗi: " + e.getMessage());
        }
        return "redirect:/khach-hang/gio-hang";
    }

    // 5. XÓA MỘT DÒNG SẢN PHẨM
    @GetMapping("/delete/{id}")
    public String xoaSanPham(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            gioHangService.xoaSanPhamKhoiGio(id);
            ra.addFlashAttribute("message", "Đã xóa sản phẩm khỏi giỏ!");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/khach-hang/gio-hang";
    }

    // 6. XÓA SẠCH GIỎ HÀNG
    @GetMapping("/clear")
    public String xoaSach(RedirectAttributes ra) {
        try {
            gioHangService.xoaTatCaGioHang(1);
            ra.addFlashAttribute("message", "Đã làm trống giỏ hàng!");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Lỗi: " + e.getMessage());
        }
        return "redirect:/khach-hang/gio-hang";
    }
}