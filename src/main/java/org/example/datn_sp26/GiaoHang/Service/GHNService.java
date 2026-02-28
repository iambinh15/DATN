package org.example.datn_sp26.GiaoHang.Service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class GHNService {

    // ====== GHN CONFIG ======
    private static final String BASE_URL =
            "https://online-gateway.ghn.vn/shiip/public-api";
    private final String GHN_URL = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
    private final String TOKEN = "0159688c-fb78-11f0-92cb-eef4825ef61b"; //
    private final String SHOP_ID = "6239525"; //


    private final RestTemplate restTemplate = new RestTemplate();

    // =======================
    // HEADER GHN
    // =======================
    private HttpHeaders ghnHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", TOKEN);
        headers.set("ShopId", SHOP_ID);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // =======================
    // 1️⃣ TÍNH PHÍ SHIP
    // =======================
    public String tinhPhiShip(Integer toDistrictId, String toWardCode, Integer weight, Long value) {

        Map<String, Object> body = new HashMap<>();

        // ĐỊA CHỈ SHOP (NAM TỪ LIÊM)
        body.put("from_district_id", 1530);

        body.put("to_district_id", toDistrictId);
        body.put("to_ward_code", toWardCode);

        body.put("weight", weight);
        body.put("length", 15);
        body.put("width", 15);
        body.put("height", 10);
        body.put("insurance_value", value.intValue());

        body.put("service_type_id", 2); // Chuẩn

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, ghnHeaders());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    BASE_URL + "/v2/shipping-order/fee",
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            return "{\"code\":500,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    // =======================
    // 2️⃣ PARSE PHÍ SHIP
    // =======================
    public long parsePhiShip(String json) {
        try {
            // GHN trả: data.total
            int idx = json.indexOf("\"total\":");
            if (idx == -1) return 0;

            String sub = json.substring(idx + 8);
            return Long.parseLong(sub.split(",")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    // =======================
    // 3️⃣ LẤY TỈNH / THÀNH
    // =======================
    public String getProvinces() {
        HttpEntity<Void> entity = new HttpEntity<>(ghnHeaders());
        return restTemplate.exchange(
                BASE_URL + "/master-data/province",
                HttpMethod.GET,
                entity,
                String.class
        ).getBody();
    }

    // =======================
    // 4️⃣ LẤY HUYỆN
    // =======================
    public String getDistricts(Integer provinceId) {
        Map<String, Integer> body = new HashMap<>();
        body.put("province_id", provinceId);

        HttpEntity<Map<String, Integer>> entity =
                new HttpEntity<>(body, ghnHeaders());

        return restTemplate.exchange(
                BASE_URL + "/master-data/district",
                HttpMethod.POST,
                entity,
                String.class
        ).getBody();
    }

    // =======================
    // 5️⃣ LẤY XÃ / PHƯỜNG
    // =======================
    public String getWards(Integer districtId) {
        Map<String, Integer> body = new HashMap<>();
        body.put("district_id", districtId);

        HttpEntity<Map<String, Integer>> entity =
                new HttpEntity<>(body, ghnHeaders());

        return restTemplate.exchange(
                BASE_URL + "/master-data/ward",
                HttpMethod.POST,
                entity,
                String.class
        ).getBody();
    }
}
//abc