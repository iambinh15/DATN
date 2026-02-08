package org.example.datn_sp26.SanPham.Controller;


import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.example.datn_sp26.SanPham.Service.SanPhamChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class SanPhamChiTietController {

    @Autowired
    private SanPhamChiTietRepository spctRepo;
    
    @Autowired
    private SanPhamChiTietService sanPhamChiTietService;

    // ============ KHÁCH HÀNG ============
    
    @GetMapping("/sanpham")
    public String showSanPham(Model model) {
        // Lấy danh sách tất cả các biến thể (Sản phẩm chi tiết)
        List<SanPhamChiTiet> listSPCT = spctRepo.findAll();

        // Đưa danh sách chi tiết này sang HTML
        model.addAttribute("listSPCT", listSPCT);
        return "KhachHang/spct";
    }
    
    // ============ ADMIN ============
    
    @GetMapping("/san-pham-chi-tiet")
    public String adminIndex(Model model) {
        model.addAttribute("listSanPhamChiTiet", sanPhamChiTietService.getAll());
        model.addAttribute("page", "san-pham-chi-tiet");
        return "SanPhamChiTiet/index";
    }

    @GetMapping("/san-pham-chi-tiet/add")
    public String adminShowAddForm(Model model) {
        model.addAttribute("sanPhamChiTiet", new SanPhamChiTiet());
        model.addAttribute("listSanPham", sanPhamChiTietService.getAllSanPham());
        model.addAttribute("listMauSac", sanPhamChiTietService.getAllMauSac());
        model.addAttribute("listSize", sanPhamChiTietService.getAllSize());
        model.addAttribute("listChatLieu", sanPhamChiTietService.getAllChatLieu());
        model.addAttribute("action", "add");
        model.addAttribute("page", "san-pham-chi-tiet");
        return "SanPhamChiTiet/form";
    }

    @PostMapping("/san-pham-chi-tiet/add")
    public String adminAdd(@ModelAttribute SanPhamChiTiet sanPhamChiTiet, RedirectAttributes redirectAttributes) {
        try {
            sanPhamChiTietService.save(sanPhamChiTiet);
            redirectAttributes.addFlashAttribute("message", "Thêm sản phẩm chi tiết thành công!");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }
        return "redirect:/san-pham-chi-tiet";
    }

    @GetMapping("/san-pham-chi-tiet/edit/{id}")
    public String adminShowEditForm(@PathVariable Integer id, Model model) {
        sanPhamChiTietService.getById(id).ifPresent(spct -> {
            model.addAttribute("sanPhamChiTiet", spct);
            model.addAttribute("listSanPham", sanPhamChiTietService.getAllSanPham());
            model.addAttribute("listMauSac", sanPhamChiTietService.getAllMauSac());
            model.addAttribute("listSize", sanPhamChiTietService.getAllSize());
            model.addAttribute("listChatLieu", sanPhamChiTietService.getAllChatLieu());
            model.addAttribute("action", "edit");
            model.addAttribute("page", "san-pham-chi-tiet");
        });
        return "SanPhamChiTiet/form";
    }

    @PostMapping("/san-pham-chi-tiet/edit/{id}")
    public String adminEdit(@PathVariable Integer id, @ModelAttribute SanPhamChiTiet sanPhamChiTiet, RedirectAttributes redirectAttributes) {
        try {
            sanPhamChiTiet.setId(id);
            sanPhamChiTietService.save(sanPhamChiTiet);
            redirectAttributes.addFlashAttribute("message", "Cập nhật sản phẩm chi tiết thành công!");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }
        return "redirect:/san-pham-chi-tiet";
    }

    @GetMapping("/san-pham-chi-tiet/delete/{id}")
    public String adminDelete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            sanPhamChiTietService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Xóa sản phẩm chi tiết thành công!");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }
        return "redirect:/san-pham-chi-tiet";
    }
}