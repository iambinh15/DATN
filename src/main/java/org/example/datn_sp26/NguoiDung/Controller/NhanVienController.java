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

    // READ
    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", service.getAll());
        return "nhanvien/list";
    }

    // CREATE FORM
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("nv", new NhanVien());
        return "nhanvien/form";
    }

    // UPDATE FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("nv", service.getById(id));
        return "nhanvien/form";
    }

    // SAVE (CREATE + UPDATE)
    @PostMapping("/save")
    public String save(@ModelAttribute("nv") NhanVien nv) {
        service.save(nv);
        return "redirect:/nhan-vien";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/nhan-vien";
    }
}