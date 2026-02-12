package org.example.datn_sp26.BanHang.Controller.KhachHang;

import jakarta.servlet.http.HttpSession;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.example.datn_sp26.BanHang.Service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/khach-hang/don-hang")
public class DonHangController {

    @Autowired
    private HoaDonService hoaDonService;

    // Helper method để lấy idKhachHang từ session
    private Integer getIdKhachHang(HttpSession session) {
        return (Integer) session.getAttribute("idKhachHang");
    }

    @GetMapping
    public String xemDonHang(Model model, HttpSession session) {
        Integer idKhachHang = getIdKhachHang(session);

        if (idKhachHang == null) {
            return "redirect:/login";
        }

        List<HoaDon> danhSachDonHang = hoaDonService.layDonHangCuaKhach(idKhachHang);
        model.addAttribute("donHangs", danhSachDonHang);
        return "KhachHang/don-hang";
    }
}
