package org.example.datn_sp26.NguoiDung.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.DiaChi; // Nhớ import Entity DiaChi
import org.example.datn_sp26.NguoiDung.Repository.KhachHangRepository;
import org.example.datn_sp26.NguoiDung.Repository.DiaChiRepository; // Nhớ import Repo DiaChi
import org.example.datn_sp26.NguoiDung.Service.KhachHangService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
            // Tìm đối tượng khách hàng từ DB
            KhachHang kh = khachHangRepository.findById(idKH).orElse(null);

            if (kh != null) {
                DiaChi moi = new DiaChi();
                moi.setDiaChi(diaChi);
                moi.setIdKhachHang(kh); // Gán cả đối tượng KhachHang vào
                moi.setTrangThai(1);    // 1: Hoạt động/Mặc định

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






    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", service.findAll());
        return "KhachHang/list";
    }

    @GetMapping("/add")
    public String add(Model model) {
        KhachHang kh = new KhachHang();
        kh.setNgayTao(Instant.now());
        kh.setTrangThai(1);
        model.addAttribute("kh", kh);
        return "KhachHang/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("kh", service.findById(id));
        return "KhachHang/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute KhachHang kh) {
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