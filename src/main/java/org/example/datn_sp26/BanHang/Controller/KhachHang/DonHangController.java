package org.example.datn_sp26.BanHang.Controller.KhachHang;

import jakarta.servlet.http.HttpSession;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.example.datn_sp26.BanHang.Entity.HoaDonChiTiet;
import org.example.datn_sp26.BanHang.Repository.HoaDonChiTietRepository;
import org.example.datn_sp26.BanHang.Repository.HoaDonRepository;
import org.example.datn_sp26.BanHang.Service.HoaDonService;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/khach-hang/don-hang")
public class DonHangController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    // Helper method để lấy idKhachHang từ session
    private Integer getIdKhachHang(HttpSession session) {
        return (Integer) session.getAttribute("idKhachHang");
    }

    @GetMapping
    public String xemDonHang(Model model, HttpSession session) {
        Integer idKhachHang = getIdKhachHang(session);

        if (idKhachHang == null) {
            return "redirect:/login";
        }

        List<HoaDon> danhSachDonHang = hoaDonService.layDonHangCuaKhach(idKhachHang);
        model.addAttribute("donHangs", danhSachDonHang);
        return "KhachHang/don-hang";
    }
    @PostMapping("/huy/{id}")
    @ResponseBody
    public ResponseEntity<String> huyDon(@PathVariable Integer id,
                                         @RequestBody java.util.Map<String, String> body) {

        try {
            String lyDoHuy = body.get("lyDoHuy");
            hoaDonService.huyDonHangVaHoanKho(id, lyDoHuy);
            return ResponseEntity.ok("Hủy đơn thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể hủy đơn: " + e.getMessage());
        }
    }
    // API: Lấy toàn bộ thông tin hóa đơn + chi tiết sản phẩm cho khách hàng (JSON)
    @GetMapping("/chi-tiet/{id}")
    @ResponseBody
    public ResponseEntity<?> getChiTietHoaDon(@PathVariable Integer id) {
        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();

        // 1. Thông tin hóa đơn
        HoaDon hd = hoaDonRepository.findById(id).orElse(null);
        if (hd == null) {
            return ResponseEntity.notFound().build();
        }

        response.put("maHoaDon", hd.getMaHoaDon() != null ? hd.getMaHoaDon() : "—");
        response.put("tenKhachHang", hd.getIdKhachHang() != null ? hd.getIdKhachHang().getTenKhachHang() : "—");
        response.put("maGiamGia", hd.getIdMaGiamGia() != null ? hd.getIdMaGiamGia().getMa() : "Không có");
        response.put("loaiThanhToan", hd.getIdLoaiThanhToan() != null ? hd.getIdLoaiThanhToan().getTenLoai() : "—");
        response.put("trangThai", hd.getIdTrangThaiHoaDon() != null ? hd.getIdTrangThaiHoaDon().getTenTrangThai() : "—");
        response.put("diaChi", hd.getDiaChi() != null ? hd.getDiaChi() : "—");
        response.put("ngayTao", hd.getNgayTao() != null ? hd.getNgayTao().toString() : "—");
        response.put("tongThanhToan", hd.getTongThanhToan());

        // 2. Chi tiết sản phẩm
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonIdWithFullDetails(id);
        List<java.util.Map<String, Object>> sanPhamList = new java.util.ArrayList<>();
        for (HoaDonChiTiet ct : chiTietList) {
            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
            SanPhamChiTiet spct = ct.getIdSanPhamChiTiet();
            item.put("tenSanPham", spct != null && spct.getIdSanPham() != null ? spct.getIdSanPham().getTenSanPham() : "—");
            item.put("mauSac", spct != null && spct.getIdMauSac() != null ? spct.getIdMauSac().getTenMau() : "—");
            item.put("size", spct != null && spct.getIdSize() != null ? spct.getIdSize().getTenSize() : "—");
            item.put("chatLieu", spct != null && spct.getIdChatLieu() != null ? spct.getIdChatLieu().getTenChatLieu() : "—");
            item.put("donGia", ct.getDonGia());
            item.put("soLuong", ct.getSoLuong());
            sanPhamList.add(item);
        }
        response.put("chiTietSanPham", sanPhamList);

        return ResponseEntity.ok(response);
    }

}