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

        // 1. Kiểm tra NULL hoặc RỖNG cho tất cả các trường bắt buộc
        if (km.getMa() == null || km.getMa().trim().isEmpty() ||
                km.getTenGiamGia() == null || km.getTenGiamGia().trim().isEmpty() ||
                km.getGiaTri() == null ||
                km.getNgayBatDau() == null ||
                km.getNgayKetThuc() == null ||
                km.getSoLuong() == null ||
                km.getGiamToiThieu() == null ||
                km.getTrangThai() == null) {

            ra.addFlashAttribute("error", "Lỗi: Không được để trống bất kỳ thông tin nào!");
            return "redirect:/admin/khuyen-mai";
        }

        // 2. Kiểm tra logic giá trị âm
        if (km.getGiaTri() < 0 || km.getSoLuong() < 0 || km.getGiamToiThieu() < 0) {
            ra.addFlashAttribute("error", "Lỗi: Các giá trị số không được âm!");
            return "redirect:/admin/khuyen-mai";
        }

        // 3. Kiểm tra logic % (không quá 100)
        if (km.getLoaiGiam() != null && km.getLoaiGiam() == 1) {
            if (km.getGiaTri() > 100) {
                ra.addFlashAttribute("error", "Lỗi: Phần trăm giảm giá tối đa là 100%!");
                return "redirect:/admin/khuyen-mai";
            }
        }

        // 4. Kiểm tra logic ngày tháng (Ngày kết thúc phải sau ngày bắt đầu)
        if (km.getNgayKetThuc().isBefore(km.getNgayBatDau())) {
            ra.addFlashAttribute("error", "Lỗi: Ngày kết thúc phải sau ngày bắt đầu!");
            return "redirect:/admin/khuyen-mai";
        }

        // 5. Mọi thứ hợp lệ mới thực hiện lưu
        try {
            repo.save(km);
            ra.addFlashAttribute("message", "Lưu thông tin khuyến mãi thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi hệ thống khi lưu dữ liệu!");
        }

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