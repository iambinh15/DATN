package org.example.datn_sp26.SanPham.Controller;

import jakarta.validation.Valid;
import org.example.datn_sp26.SanPham.Entity.ChatLieu;
import org.example.datn_sp26.SanPham.Service.ChatLieuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chat-lieu")
public class ChatLieuController {

    private final ChatLieuService chatLieuService;

    public ChatLieuController(ChatLieuService chatLieuService) {
        this.chatLieuService = chatLieuService;
    }

    // 📄 Danh sách
    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", chatLieuService.findAll());
        return "ChatLieu/list";
    }

    // ➕ Form thêm
    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("chatLieu", new ChatLieu());
        return "ChatLieu/form";
    }

    // ✏ Sửa
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("chatLieu", chatLieuService.findById(id));
        return "ChatLieu/form";
    }

    // 💾 Lưu (CÓ VALIDATE)
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("chatLieu") ChatLieu chatLieu,
            BindingResult result,
            Model model
    ) {

        // validate trùng tên
        if (chatLieuService.isTenChatLieuExists(chatLieu)) {
            result.rejectValue(
                    "tenChatLieu",
                    "duplicate",
                    "Tên chất liệu đã tồn tại"
            );
        }

        if (result.hasErrors()) {
            return "ChatLieu/form";
        }

        chatLieuService.save(chatLieu);
        return "redirect:/chat-lieu";
    }

    // ❌ Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        chatLieuService.delete(id);
        return "redirect:/chat-lieu";
    }
}
