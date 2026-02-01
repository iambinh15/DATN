package org.example.datn_sp26.SanPham.Controller;


import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class SanPhamChiTietController {

    @Autowired
    private SanPhamChiTietRepository spctRepo; // Gọi thẳng Repo hoặc qua Service của SPCT

    @GetMapping("/sanpham")
    public String showSanPham(Model model) {
        // Lấy danh sách tất cả các biến thể (Sản phẩm chi tiết)
        List<SanPhamChiTiet> listSPCT = spctRepo.findAll();

        // Đưa danh sách chi tiết này sang HTML
        model.addAttribute("listSPCT", listSPCT);
        return "KhachHang/spct";
    }
}