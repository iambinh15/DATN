package org.example.datn_sp26.NguoiDung.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.DiaChi; // Nhớ import Entity DiaChi
import org.example.datn_sp26.NguoiDung.Repository.KhachHangRepository;
import org.example.datn_sp26.NguoiDung.Repository.DiaChiRepository; // Nhớ import Repo DiaChi
import org.example.datn_sp26.NguoiDung.Service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/khach-hang")
public class KhachHangController {

    private final KhachHangService service;
    private final KhachHangRepository khachHangRepository;
    private final DiaChiRepository diaChiRepository; // 1. Thêm Repository DiaChi

    // 2. Cập nhật Constructor để Spring tiêm DiaChiRepository vào
    public KhachHangController(KhachHangService service,
                               KhachHangRepository khachHangRepository,
                               DiaChiRepository diaChiRepository) {
        this.service = service;
        this.khachHangRepository = khachHangRepository;
        this.diaChiRepository = diaChiRepository;
    }

    @GetMapping("/thong-tin-ca-nhan")
    public String xemThongTin(HttpSession session, Model model) {
        Integer idDangNhap = (Integer) session.getAttribute("idKhachHang");

        if (idDangNhap == null) {
            return "redirect:/login";
        }

        KhachHang kh = khachHangRepository.findById(idDangNhap).orElse(null);

        if (kh != null) {
            // Sửa lỗi hiển thị ngày: Định dạng Instant thành String tại đây
            if (kh.getNgayTao() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .withZone(ZoneId.systemDefault());
                model.addAttribute("ngayTaoFormat", formatter.format(kh.getNgayTao()));
            }

            List<DiaChi> listDiaChi = diaChiRepository.findByIdKhachHang_Id(idDangNhap);
            model.addAttribute("khachHang", kh);
            model.addAttribute("listDiaChi", listDiaChi);

            return "KhachHang/ThongtinKH";
        }

        return "redirect:/login";
    }

    @PostMapping("/dia-chi/add")
    public String addDiaChi(@RequestParam("diaChi") String diaChi, HttpSession session) {
        Integer idKH = (Integer) session.getAttribute("idKhachHang");

        if (idKH != null && diaChi != null && !diaChi.trim().isEmpty()) {
            KhachHang kh = khachHangRepository.findById(idKH).orElse(null);

            if (kh != null) {
                DiaChi moi = new DiaChi();
                moi.setDiaChi(diaChi);
                moi.setIdKhachHang(kh);

                // --- LOGIC TỰ ĐỘNG PHÂN LOẠI 0 VÀ 1 ---
                List<DiaChi> listHienTai = diaChiRepository.findByIdKhachHang_Id(idKH);

                if (listHienTai == null || listHienTai.isEmpty()) {
                    // Chưa có địa chỉ nào -> Cái này là mặc định
                    moi.setTrangThai(1);
                } else {
                    // Đã có địa chỉ rồi -> Cái mới thêm luôn là 0
                    moi.setTrangThai(0);
                }
                // --------------------------------------

                diaChiRepository.save(moi);
            }
        }
        return "redirect:/khach-hang/thong-tin-ca-nhan";
    }
    @GetMapping("/dia-chi/delete/{id}")
    public String deleteDiaChi(@PathVariable("id") Integer id, HttpSession session) {
        Integer idKH = (Integer) session.getAttribute("idKhachHang");
        DiaChi dc = diaChiRepository.findById(id).orElse(null);

        // Chỉ xóa nếu địa chỉ tồn tại và thuộc về khách hàng đang đăng nhập
        if (dc != null && dc.getIdKhachHang().getId().equals(idKH)) {
            diaChiRepository.delete(dc);
        }
        return "redirect:/khach-hang/thong-tin-ca-nhan";
    }
    @Autowired
    private KhachHangService khachHangService;

    @PostMapping("/admin/ban-hang/them-khach-hang-nhanh")
    @ResponseBody
    public Map<String, Object> themKhachHangNhanh(@RequestBody Map<String, String> req) {

        Map<String, Object> result = new HashMap<>();

        try {
            KhachHang kh = khachHangService.themNhanh(
                    req.get("tenKhachHang"),
                    req.get("sdt"),
                    req.get("diaChi")
            );

            result.put("success", true);
            result.put("id", kh.getId());
            result.put("ten", kh.getTenKhachHang());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }






    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {
        model.addAttribute("list", service.search(keyword));
        model.addAttribute("keyword", keyword);
        return "KhachHang/list";
    }

    @GetMapping("/add")
    public String add(Model model) {
        KhachHang kh = new KhachHang();
        kh.setNgayTao(Instant.now());
        kh.setTrangThai(1);
        model.addAttribute("kh", kh);
        model.addAttribute("error", null);
        return "KhachHang/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("kh", service.findById(id));
        model.addAttribute("error", null);
        return "KhachHang/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute KhachHang kh, Model model) {

        // Validate rỗng
        if (kh.getMaKhachHang() == null || kh.getMaKhachHang().trim().isEmpty()
                || kh.getTenKhachHang() == null || kh.getTenKhachHang().trim().isEmpty()
                || kh.getSdt() == null || kh.getSdt().trim().isEmpty()
                || kh.getEmail() == null || kh.getEmail().trim().isEmpty()) {

            model.addAttribute("kh", kh);
            model.addAttribute("error", "Mã KH, Tên, SĐT và Email không được để trống.");
            return "KhachHang/form";
        }

        // Validate trùng mã khách hàng
        if (service.isMaKhachHangTrung(kh.getMaKhachHang(), kh.getId())) {
            model.addAttribute("kh", kh);
            model.addAttribute("error", "Mã khách hàng đã tồn tại, vui lòng chọn mã khác.");
            return "KhachHang/form";
        }

        // Validate SĐT phải là số và đủ 10 chữ số
        if (!kh.getSdt().trim().matches("\\d{10}")) {
            model.addAttribute("kh", kh);
            model.addAttribute("error", "Số điện thoại phải đủ 10 chữ số.");
            return "KhachHang/form";
        }

        // Validate email phải có @gmail.com
        if (!kh.getEmail().trim().toLowerCase().contains("@gmail.com")) {
            model.addAttribute("kh", kh);
            model.addAttribute("error", "Email phải có đuôi @gmail.com.");
            return "KhachHang/form";
        }

        if (kh.getNgayTao() == null) {
            kh.setNgayTao(Instant.now());
        }

        service.save(kh);
        return "redirect:/khach-hang";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/khach-hang";
    }
}