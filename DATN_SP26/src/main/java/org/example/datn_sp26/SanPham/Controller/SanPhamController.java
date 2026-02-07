package org.example.datn_sp26.SanPham.Controller;


import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.example.datn_sp26.SanPham.Repository.SanPhamRepository;
import org.example.datn_sp26.SanPham.Service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class SanPhamController {

    @Autowired
    private SanPhamRepository sanPhamRepo;

    @Autowired
    private SanPhamChiTietRepository spctRepo;
    
    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private org.example.datn_sp26.SanPham.Repository.HinhAnhRepository hinhAnhRepository;

    // ============ KHÁCH HÀNG ============
    
    // 1. TRANG CHỦ: Hiển thị danh sách sản phẩm chung
    @GetMapping("/khach-hang/trang-chu")
    public String trangChu(Model model) {
        model.addAttribute("listSanPham", sanPhamRepo.findAll());
        return "khachhang/trang-chu";
    }

    // 2. TRANG CHI TIẾT: Hiển thị 1 sản phẩm và các biến thể của nó
    @GetMapping("/khach-hang/san-pham/{id}")
    public String chiTiet(@PathVariable("id") Integer id, Model model) {
        // Lấy thông tin sản phẩm chính
        SanPham sp = sanPhamRepo.findById(id).orElse(null);

        // Lấy danh sách các biến thể (SPCT) của sản phẩm đó
        List<SanPhamChiTiet> listBienThe = spctRepo.findAllWithDetailsBySanPhamId(id);

        model.addAttribute("sp", sp);
        model.addAttribute("listBienThe", listBienThe);
        return "khachhang/chi-tiet";
    }
    
    // ============ ADMIN ============
    
    @GetMapping("/san-pham")
    public String adminIndex(Model model) {
        List<SanPham> listSanPham = sanPhamService.getAll();
        
        // Gắn ảnh đầu tiên cho mỗi sản phẩm để tránh random do Set
        listSanPham.forEach(sp -> {
            hinhAnhRepository.findFirstByIdSanPham_IdOrderByIdAsc(sp.getId())
                .ifPresent(hinhAnh -> sp.setFirstImage(hinhAnh.getHinhAnh()));
        });
        
        model.addAttribute("listSanPham", listSanPham);
        model.addAttribute("page", "san-pham");
        return "SanPham/index";
    }

    @GetMapping("/san-pham/add")
    public String adminShowAddForm(Model model) {
        model.addAttribute("sanPham", new SanPham());
        model.addAttribute("listThuongHieu", sanPhamService.getAllThuongHieu());
        model.addAttribute("action", "add");
        model.addAttribute("page", "san-pham");
        return "SanPham/form";
    }

    @PostMapping("/san-pham/add")
    public String adminAdd(@ModelAttribute SanPham sanPham, 
                          @RequestParam(value = "hinhAnhFile", required = false) MultipartFile file,
                          RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.saveProductWithImage(sanPham, file);
            redirectAttributes.addFlashAttribute("message", "Thêm sản phẩm thành công!");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }
        return "redirect:/san-pham";
    }

    @GetMapping("/san-pham/edit/{id}")
    public String adminShowEditForm(@PathVariable Integer id, Model model) {
        sanPhamService.getById(id).ifPresent(sp -> {
            model.addAttribute("sanPham", sp);
            model.addAttribute("listThuongHieu", sanPhamService.getAllThuongHieu());
            model.addAttribute("action", "edit");
            model.addAttribute("page", "san-pham");
            
            // Lấy ảnh hiện tại
            var hinhAnhs = hinhAnhRepository.findByIdSanPham_Id(id);
            if (!hinhAnhs.isEmpty()) {
                model.addAttribute("currentImage", hinhAnhs.get(0).getHinhAnh());
            }
        });
        return "SanPham/form";
    }

    @PostMapping("/san-pham/edit/{id}")
    public String adminEdit(@PathVariable Integer id, 
                           @ModelAttribute SanPham sanPham,
                           @RequestParam(value = "hinhAnhFile", required = false) MultipartFile file,
                           RedirectAttributes redirectAttributes) {
        try {
            sanPham.setId(id);
            sanPhamService.saveProductWithImage(sanPham, file);
            redirectAttributes.addFlashAttribute("message", "Cập nhật sản phẩm thành công!");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }
        return "redirect:/san-pham";
    }

    @GetMapping("/san-pham/delete/{id}")
    public String adminDelete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Xóa sản phẩm thành công!");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }
        return "redirect:/san-pham";
    }
}
