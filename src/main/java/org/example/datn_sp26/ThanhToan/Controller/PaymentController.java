package org.example.datn_sp26.ThanhToan.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.datn_sp26.BanHang.Service.HoaDonService;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class PaymentController {

    private final HoaDonService hoaDonService;

    public PaymentController(HoaDonService hoaDonService) {
        this.hoaDonService = hoaDonService;
    }

    private final String vnp_TmnCode = "9QMQRYDY";
    private final String vnp_HashSecret = "4ZUKUSPD2A1TYBJAYVV7X79TTEM52Z9H";
    private final String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    @GetMapping("/api/vnpay/pay")
    public String vnpayPayment(HttpServletRequest request,
            @RequestParam("amount") Long amount) throws Exception {

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", String.valueOf(System.currentTimeMillis()));
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang");
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", "http://localhost:8080/api/vnpay/callback");
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII))
                        .append('&');
                hashData.append('&');
            }
        }

        hashData.deleteCharAt(hashData.length() - 1);
        query.deleteCharAt(query.length() - 1);

        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        String queryUrl = query + "&vnp_SecureHash=" + vnp_SecureHash;

        return "redirect:" + vnp_Url + "?" + queryUrl;
    }

    @GetMapping("/api/vnpay/callback")
    public String vnpayCallback(HttpServletRequest request,
            HttpSession session) {

        String responseCode = request.getParameter("vnp_ResponseCode");
        String amountStr = request.getParameter("vnp_Amount");

        // Nếu thanh toán thất bại, trả về trang lỗi ngay
        if (!"00".equals(responseCode) || amountStr == null) {
            return "payment-fail";
        }

        // 1. LẤY DỮ LIỆU TỪ SESSION
        String diaChi = (String) session.getAttribute("DIA_CHI_TAM");
        Object phiShipObj = session.getAttribute("PHI_SHIP");
        KhachHang khachHang = (KhachHang) session.getAttribute("khachHang");

        if (khachHang == null) {
            return "redirect:/login";
        }

        if (diaChi == null || phiShipObj == null) {
            throw new RuntimeException("❌ MẤT SESSION ĐỊA CHỈ / PHÍ SHIP");
        }

        BigDecimal phiShip = new BigDecimal(phiShipObj.toString());
        // VNPay amount trả về là (số tiền * 100)
        BigDecimal tongThanhToan = BigDecimal.valueOf(Long.parseLong(amountStr) / 100);

        // Lấy danh sách ID sản phẩm đã chọn từ session
        @SuppressWarnings("unchecked")
        java.util.List<Integer> selectedIds = (java.util.List<Integer>) session.getAttribute("SELECTED_IDS");

        try {
            // ✅ TẠO HÓA ĐƠN + CHI TIẾT + TRỪ KHO + XÓA SẢN PHẨM ĐÃ CHỌN (ATOMIC)
            org.example.datn_sp26.BanHang.Entity.HoaDon hoaDon = hoaDonService.taoHoaDonVNPay(
                    khachHang,
                    tongThanhToan,
                    diaChi,
                    phiShip,
                    selectedIds);

            // XÓA SESSION SAU KHI HOÀN TẤT
            session.removeAttribute("DIA_CHI_TAM");
            session.removeAttribute("PHI_SHIP");
            session.removeAttribute("SELECTED_IDS");

            // Trả về trực tiếp tên file HTML như trong ảnh cấu trúc của bạn
            return "payment-success";

        } catch (Exception e) {
            e.printStackTrace();
            return "payment-fail";
        }
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}