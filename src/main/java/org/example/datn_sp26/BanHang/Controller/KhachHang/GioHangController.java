package org.example.datn_sp26.BanHang.Controller.KhachHang;

import jakarta.servlet.http.HttpSession;
import org.example.datn_sp26.BanHang.Entity.GioHangChiTiet;
import org.example.datn_sp26.BanHang.Service.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/khach-hang/gio-hang")
public class GioHangController {

    @Autowired
    private GioHangService gioHangService;

    // Helper method để lấy idKhachHang từ session
    private Integer getIdKhachHang(HttpSession session) {
        return (Integer) session.getAttribute("idKhachHang");
    }

    // 1. HIỂN THỊ GIỎ HÀNG
    @GetMapping
    public String xemGioHang(Model model, HttpSession session) {
        Integer idKhachHang = getIdKhachHang(session);

        // Kiểm tra nếu chưa đăng nhập
        if (idKhachHang == null) {
            return "redirect:/login";
        }

        List<GioHangChiTiet> danhSachTrongGio = gioHangService.layGioHangCuaKhach(idKhachHang);
        model.addAttribute("items", danhSachTrongGio);
        return "KhachHang/gio-hang";
    }

    // 2. THÊM SẢN PHẨM VÀO GIỎ
    @PostMapping("/add")
    public String themVaoGio(@RequestParam("productId") Integer productId,
            @RequestParam(value = "soLuong", defaultValue = "1") Integer soLuong,
            HttpSession session,
            RedirectAttributes ra) {
        Integer idKhachHang = getIdKhachHang(session);

        // Kiểm tra nếu chưa đăng nhập
        if (idKhachHang == null) {
            return "redirect:/login";
        }

        try {
            gioHangService.themVaoGio(idKhachHang, productId);
            ra.addFlashAttribute("message", "Đã thêm vào giỏ hàng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("message", "Lỗi khi thêm: " + e.getMessage());
        }
        return "redirect:/khach-hang/gio-hang";
    }

    // 3. TĂNG / GIẢM SỐ LƯỢNG NHANH (+1 hoặc -1)
    @GetMapping("/update/{id}/{action}")
    public String updateSoLuong(@PathVariable("id") Integer id,
            @PathVariable("action") String action,
            HttpSession session,
            RedirectAttributes ra) {
        Integer idKhachHang = getIdKhachHang(session);
        if (idKhachHang == null) {
            return "redirect:/login";
        }

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
            HttpSession session,
            RedirectAttributes ra) {
        Integer idKhachHang = getIdKhachHang(session);
        if (idKhachHang == null) {
            return "redirect:/login";
        }

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
    public String xoaSanPham(@PathVariable("id") Integer id,
            HttpSession session,
            RedirectAttributes ra) {
        Integer idKhachHang = getIdKhachHang(session);
        if (idKhachHang == null) {
            return "redirect:/login";
        }

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
    public String xoaSach(HttpSession session, RedirectAttributes ra) {
        Integer idKhachHang = getIdKhachHang(session);
        if (idKhachHang == null) {
            return "redirect:/login";
        }

        try {
            gioHangService.xoaTatCaGioHang(idKhachHang);
            ra.addFlashAttribute("message", "Đã làm trống giỏ hàng!");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Lỗi: " + e.getMessage());
        }
        return "redirect:/khach-hang/gio-hang";
    }

    // 7. AJAX: TĂNG / GIẢM SỐ LƯỢNG (không reload trang)
    @GetMapping("/update/{id}/{action}/ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSoLuongAjax(
            @PathVariable("id") Integer id,
            @PathVariable("action") String action,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Integer idKhachHang = getIdKhachHang(session);
        if (idKhachHang == null) {
            result.put("success", false);
            result.put("message", "Chưa đăng nhập");
            return ResponseEntity.status(401).body(result);
        }

        try {
            int thayDoi = "increase".equals(action) ? 1 : -1;
            GioHangChiTiet updated = gioHangService.thayDoiSoLuong(id, thayDoi);
            result.put("success", true);
            result.put("soLuong", updated.getSoLuong());
            result.put("donGia", updated.getIdSanPhamChiTiet().getDonGia());
            result.put("thanhTien",
                    updated.getIdSanPhamChiTiet().getDonGia().multiply(BigDecimal.valueOf(updated.getSoLuong())));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    // 8. AJAX: CẬP NHẬT SỐ LƯỢNG TÙY CHỈNH (không reload trang)
    @PostMapping("/update-quantity-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantityAjax(
            @RequestParam("id") Integer id,
            @RequestParam("quantity") Integer quantity,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Integer idKhachHang = getIdKhachHang(session);
        if (idKhachHang == null) {
            result.put("success", false);
            result.put("message", "Chưa đăng nhập");
            return ResponseEntity.status(401).body(result);
        }

        try {
            if (quantity == null || quantity <= 0) {
                gioHangService.xoaSanPhamKhoiGio(id);
                result.put("success", true);
                result.put("deleted", true);
            } else {
                GioHangChiTiet updated = gioHangService.capNhatSoLuongTuyChinh(id, quantity);
                result.put("success", true);
                result.put("soLuong", updated.getSoLuong());
                result.put("donGia", updated.getIdSanPhamChiTiet().getDonGia());
                result.put("thanhTien",
                        updated.getIdSanPhamChiTiet().getDonGia().multiply(BigDecimal.valueOf(updated.getSoLuong())));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

}