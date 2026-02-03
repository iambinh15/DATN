package org.example.datn_sp26.SanPham.Controller;

import org.example.datn_sp26.SanPham.Entity.ChatLieu;
import org.example.datn_sp26.SanPham.Service.ChatLieuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chat-lieu")
public class ChatLieuController {

    private final ChatLieuService service;

    public ChatLieuController(ChatLieuService service) {
        this.service = service;
    }

    // 📄 Danh sách
    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", service.findAll());
        return "ChatLieu/list";
    }

    // ➕ Thêm
    @GetMapping("/add")
    public String add(Model model) {
        ChatLieu cl = new ChatLieu();
        cl.setTrangThai(1);
        model.addAttribute("cl", cl);
        return "ChatLieu/form";
    }

    // ✏ Sửa
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("cl", service.findById(id));
        return "ChatLieu/form";
    }

    // 💾 Lưu
    @PostMapping("/save")
    public String save(@ModelAttribute ChatLieu cl) {
        service.save(cl);
        return "redirect:/chat-lieu";
    }

    // ❌ Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/chat-lieu";
    }
}
