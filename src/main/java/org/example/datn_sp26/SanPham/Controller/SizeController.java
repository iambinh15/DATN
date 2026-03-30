package org.example.datn_sp26.SanPham.Controller;

import org.example.datn_sp26.SanPham.Entity.Size;
import org.example.datn_sp26.SanPham.Service.SizeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/size")
public class SizeController {

    private final SizeService service;

    public SizeController(SizeService service) {
        this.service = service;
    }

    // 📄 Danh sách
    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", service.findAll());
        return "Size/list";
    }

    // ➕ Thêm
    @GetMapping("/add")
    public String add(Model model) {
        Size size = new Size();
        size.setTrangThai(1);
        model.addAttribute("sz", size);
        return "Size/form";
    }

    // ✏ Sửa
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("sz", service.findById(id));
        return "Size/form";
    }

    // 💾 Lưu
    @PostMapping("/save")
    public String save(@ModelAttribute Size sz) {
        service.save(sz);
        return "redirect:/size";
    }

    // ❌ Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/size";
    }
}
