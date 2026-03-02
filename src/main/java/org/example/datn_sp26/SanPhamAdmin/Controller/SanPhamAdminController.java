package org.example.datn_sp26.SanPhamAdmin.Controller;

import lombok.RequiredArgsConstructor;
import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Repository.SanPhamRepository;
import org.example.datn_sp26.SanPham.Repository.ThuongHieuRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/admin/san-pham")
@RequiredArgsConstructor
public class SanPhamAdminController {

    private final SanPhamRepository sanPhamRepository;
    private final ThuongHieuRepository thuongHieuRepository; // ✅ thêm dòng này
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    // ================== HIỂN THỊ DANH SÁCH ==================
    @GetMapping
    public String index(Model model) {
        model.addAttribute("list", sanPhamRepository.findByTrangThai(1));
        return "SanPham/index";
    }

    // ================== FORM THÊM ==================
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("sanPham", new SanPham());
        model.addAttribute("listThuongHieu",
                thuongHieuRepository.findByTrangThai(1)); // ✅ gọi đúng cách
        return "SanPham/create";
    }

    // ================== LƯU ==================
    @PostMapping("/save")
    public String save(@ModelAttribute SanPham sanPham) {

        // Nếu chưa chọn trạng thái thì mặc định = 1
        if (sanPham.getTrangThai() == null) {
            sanPham.setTrangThai(1);
        }

        sanPhamRepository.save(sanPham);
        return "redirect:/admin/san-pham";
    }

    // ================== XOÁ ==================
    @GetMapping("/delete/{id}")
    @Transactional
    public String delete(@PathVariable Integer id) {

        sanPhamChiTietRepository.deleteByIdSanPham_Id(id);

        sanPhamRepository.deleteById(id);

        return "redirect:/admin/san-pham";
    }
}