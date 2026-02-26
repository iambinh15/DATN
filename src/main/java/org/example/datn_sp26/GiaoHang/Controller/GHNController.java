package org.example.datn_sp26.GiaoHang.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.datn_sp26.GiaoHang.Service.GHNService;
import org.example.datn_sp26.BanHang.Service.GioHangService;
import org.example.datn_sp26.KhuyenMai.Service.MaGiamGiaService;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Service.DiaChiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/khach-hang")
public class GHNController {

    @Autowired
    private GHNService ghnService;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private MaGiamGiaService maGiamGiaService;

    @Autowired
    private DiaChiService diaChiService;

    // =======================
    // 1️⃣ TRANG THANH TOÁN
    // =======================
    @GetMapping("/thanh-toan")
    public String hienThiThanhToan(Model model, HttpSession session) {
        // 1. Kiểm tra chính xác tên biến Session
        // Ghi chú: Hãy chắc chắn ở hàm Login bạn đã set: session.setAttribute("userLog", khách_hàng_đó)
        KhachHang kh = (KhachHang) session.getAttribute("khachHang");

        // DEBUG: Dòng này sẽ in ra console để bạn xem có lấy được khách hàng không
        if (kh == null) {
            System.out.println(">>> LỖI: Session 'userLog' bị NULL. Đang quay về trang Login.");
            return "redirect:/login";
        }

        System.out.println(">>> ĐANG THANH TOÁN CHO KHÁCH HÀNG ID: " + kh.getId());

        try {
            Integer idKhachHang = kh.getId();

            var items = gioHangService.layGioHangCuaKhach(idKhachHang);

            // Tính tổng tiền hàng
            long tongTienHang = items.stream()
                    .mapToLong(i ->
                            i.getIdSanPhamChiTiet()
                                    .getDonGia()
                                    .multiply(BigDecimal.valueOf(i.getSoLuong()))
                                    .longValue()
                    ).sum();

            var dsDiaChi = diaChiService.layDiaChiCuaKhach(idKhachHang);
            var dsMaGiamGia = maGiamGiaService.layMaDangHoatDong();

            model.addAttribute("tongTienHang", tongTienHang);
            model.addAttribute("dsDiaChi", dsDiaChi);
            model.addAttribute("dsMaGiamGia", dsMaGiamGia);

            return "KhachHang/xac-nhan-thanh-toan";

        } catch (Exception e) {
            e.printStackTrace(); // In lỗi chi tiết ra console nếu có lỗi logic bên trong
            return "redirect:/khach-hang/trang-chu";
        }
    }

    // =======================
    // 2️⃣ TÍNH PHÍ SHIP GHN
    // =======================
    @PostMapping("/tinh-phi-ship-dia-chi-moi")
    @ResponseBody
    public Map<String, Object> tinhPhiShip(@RequestBody Map<String, String> req) {

        Integer districtId = Integer.parseInt(req.get("districtId"));
        String wardCode = req.get("wardCode");

        String res = ghnService.tinhPhiShip(
                districtId,
                wardCode,
                1000,
                0L
        );

        long phiShip = ghnService.parsePhiShip(res);

        Map<String, Object> result = new HashMap<>();
        result.put("phiShip", phiShip);
        return result;
    }

    // =======================
    // 3️⃣ LƯU ĐỊA CHỈ TẠM
    // =======================
    @PostMapping("/luu-dia-chi-tam")
    @ResponseBody
    public void luuDiaChiTam(@RequestBody Map<String, Object> req,
                             HttpSession session) {

        String diaChi = (String) req.get("diaChi");
        Long phiShip = Long.valueOf(req.get("phiShip").toString());

        session.setAttribute("DIA_CHI_TAM", diaChi);
        session.setAttribute("PHI_SHIP", phiShip);
    }

    // =======================
    // 4️⃣ TẠO ĐƠN COD
    // =======================
    @PostMapping("/tao-don-cod")
    @ResponseBody
    public void taoDonCOD(HttpSession session) {

        Integer idKhachHang = 1; // TODO lấy từ session đăng nhập
        String diaChi = (String) session.getAttribute("DIA_CHI_TAM");

        if (diaChi == null) {
            throw new RuntimeException("Chưa có địa chỉ giao hàng");
        }

        // hoaDonService.taoHoaDon(idKhachHang, diaChi, "COD");

        session.removeAttribute("DIA_CHI_TAM");
        session.removeAttribute("PHI_SHIP");
    }
}
