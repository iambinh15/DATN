package org.example.datn_sp26.NguoiDung.Controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Service.KhachHangService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/khach-hang")
public class KhachHangController {

    private final KhachHangService service;

    public KhachHangController(KhachHangService service) {
        this.service = service;
    }

    @GetMapping
    public String list(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model
    ) {
        model.addAttribute("list", service.search(keyword));
        model.addAttribute("keyword", keyword);
        return "khachhang/list";
    }

    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("kh", new KhachHang());
        return "khachhang/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("kh", service.getById(id));
        return "khachhang/form";
    }

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("kh") KhachHang kh,
            BindingResult result
    ) {

        if (result.hasErrors()) {
            return "khachhang/form";
        }

        try {
            service.save(kh);
        } catch (ValidationException e) {
            String msg = e.getMessage();

            if (msg.contains("Mã"))
                result.rejectValue("maKhachHang", "error.maKhachHang", msg);
            else if (msg.contains("Email"))
                result.rejectValue("email", "error.email", msg);
            else if (msg.contains("Số"))
                result.rejectValue("sdt", "error.sdt", msg);

            return "khachhang/form";
        }

        return "redirect:/khach-hang";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/khach-hang";
    }
}
