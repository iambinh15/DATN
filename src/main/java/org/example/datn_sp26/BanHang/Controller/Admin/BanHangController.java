package org.example.datn_sp26.BanHang.Controller.Admin;

import jakarta.servlet.http.HttpSession;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.example.datn_sp26.BanHang.Entity.HoaDonChiTiet;
import org.example.datn_sp26.BanHang.Repository.HoaDonRepository;
import org.example.datn_sp26.BanHang.Service.HoaDonService;
import org.example.datn_sp26.NguoiDung.Service.KhachHangService;

import org.example.datn_sp26.KhuyenMai.Repository.MaGiamGiaRepository;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.example.datn_sp26.NguoiDung.Repository.KhachHangRepository;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.example.datn_sp26.DangNhap.repository.TaiKhoanDangNhapRepository;
import org.example.datn_sp26.DangNhap.repository.NhanVienDangNhapRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import org.example.datn_sp26.NguoiDung.Entity.TaiKhoan;
@Controller
@RequestMapping("/admin/ban-hang")
public class BanHangController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;
    @Autowired
    private KhachHangService khachHangService;
    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;
    @Autowired
    private TaiKhoanDangNhapRepository taiKhoanDangNhapRepository;

    @Autowired
    private NhanVienDangNhapRepository nhanVienDangNhapRepository;
    // 1. Hiển thị trang bán hàng (POS)
    @GetMapping
    public String hienThiBanHang(@RequestParam(value = "idHoaDon", required = false) Integer idHoaDon,
                                 Model model) {
        // Load danh sách hóa đơn chờ (Trạng thái "Chờ thanh toán")
        model.addAttribute("dsHoaDon", hoaDonService.layDsHoaDonTaiQuay());

        // Load danh sách sản phẩm
        model.addAttribute("dsSanPham", sanPhamChiTietRepository.findAll());

        // Load khách hàng & voucher
        model.addAttribute("dsKhachHang", khachHangRepository.findAll());
        model.addAttribute("dsVoucher", maGiamGiaRepository.findAll());

        // Nếu có hóa đơn được chọn, load chi tiết hóa đơn đó
        if (idHoaDon != null) {
            HoaDon hoaDon = hoaDonRepository.findById(idHoaDon).orElse(null);
            if (hoaDon != null) {
                model.addAttribute("selectedHoaDon", hoaDon);
                model.addAttribute("cartItems", hoaDonService.layChiTietHoaDon(idHoaDon));
            }
        }

        return "ban-hang";
    }

    // 2. Tạo hóa đơn mới
    @PostMapping("/tao-hoa-don")
    public String taoHoaDon(RedirectAttributes ra) {

        try {
            // Lấy thông tin đăng nhập từ Spring Security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
                ra.addFlashAttribute("errorMessage", "Bạn chưa đăng nhập!");
                return "redirect:/login";
            }

            String username = authentication.getName();

            // Tìm tài khoản
            Optional<TaiKhoan> taiKhoanOpt =
                    taiKhoanDangNhapRepository.findByTenDangNhap(username);

            if (taiKhoanOpt.isEmpty()) {
                ra.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản!");
                return "redirect:/login";
            }

            // Tìm nhân viên theo tài khoản
            Optional<NhanVien> nvOpt =
                    nhanVienDangNhapRepository.findByTaiKhoan(taiKhoanOpt.get());

            if (nvOpt.isEmpty()) {
                ra.addFlashAttribute("errorMessage", "Không tìm thấy nhân viên!");
                return "redirect:/login";
            }

            NhanVien nhanVien = nvOpt.get();

            // Tạo hóa đơn
            HoaDon hd = hoaDonService.taoHoaDonTaiQuay(nhanVien);

            return "redirect:/admin/ban-hang?idHoaDon=" + hd.getId();

        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi tạo hóa đơn: " + e.getMessage());
            return "redirect:/admin/ban-hang";
        }
    }

    // 3. Thêm sản phẩm vào hóa đơn
    @PostMapping("/add-product")
    public String themSanPham(@RequestParam("idHoaDon") Integer idHoaDon,
                              @RequestParam("spctId") Integer spctId,
                              @RequestParam("soLuong") Integer soLuong,
                              RedirectAttributes ra) {
        try {
            hoaDonService.themSanPhamVaoHoaDon(idHoaDon, spctId, soLuong);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/ban-hang?idHoaDon=" + idHoaDon;
    }

    // 4. Xóa sản phẩm khỏi hóa đơn
    @GetMapping("/remove-cart")
    public String xoaSanPham(@RequestParam("hdctId") Integer hdctId,
                             @RequestParam("idHoaDon") Integer idHoaDon,
                             RedirectAttributes ra) {
        try {
            hoaDonService.xoaSanPhamKhoiHoaDon(hdctId);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/ban-hang?idHoaDon=" + idHoaDon;
    }

    // 5. Cập nhật số lượng sản phẩm trong giỏ
    @PostMapping("/update-cart")
    public String capNhatSoLuong(@RequestParam("hdctId") Integer hdctId,
                                 @RequestParam("soLuong") Integer soLuong,
                                 @RequestParam("idHoaDon") Integer idHoaDon,
                                 RedirectAttributes ra) {
        try {
            hoaDonService.capNhatSoLuongChiTiet(hdctId, soLuong);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/ban-hang?idHoaDon=" + idHoaDon;
    }

    // 6. Xử lý thanh toán
    @PostMapping("/thanh-toan")
    public String thanhToan(@RequestParam("hoaDonId") Integer hoaDonId,
                            @RequestParam(value = "khachHangId", required = false) Integer khachHangId,
                            @RequestParam(value = "voucherId", required = false) Integer voucherId,
                            @RequestParam("phuongThuc") String phuongThuc,
                            @RequestParam("tienKhachDua") String tienKhachDuaStr,
                            RedirectAttributes ra) {
        try {
            // Loại bỏ dấu chấm/phẩy trong chuỗi tiền tệ (ví dụ: 100.000 -> 100000)
            BigDecimal tienKhachDua = new BigDecimal(tienKhachDuaStr.replaceAll("[^\\d]", ""));
            
            hoaDonService.thanhToanTaiQuay(hoaDonId, khachHangId, voucherId, phuongThuc, tienKhachDua);
            ra.addFlashAttribute("successMessage", "Thanh toán thành công hóa đơn!");
            return "redirect:/admin/ban-hang";
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("errorMessage", "Lỗi thanh toán: " + e.getMessage());
            return "redirect:/admin/ban-hang?idHoaDon=" + hoaDonId;
        }
    }
    @PostMapping("/them-khach-hang-nhanh")
    @ResponseBody
    public Map<String, Object> themKhachHangNhanh(@RequestBody Map<String, String> data) {

        Map<String, Object> response = new HashMap<>();

        try {
            String ten = data.get("tenKhachHang");
            String sdt = data.get("sdt");
            String diaChi = data.get("diaChi");
            KhachHang kh = khachHangService.themNhanh(ten, sdt,diaChi);

            response.put("success", true);
            response.put("id", kh.getId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }
}