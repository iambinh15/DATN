package org.example.datn_sp26.SanPham.Controller;

import org.example.datn_sp26.SanPham.Entity.MauSac;
import org.example.datn_sp26.SanPham.Service.MauSacService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/mau-sac")
public class MauSacController {

    private final MauSacService service;

    public MauSacController(MauSacService service) {
        this.service = service;
    }

    // 📄 Danh sách
    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", service.findAll());
        return "MauSac/list";
    }

    // ➕ Thêm
    @GetMapping("/add")
    public String add(Model model) {
        MauSac ms = new MauSac();
        ms.setTrangThai(1);
        model.addAttribute("ms", ms);
        return "MauSac/form";
    }

    // ✏ Sửa
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("ms", service.findById(id));
        return "MauSac/form";
    }

    // 💾 Lưu
    @PostMapping("/save")
    public String save(@ModelAttribute MauSac ms) {
        service.save(ms);
        return "redirect:/mau-sac";
    }

    // ❌ Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/mau-sac";
    }
}
