package org.example.datn_sp26.ThanhToan.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.datn_sp26.BanHang.Service.HoaDonService;
import org.example.datn_sp26.KhuyenMai.Entity.MaGiamGia;
import org.example.datn_sp26.KhuyenMai.Repository.MaGiamGiaRepository;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.BanHang.Service.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/api/vnpay")
public class PaymentController {

    @Autowired private HoaDonService hoaDonService;
    @Autowired private GioHangService gioHangService;
    @Autowired private MaGiamGiaRepository maGiamGiaRepository;

    private final String vnp_TmnCode = "9QMQRYDY";
    private final String vnp_HashSecret = "4ZUKUSPD2A1TYBJAYVV7X79TTEM52Z9H";
    private final String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    // --- 1. API TẠO LINK THANH TOÁN (Gửi tiền sang VNPay) ---
    @GetMapping("/pay")
    public String vnpayPayment(HttpServletRequest request,
                               HttpSession session,
                               @RequestParam(value = "amount", required = false) Double amountURL) throws Exception {

        KhachHang kh = (KhachHang) session.getAttribute("khachHang");
        if (kh == null) return "redirect:/login";

        // Lấy dữ liệu từ Session (đã lưu ở bước chọn địa chỉ/voucher trước đó)
        String diaChi = (String) session.getAttribute("DIA_CHI_TAM");
        Object phiShipObj = session.getAttribute("PHI_SHIP");
        String maVoucher = (String) session.getAttribute("MA_GIAM_GIA_DA_CHON");

        BigDecimal phiShip = (phiShipObj != null) ? new BigDecimal(phiShipObj.toString()) : BigDecimal.ZERO;

        // BƯỚC A: Tính tiền hàng thực tế từ DB
        var listGioHang = gioHangService.layGioHangCuaKhach(kh.getId());
        BigDecimal tienHang = BigDecimal.ZERO;
        for (var item : listGioHang) {
            BigDecimal gia = item.getIdSanPhamChiTiet().getDonGia();
            tienHang = tienHang.add(gia.multiply(BigDecimal.valueOf(item.getSoLuong())));
        }

        // BƯỚC B: Tính số tiền giảm giá từ Voucher
        BigDecimal soTienGiam = BigDecimal.ZERO;
        if (maVoucher != null && !maVoucher.isEmpty()) {
            MaGiamGia voucher = maGiamGiaRepository.findByMa(maVoucher).orElse(null);
            if (voucher != null) {
                if (voucher.getLoaiGiam() == 0) { // Giảm theo số tiền cố định
                    soTienGiam = BigDecimal.valueOf(voucher.getGiaTri());
                } else { // Giảm theo %
                    soTienGiam = tienHang.multiply(BigDecimal.valueOf(voucher.getGiaTri()))
                            .divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);
                }
            }
        }
        if (soTienGiam.compareTo(tienHang) > 0) soTienGiam = tienHang;

        // BƯỚC C: Tổng thanh toán cuối cùng gửi sang VNPay
        BigDecimal tongThanhToan = tienHang.subtract(soTienGiam).add(phiShip);

        // --- CẤU HÌNH VNPAY PARAMS ---
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(tongThanhToan.multiply(new BigDecimal(100)).longValue()));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", kh.getId() + "_" + System.currentTimeMillis());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang Di Coffee");
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", "http://localhost:8080/api/vnpay/callback");
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        // Sắp xếp và tạo Hash (Giữ nguyên logic chuẩn VNPay)
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII)).append('&');
                hashData.append('&');
            }
        }
        hashData.deleteCharAt(hashData.length() - 1);
        query.deleteCharAt(query.length() - 1);
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());

        return "redirect:" + vnp_Url + "?" + query + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    // --- 2. API CALLBACK (Nhận kết quả từ VNPay và Lưu DB) ---
    @GetMapping("/callback")
    public String vnpayCallback(HttpServletRequest request, HttpSession session) {
        String responseCode = request.getParameter("vnp_ResponseCode");

        if ("00".equals(responseCode)) {
            try {
                KhachHang kh = (KhachHang) session.getAttribute("khachHang");
                String diaChi = (String) session.getAttribute("DIA_CHI_TAM");
                Object phiShipObj = session.getAttribute("PHI_SHIP");
                String maVoucher = (String) session.getAttribute("MA_GIAM_GIA_DA_CHON");

                BigDecimal phiShip = (phiShipObj != null) ? new BigDecimal(phiShipObj.toString()) : BigDecimal.ZERO;

                // --- ĐỒNG BỘ GIÁ TIỀN: Lấy trực tiếp từ VNPay gửi về ---
                String vnpAmountStr = request.getParameter("vnp_Amount");
                BigDecimal tongThanhToanThucTe = new BigDecimal(vnpAmountStr).divide(new BigDecimal(100));

                // LƯU HÓA ĐƠN VỚI SỐ TIỀN THỰC TẾ ĐÃ TRẢ
                hoaDonService.taoHoaDonVNPay(kh, tongThanhToanThucTe, diaChi, phiShip, maVoucher);

                // Dọn dẹp session sau khi xong việc
                session.removeAttribute("MA_GIAM_GIA_DA_CHON");
                session.removeAttribute("DIA_CHI_TAM");
                session.removeAttribute("PHI_SHIP");

                return "payment-success"; // Đã bỏ 'khach-hang/' để khớp với ảnh cấu hình file của bạn
            } catch (Exception e) {
                e.printStackTrace();
                return "payment-fail";
            }
        }
        return "payment-fail";
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : result) sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        } catch (Exception ex) { return ""; }
    }
}