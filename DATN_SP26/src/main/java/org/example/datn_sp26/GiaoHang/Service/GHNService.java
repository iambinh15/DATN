package org.example.datn_sp26.GiaoHang.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class GHNService {
    // URL môi trường thật
    private final String GHN_URL = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
    private final String TOKEN = "0159688c-fb78-11f0-92cb-eef4825ef61b"; //
    private final String SHOP_ID = "6239525"; //

    public String tinhPhiShip(Integer toDistrictId, String toWardCode, Integer weight, Long value) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", TOKEN);
        headers.set("ShopId", SHOP_ID);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        // ĐỊA CHỈ SHOP CỐ ĐỊNH: Nam Từ Liêm, Hà Nội
        body.put("from_district_id", 1530);

        // ĐỊA CHỈ KHÁCH HÀNG: Truyền từ Controller
        body.put("to_district_id", toDistrictId);
        body.put("to_ward_code", toWardCode);

        body.put("weight", weight);
        body.put("length", 15);
        body.put("width", 15);
        body.put("height", 10);
        body.put("insurance_value", value.intValue());
        body.put("service_type_id", 2); // Chuyển phát chuẩn

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(GHN_URL, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            // Trả về JSON lỗi để JavaScript xử lý
            return "{\"code\": 500, \"message\": \"" + e.getMessage() + "\"}";
        }
    }
}