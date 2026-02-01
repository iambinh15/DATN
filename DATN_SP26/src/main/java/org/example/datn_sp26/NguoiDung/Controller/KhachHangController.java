package org.example.datn_sp26.NguoiDung.Controller;

import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Service.KhachHangService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/khach-hang")
public class KhachHangController {

    private final KhachHangService service;

    public KhachHangController(KhachHangService service) {
        this.service = service;
    }

    // 📄 List
    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", service.findAll());
        return "KhachHang/list";
    }

    // ➕ Add
    @GetMapping("/add")
    public String add(Model model) {
        KhachHang kh = new KhachHang();
        kh.setNgayTao(new Date());
        kh.setTrangThai(1);
        model.addAttribute("kh", kh);
        return "KhachHang/form";
    }

    // ✏ Edit
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("kh", service.findById(id));
        return "KhachHang/form";
    }

    // 💾 Save
    @PostMapping("/save")
    public String save(@ModelAttribute KhachHang kh) {
        if (kh.getNgayTao() == null) {
            kh.setNgayTao(new Date());
        }
        service.save(kh);
        return "redirect:/khach-hang";
    }

    // ❌ Delete
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/khach-hang";
    }
}