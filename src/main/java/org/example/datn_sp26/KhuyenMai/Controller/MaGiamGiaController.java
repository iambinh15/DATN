package org.example.datn_sp26.KhuyenMai.Controller;

import org.example.datn_sp26.KhuyenMai.Entity.MaGiamGia;
import org.example.datn_sp26.KhuyenMai.Repository.MaGiamGiaRepository;
import org.example.datn_sp26.KhuyenMai.Service.MaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/khuyen-mai")
public class MaGiamGiaController {

    @Autowired
    private MaGiamGiaRepository repo;
    @Autowired
    private MaGiamGiaService maGiamGiaService;

    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("list", repo.findAll());
        model.addAttribute("km", new MaGiamGia());
        return "khuyen-mai";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("km") MaGiamGia km, RedirectAttributes ra) {
        repo.save(km);
        ra.addFlashAttribute("message", "Lưu thông tin khuyến mãi thành công!");
        return "redirect:/admin/khuyen-mai";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("km", repo.findById(id).orElse(new MaGiamGia()));
        model.addAttribute("list", repo.findAll());
        return "khuyen-mai";
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            repo.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Xóa thành công mã khuyến mãi!");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Gửi thông báo lỗi về nếu mã đã được sử dụng trong hóa đơn
            redirectAttributes.addFlashAttribute("error", "Không thể xóa! Mã này đã được sử dụng trong hóa đơn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/khuyen-mai";
    }
    @PostMapping("/ap-dung")
    public ResponseEntity<?> applyVoucher(@RequestParam String code, @RequestParam double total) {
        double giamGia = maGiamGiaService.tinhSoTienGiam(code, total);
        return ResponseEntity.ok(giamGia);
    }
}