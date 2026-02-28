package org.example.datn_sp26.GiaoHang.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.example.datn_sp26.BanHang.Repository.HoaDonRepository;
import org.example.datn_sp26.BanHang.Service.HoaDonService;
import org.example.datn_sp26.GiaoHang.Service.GHNService;
import org.example.datn_sp26.BanHang.Service.GioHangService;
import org.example.datn_sp26.KhuyenMai.Entity.MaGiamGia;
import org.example.datn_sp26.KhuyenMai.Repository.MaGiamGiaRepository;
import org.example.datn_sp26.KhuyenMai.Service.MaGiamGiaService;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Service.DiaChiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Autowired
    private HoaDonService hoaDonService;
    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;
    @Autowired
    private HoaDonRepository hoaDonRepository;

    // =======================
    // 1️⃣ TRANG THANH TOÁN
    // =======================
    @GetMapping("/thanh-toan")
    public String hienThiThanhToan(@RequestParam(value = "selectedIds", required = false) List<Integer> selectedIds,
            Model model, HttpSession session) {
        KhachHang kh = (KhachHang) session.getAttribute("khachHang");

        if (kh == null) {
            System.out.println(">>> LỖI: Session 'userLog' bị NULL. Đang quay về trang Login.");
            return "redirect:/login";
        }

        System.out.println(">>> ĐANG THANH TOÁN CHO KHÁCH HÀNG ID: " + kh.getId());

        try {
            Integer idKhachHang = kh.getId();

            var allItems = gioHangService.layGioHangCuaKhach(idKhachHang);

            // Nếu không có selectedIds hoặc rỗng, lấy tất cả
            List<Integer> finalSelectedIds;
            if (selectedIds != null && !selectedIds.isEmpty()) {
                finalSelectedIds = selectedIds;
            } else {
                finalSelectedIds = allItems.stream()
                        .map(i -> i.getId())
                        .collect(Collectors.toList());
            }

            // Lưu vào session để dùng khi tạo đơn hàng
            session.setAttribute("SELECTED_IDS", finalSelectedIds);

            // Chỉ tính tổng tiền cho các sản phẩm được chọn
            var selectedItems = allItems.stream()
                    .filter(i -> finalSelectedIds.contains(i.getId()))
                    .collect(Collectors.toList());

            long tongTienHang = selectedItems.stream()
                    .mapToLong(i -> i.getIdSanPhamChiTiet()
                            .getDonGia()
                            .multiply(BigDecimal.valueOf(i.getSoLuong()))
                            .longValue())
                    .sum();

            var dsDiaChi = diaChiService.layDiaChiCuaKhach(idKhachHang);
            var dsMaGiamGia = maGiamGiaService.layMaDangHoatDong();

            model.addAttribute("tongTienHang", tongTienHang);
            model.addAttribute("dsDiaChi", dsDiaChi);
            model.addAttribute("dsMaGiamGia", dsMaGiamGia);

            return "KhachHang/xac-nhan-thanh-toan";

        } catch (Exception e) {
            e.printStackTrace();
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
                0L);

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
    // =======================
// 4️⃣ TẠO ĐƠN COD
// =======================
    @PostMapping("/tao-don-cod")
    @ResponseBody
    public Map<String, Object> taoDonCOD(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // 1. Kiểm tra session khách hàng
        KhachHang khachHang = (KhachHang) session.getAttribute("khachHang");
        if (khachHang == null) {
            System.out.println("===> [CẢNH BÁO]: Phiên đăng nhập (session) khách hàng bị null!");
            response.put("success", false);
            response.put("message", "Phiên đăng nhập hết hạn");
            return response;
        }

        // 2. LẤY MÃ VOUCHER TỪ SESSION (ĐÃ FIX TÊN ATTRIBUTE)
        // Lưu ý: Tên này phải khớp 100% với tên bạn dùng ở hàm 'apDungVoucher' trong Controller
        String maVoucher = (String) session.getAttribute("MA_GIAM_GIA_DA_CHON");

        // THÔNG BÁO KIỂM TRA MÃ TRƯỚC KHI GỬI XUỐNG SERVICE
        System.out.println("===> [CONTROLLER]: Đang lấy mã từ Session: [" + maVoucher + "]");

        // 3. Lấy thông tin địa chỉ và phí ship
        Object diaChiObj = session.getAttribute("DIA_CHI_TAM");
        Object phiShipObj = session.getAttribute("PHI_SHIP");

        if (diaChiObj == null || phiShipObj == null) {
            System.out.println("===> [LỖI]: Thiếu DIA_CHI_TAM hoặc PHI_SHIP trong session!");
            response.put("success", false);
            response.put("message", "Thiếu thông tin địa chỉ hoặc phí ship");
            return response;
        }

        String diaChi = diaChiObj.toString();
        // Chống lỗi NumberFormatException nếu phiShipObj không phải chuỗi số thuần túy
        BigDecimal phiShip = new BigDecimal(phiShipObj.toString().replaceAll("[^\\d.]", ""));

        // 4. Tính tổng tiền hàng từ giỏ hàng
        var items = gioHangService.layGioHangCuaKhach(khachHang.getId());
        if (items.isEmpty()) {
            response.put("success", false);
            response.put("message", "Giỏ hàng của bạn đang trống!");
            return response;
        }

        BigDecimal tongTienHang = items.stream()
                .map(i -> i.getIdSanPhamChiTiet().getDonGia().multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Tổng thanh toán tạm tính (Tiền hàng + Ship)
        // Lưu ý: Bạn truyền tổng chưa giảm xuống, Service sẽ lo phần trừ tiền dựa trên mã voucher
        BigDecimal tongThanhToan = tongTienHang.add(phiShip);

        System.out.println("===> [CONTROLLER]: Tổng tạm tính (Hàng + Ship): " + tongThanhToan);

        try {
            // ✅ GỌI SERVICE: Truyền đủ 4 tham số.
            // Logic 'xuLyVoucher' bên trong Service sẽ gán Voucher ID vào DB và trừ tiền.
            HoaDon hoaDon = hoaDonService.taoHoaDonCODDayDu(khachHang, tongThanhToan, diaChi, maVoucher);

            // THÔNG BÁO THÀNH CÔNG TRONG CONSOLE
            System.out.println("===> [THÀNH CÔNG]: Đã tạo hóa đơn: " + hoaDon.getMaHoaDon());

            // 6. Xóa các session tạm sau khi tạo đơn thành công để tránh trùng lặp cho đơn sau
            session.removeAttribute("MA_GIAM_GIA_DA_CHON");
            session.removeAttribute("DIA_CHI_TAM");
            session.removeAttribute("PHI_SHIP");

            response.put("success", true);
            response.put("message", "Đặt hàng thành công!");
        } catch (Exception e) {
            // THÔNG BÁO LỖI CHI TIẾT
            System.err.println("===> [LỖI TẠO ĐƠN]: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi khi tạo hóa đơn: " + e.getMessage());
        }

        return response;
    }


    @PostMapping("/ap-dung-voucher-session")
    @ResponseBody
    public Map<String, Object> apDungVoucher(@RequestParam("maVoucher") String maVoucher, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // 1. Lấy khách hàng từ session
        KhachHang kh = (KhachHang) session.getAttribute("khachHang");
        if (kh == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để dùng mã!");
            return response;
        }

        // 2. Tìm mã trong DB
        Optional<MaGiamGia> vOpt = maGiamGiaRepository.findByMa(maVoucher.trim());

        if (vOpt.isPresent()) {
            MaGiamGia voucher = vOpt.get();


            if (voucher.getTrangThai() == null || voucher.getTrangThai() != 1) {
                response.put("success", false);
                response.put("message", "Mã giảm giá này hiện không khả dụng!");
                return response;
            }

            // 4. Kiểm tra số lượng tổng trong kho
            if (voucher.getSoLuong() <= 0) {
                response.put("success", false);
                response.put("message", "Mã giảm giá này đã hết lượt sử dụng!");
                return response;
            }

            // 5. LOGIC DÙNG 1 LẦN cho mã "NGUOIMOI"
            if (maVoucher.toUpperCase().startsWith("NGUOIMOI")) {
                // Sử dụng hàm checkVoucherDaDungThatSu đã khai báo ở Bước 1
                // Hàm này sẽ bỏ qua các hóa đơn đã hủy, giúp khách dùng lại được mã
                boolean daDung = hoaDonRepository.checkVoucherDaDungThatSu(kh.getId(), voucher.getId());

                if (daDung) {
                    response.put("success", false);
                    response.put("message", "Bạn đã sử dụng mã chào mừng này rồi!");
                    return response;
                }
            }

            // 6. Áp dụng thành công
            session.setAttribute("MA_GIAM_GIA_DA_CHON", maVoucher);
            response.put("success", true);
            response.put("message", "Áp dụng thành công!");

        } else {
            response.put("success", false);
            response.put("message", "Mã giảm giá không tồn tại!");
        }

        return response;
    }
    // =======================
    // 5️⃣ TRANG THÀNH CÔNG COD
    // =======================
    @GetMapping("/payment-success-cod")
    public String paymentSuccessCod() {
        return "payment-success-cod";
    }
}


