package org.example.datn_sp26.SanPham.Controller;

import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Service.SanPhamService;
import org.example.datn_sp26.SanPham.Service.ThuongHieuService;
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
@RequestMapping("/san-pham")
public class SanPhamController {
    
    @Autowired
    private SanPhamService sanPhamService;
    
    @Autowired
    private ThuongHieuService thuongHieuService;
    
    // Hiển thị danh sách sản phẩm
    @GetMapping
    public String index(Model model, @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("sanPhams", sanPhamService.searchSanPham(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("sanPhams", sanPhamService.getAllSanPham());
        }
        return "sanpham/index";
    }
    
    // Hiển thị form thêm mới sản phẩm
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("sanPham", new SanPham());
        model.addAttribute("thuongHieus", thuongHieuService.getThuongHieuActive());
        return "sanpham/create";
    }
    
    // Xử lý thêm mới sản phẩm
    @PostMapping("/create")
    public String store(@ModelAttribute SanPham sanPham, RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.saveSanPham(sanPham);
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Thêm sản phẩm thất bại!");
        }
        return "redirect:/san-pham";
    }
    
    // Hiển thị form cập nhật sản phẩm
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Optional<SanPham> sanPham = sanPhamService.getSanPhamById(id);
        if (sanPham.isPresent()) {
            model.addAttribute("sanPham", sanPham.get());
            model.addAttribute("thuongHieus", thuongHieuService.getThuongHieuActive());
            return "sanpham/edit";
        }
        return "redirect:/san-pham";
    }
    
    // Xử lý cập nhật sản phẩm
    @PostMapping("/edit/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute SanPham sanPham, 
                        RedirectAttributes redirectAttributes) {
        try {
            sanPham.setId(id);
            sanPhamService.saveSanPham(sanPham);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật sản phẩm thất bại!");
        }
        return "redirect:/san-pham";
    }
    
    // Xóa sản phẩm (soft delete - đổi trạng thái)
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.updateTrangThai(id, 0);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa sản phẩm thất bại!");
        }
        return "redirect:/san-pham";
    }
    
    // Xem chi tiết sản phẩm
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Optional<SanPham> sanPham = sanPhamService.getSanPhamById(id);
        if (sanPham.isPresent()) {
            model.addAttribute("sanPham", sanPham.get());
            return "sanpham/detail";
        }
        return "redirect:/san-pham";
    }
}
