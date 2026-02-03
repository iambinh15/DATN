package org.example.datn_sp26.SanPham.Controller;

import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Service.ChatLieuService;
import org.example.datn_sp26.SanPham.Service.MauSacService;
import org.example.datn_sp26.SanPham.Service.SanPhamChiTietService;
import org.example.datn_sp26.SanPham.Service.SanPhamService;
import org.example.datn_sp26.SanPham.Service.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/san-pham-chi-tiet")
public class SanPhamChiTietController {
    
    @Autowired
    private SanPhamChiTietService sanPhamChiTietService;
    
    @Autowired
    private SanPhamService sanPhamService;
    
    @Autowired
    private MauSacService mauSacService;
    
    @Autowired
    private SizeService sizeService;
    
    @Autowired
    private ChatLieuService chatLieuService;
    
    // Hiển thị danh sách sản phẩm chi tiết
    @GetMapping
    public String index(Model model, @RequestParam(required = false) Integer sanPhamId) {
        if (sanPhamId != null) {
            model.addAttribute("chiTiets", sanPhamChiTietService.getChiTietBySanPhamId(sanPhamId));
            model.addAttribute("sanPhamId", sanPhamId);
        } else {
            model.addAttribute("chiTiets", sanPhamChiTietService.getAllSanPhamChiTiet());
        }
        model.addAttribute("sanPhams", sanPhamService.getSanPhamActive());
        return "sanphamchitiet/index";
    }
    
    // Hiển thị form thêm mới chi tiết
    @GetMapping("/create")
    public String create(Model model, @RequestParam(required = false) Integer sanPhamId) {
        model.addAttribute("chiTiet", new SanPhamChiTiet());
        model.addAttribute("sanPhams", sanPhamService.getSanPhamActive());
        model.addAttribute("mauSacs", mauSacService.getMauSacActive());
        model.addAttribute("sizes", sizeService.getSizeActive());
        model.addAttribute("chatLieus", chatLieuService.getChatLieuActive());
        
        if (sanPhamId != null) {
            model.addAttribute("selectedSanPhamId", sanPhamId);
        }
        
        return "sanphamchitiet/create";
    }
    
    // Xử lý thêm mới chi tiết
    @PostMapping("/create")
    public String store(@ModelAttribute SanPhamChiTiet chiTiet, RedirectAttributes redirectAttributes) {
        try {
            sanPhamChiTietService.saveSanPhamChiTiet(chiTiet);
            redirectAttributes.addFlashAttribute("success", "Thêm chi tiết sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Thêm chi tiết sản phẩm thất bại!");
        }
        return "redirect:/san-pham-chi-tiet";
    }
    
    // Hiển thị form cập nhật chi tiết
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Optional<SanPhamChiTiet> chiTiet = sanPhamChiTietService.getSanPhamChiTietById(id);
        if (chiTiet.isPresent()) {
            model.addAttribute("chiTiet", chiTiet.get());
            model.addAttribute("sanPhams", sanPhamService.getSanPhamActive());
            model.addAttribute("mauSacs", mauSacService.getMauSacActive());
            model.addAttribute("sizes", sizeService.getSizeActive());
            model.addAttribute("chatLieus", chatLieuService.getChatLieuActive());
            return "sanphamchitiet/edit";
        }
        return "redirect:/san-pham-chi-tiet";
    }
    
    // Xử lý cập nhật chi tiết
    @PostMapping("/edit/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute SanPhamChiTiet chiTiet, 
                        RedirectAttributes redirectAttributes) {
        try {
            chiTiet.setId(id);
            sanPhamChiTietService.saveSanPhamChiTiet(chiTiet);
            redirectAttributes.addFlashAttribute("success", "Cập nhật chi tiết sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật chi tiết sản phẩm thất bại!");
        }
        return "redirect:/san-pham-chi-tiet";
    }
    
    // Xóa chi tiết (soft delete)
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            sanPhamChiTietService.updateTrangThai(id, 0);
            redirectAttributes.addFlashAttribute("success", "Xóa chi tiết sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa chi tiết sản phẩm thất bại!");
        }
        return "redirect:/san-pham-chi-tiet";
    }
}
