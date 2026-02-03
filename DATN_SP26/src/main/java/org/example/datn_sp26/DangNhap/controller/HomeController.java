package org.example.datn_sp26.DangNhap.controller;

import org.example.datn_sp26.DangNhap.dto.RegisterRequest;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.example.datn_sp26.NguoiDung.Entity.TaiKhoan;
import org.example.datn_sp26.DangNhap.repository.KhachHangRepository;
import org.example.datn_sp26.DangNhap.repository.NhanVienRepository;
import org.example.datn_sp26.DangNhap.repository.TaiKhoanRepository;
import org.example.datn_sp26.DangNhap.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private AuthService authService;

    @GetMapping("/home")
    public String home(Model model) {
        addUserToModel(model);
        return "index";
    }

    @GetMapping("/ao-khoac")
    public String aoKhoac(Model model) {
        addUserToModel(model);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "DangNhap/login";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest request, RedirectAttributes redirectAttributes) {
        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login#register";
        }
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            authService.forgotPassword(email);
            redirectAttributes.addFlashAttribute("successMessage", "Mật khẩu mới đã được gửi đến email của bạn.");
            return "redirect:/login"; // Quay lại trang login để hiển thị thông báo
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Quay lại form quên mật khẩu (bằng JS ở client sẽ tự mở tab/form tương ứng)
            // Ở đây mình redirect về login và dùng JS để mở form forgot
            return "redirect:/login#forgot";
        }
    }

    private void addUserToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String username = authentication.getName();
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findByTenDangNhap(username);

            if (taiKhoanOpt.isPresent()) {
                TaiKhoan taiKhoan = taiKhoanOpt.get();
                String role = taiKhoan.getVaiTro().getMaVaiTro();
                String displayName = username;

                if ("USER".equals(role)) {
                    Optional<KhachHang> kh = khachHangRepository.findByTaiKhoan(taiKhoan);
                    if (kh.isPresent()) {
                        displayName = kh.get().getTen();
                    }
                } else if ("ADMIN".equals(role) || "STAFF".equals(role)) {
                    Optional<NhanVien> nv = nhanVienRepository.findByTaiKhoan(taiKhoan);
                    if (nv.isPresent()) {
                        displayName = nv.get().getTen();
                    }
                }

                model.addAttribute("currentUser", displayName);
                model.addAttribute("isLoggedIn", true);
            }
        } else {
            model.addAttribute("isLoggedIn", false);
        }
    }
}
