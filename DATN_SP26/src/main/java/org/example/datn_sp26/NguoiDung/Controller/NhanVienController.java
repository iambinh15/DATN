package org.example.datn_sp26.NguoiDung.Controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.example.datn_sp26.NguoiDung.Service.NhanVienService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/nhan-vien")
public class NhanVienController {

    private final NhanVienService service;

    public NhanVienController(NhanVienService service) {
        this.service = service;
    }

    // 📄 LIST
    @GetMapping
    public String list(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model
    ) {
        model.addAttribute("list", service.search(keyword));
        model.addAttribute("keyword", keyword);
        return "nhanvien/list";
    }

    // ➕ ADD
    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("nv", new NhanVien());
        return "nhanvien/form";
    }

    // ✏ EDIT
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("nv", service.getById(id));
        return "nhanvien/form";
    }

    // 💾 SAVE
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("nv") NhanVien nv,
            BindingResult result,
            Model model
    ) {

        // 1️⃣ Lỗi validate @NotBlank, @Size...
        if (result.hasErrors()) {
            return "nhanvien/form";
        }

        // 2️⃣ Lỗi trùng (mã / email / sdt)
        try {
            service.save(nv);
        } catch (ValidationException e) {

            String msg = e.getMessage();

            if (msg.contains("Mã")) {
                result.rejectValue("maNhanVien", "error.maNhanVien", msg);
            }
            else if (msg.contains("Email")) {
                result.rejectValue("email", "error.email", msg);
            }
            else if (msg.contains("Số")) {
                result.rejectValue("sdt", "error.sdt", msg);
            }

            return "nhanvien/form";
        }

        return "redirect:/nhan-vien";
    }


    // ❌ DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/nhan-vien";
    }
}