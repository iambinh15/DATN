package org.example.datn_sp26.NguoiDung.Controller;

import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.example.datn_sp26.NguoiDung.Service.NhanVienService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/nhan-vien")
public class NhanVienController {

    private final NhanVienService service;

    public NhanVienController(NhanVienService service) {
        this.service = service;
    }

    // READ + SEARCH
    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {
        model.addAttribute("list", service.search(keyword));
        model.addAttribute("keyword", keyword);
        return "nhanvien/list";
    }

    // CREATE FORM
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("nv", new NhanVien());
        model.addAttribute("error", null);
        return "nhanvien/form";
    }

    // UPDATE FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("nv", service.getById(id));
        model.addAttribute("error", null);
        return "nhanvien/form";
    }

    // SAVE (CREATE + UPDATE) + VALIDATE
    @PostMapping("/save")
    public String save(@ModelAttribute("nv") NhanVien nv, Model model) {

        // Validate rỗng
        if (nv.getMaNhanVien() == null || nv.getMaNhanVien().trim().isEmpty()
                || nv.getTenNhanVien() == null || nv.getTenNhanVien().trim().isEmpty()
                || nv.getSdt() == null || nv.getSdt().trim().isEmpty()
                || nv.getEmail() == null || nv.getEmail().trim().isEmpty()) {

            model.addAttribute("nv", nv);
            model.addAttribute("error", "Mã NV, Tên, SĐT và Email không được để trống.");
            return "nhanvien/form";
        }

        // Validate trùng mã nhân viên
        if (service.isMaNhanVienTrung(nv.getMaNhanVien(), nv.getId())) {
            model.addAttribute("nv", nv);
            model.addAttribute("error", "Mã nhân viên đã tồn tại, vui lòng chọn mã khác.");
            return "nhanvien/form";
        }

        // Validate SĐT phải là số và đủ 10 chữ số
        if (!nv.getSdt().trim().matches("\\d{10}")) {
            model.addAttribute("nv", nv);
            model.addAttribute("error", "Số điện thoại phải đủ 10 chữ số.");
            return "nhanvien/form";
        }

        // Validate email phải có @gmail.com
        if (!nv.getEmail().trim().toLowerCase().contains("@gmail.com")) {
            model.addAttribute("nv", nv);
            model.addAttribute("error", "Email phải có đuôi @gmail.com.");
            return "nhanvien/form";
        }

        service.save(nv);
        return "redirect:/nhan-vien";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/nhan-vien";
    }
}