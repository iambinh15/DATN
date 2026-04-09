package org.example.datn_sp26.HoTro.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


import java.util.*;

@Controller
@CrossOrigin(origins = "*")
public class ChatController {

    // 🔑 API KEY GEMINI
    private static final String GEMINI_API_KEY = "AIzaSyAxSyHvC_8nUL68RvGI5Ls-pT3hdX0bx08";

    // ✅ MODEL ĐÚNG – PHẢI CÓ -latest
    private static final String GEMINI_API =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key="
                    + GEMINI_API_KEY;

    // 🧠 SYSTEM PROMPT
    private static final String SYSTEM_INSTRUCTION =
            "Bạn là Trợ lý AI của Chu Đình Bình. Trả lời NGẮN GỌN, súc tích, chỉ nói điều quan trọng. "
                    + "Không nói dài, không lặp lại. Luôn dùng tiếng Việt thân thiện, tự nhiên. "
                    + "Nếu người dùng muốn nói tiếng Anh thì trả lời bằng tiếng Anh.";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/uu")
    public String showcv() {
        return "fragments/cv";
    }

    @GetMapping("/chat")
    public String showChatPage() {
        return "fragments/ChatBot";
    }

    @PostMapping("/api/chat")
    @ResponseBody
    public String chat(@RequestBody String userMessage) {

        try {
            String personalInfo = """
                    Họ và tên: Chu Đình Bình
                    Nghề nghiệp: Lập trình viên, chạy Ads, thiết kế Website
                    Kinh nghiệm: 1 năm Java Spring Boot, hệ thống quán cà phê, khu vui chơi
                    Liên hệ: 0389415404
                    Dịch vụ: Thiết kế web, quảng cáo Facebook, chatbot
                    """;

            String prompt = SYSTEM_INSTRUCTION + "\n\n"
                    + "--- HỒ SƠ ---\n" + personalInfo
                    + "\n--- CÂU HỎI ---\n" + userMessage;

            Map<String, Object> userPart = Map.of("text", prompt);

            Map<String, Object> userContent = Map.of(
                    "role", "user",
                    "parts", List.of(userPart)
            );

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(userContent)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(GEMINI_API, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());

            return root
                    .path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText("⚠️ Gemini không trả nội dung.");

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Backend error: " + e.getMessage();
        }
    }
}
