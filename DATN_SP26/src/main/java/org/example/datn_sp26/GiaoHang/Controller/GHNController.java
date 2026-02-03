package org.example.datn_sp26.GiaoHang.Controller;

import org.example.datn_sp26.GiaoHang.Service.GHNService;
import org.example.datn_sp26.BanHang.Service.GioHangService;
import org.example.datn_sp26.KhuyenMai.Service.MaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequestMapping("/khach-hang")
public class GHNController {

    @Autowired
    private GHNService ghnService;

    @Autowired
    private GioHangService gioHangService;
    @Autowired
    private MaGiamGiaService maGiamGiaService;
    @GetMapping("/thanh-toan")
    public String hienThiThanhToan(Model model) {

        Integer idKhachHang = 1; // TODO: lấy từ session/login

        // 1️⃣ Lấy giỏ hàng
        var items = gioHangService.layGioHangCuaKhach(idKhachHang);

        // 2️⃣ TÍNH TỔNG TIỀN (LẤY GIÁ TỪ SẢN PHẨM CHI TIẾT)
        long tongTienHang = items.stream()
                .mapToLong(i ->
                        i.getIdSanPhamChiTiet()
                                .getDonGia()
                                .multiply(BigDecimal.valueOf(i.getSoLuong()))
                                .longValue()
                )
                .sum();


        // 3️⃣ Địa chỉ khách hàng (demo)
        Integer toDistrictId = 1442;      // Quận Hoàn Kiếm
        String toWardCode = "1A0107";     // Phường Cửa Đông

        // 4️⃣ Gọi GHN tính phí ship
        String responseGHN = ghnService.tinhPhiShip(
                toDistrictId,
                toWardCode,
                1000,           // trọng lượng (gram)
                tongTienHang
        );
        // 5️⃣ 🔥 LẤY DANH SÁCH MÃ GIẢM GIÁ
        var dsMaGiamGia = maGiamGiaService.layMaDangHoatDong();
        // 5️⃣ Trả dữ liệu ra view
        model.addAttribute("tongTienHang", tongTienHang);
        model.addAttribute("dataGHN", responseGHN);
        model.addAttribute("dsMaGiamGia", dsMaGiamGia);
        return "KhachHang/xac-nhan-thanh-toan";
    }
}
