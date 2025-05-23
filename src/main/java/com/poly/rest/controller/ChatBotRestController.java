package com.poly.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.poly.entity.Product;
import com.poly.service.ProductService;
import com.poly.service.CartService;
import com.poly.service.OrderDetailService;
import com.poly.service.OrderService;
import com.poly.entity.Cart;
import com.poly.entity.Order;
import com.poly.entity.OrderDetail;

import java.util.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatBotRestController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        Integer accountId = null;
        if (body.containsKey("accountId")) {
            try {
                accountId = Integer.valueOf(body.get("accountId"));
            } catch (Exception ignored) {
            }
        }

        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing question");
        }

        List<Product> products = productService.findAll();

        List<Cart> cartItems = accountId != null && accountId != 0 ? cartService.findByAccount(accountId) : List.of();

        List<Order> orders = accountId != null && accountId != 0 ? orderService.findByAccountId(accountId) : List.of();

        StringBuilder prompt = new StringBuilder();
        prompt.append(
                "Bạn là trợ lý bán hàng chuyên nghiệp cho một cửa hàng điện thoại. Dưới đây là dữ liệu sản phẩm và giỏ hàng người dùng:\n\n");
        prompt.append(
                "Thông tin sản phẩm bao gồm tên, giá, số lượng và hãng sản xuất (ví dụ: iPhone, Samsung, v.v.).\n\n");

        prompt.append("Danh sách sản phẩm (dạng JSON):\n[\n");
        for (Product p : products) {
            prompt.append(String.format("  {\"name\": \"%s\", \"price\": %s, \"quantity\": %d, \"brand\": \"%s\"},\n",
                    p.getName(), p.getPrice(), p.getQuantity(),
                    p.getCategory() != null ? p.getCategory().getName() : "Không rõ"));
        }
        prompt.append("]\n\n");

        if (!cartItems.isEmpty()) {
            prompt.append("Giỏ hàng của người dùng:\n[\n");
            for (Cart c : cartItems) {
                Product p = products.stream().filter(pr -> pr.getId().equals(c.getProductid())).findFirst()
                        .orElse(null);
                if (p != null) {
                    prompt.append(
                            String.format("  {\"name\": \"%s\", \"quantity\": %d},\n", p.getName(), c.getQuantity()));
                }
            }
            prompt.append("]\n\n");
        } else {
            prompt.append("Giỏ hàng của người dùng hiện đang trống.\n\n");
        }

        if (!orders.isEmpty()) {
            prompt.append("Lịch sử đơn hàng của người dùng:\n[\n");
            for (Order o : orders) {
                prompt.append(
                        String.format(
                                "  {\"orderId\": %d, \"createDate\": \"%s\",\"status\": \"%s\", \"total\": %s,\n  \"details\": [\n",
                                o.getId(), o.getOrderDate(), o.getTotalAmount(), o.getStatus()));

                List<OrderDetail> details = orderDetailService.findByOrderId(o.getId());

                for (OrderDetail d : details) {
                    Product p = d.getProduct();
                    prompt.append(
                            String.format("    {\"name\": \"%s\", \"quantity\": %d},\n", p.getName(), d.getQuantity()));
                }

                prompt.append("  ]},\n");
            }
            prompt.append("]\n\n");
        } else {
            prompt.append("Người dùng chưa có đơn hàng nào.\n\n");
        }

        prompt.append(
                "Trả lời câu hỏi sau dựa trên thông tin ở trên (sản phẩm, giỏ hàng, đơn hàng và chi tiết đơn hàng). Nếu không chắc chắn, hãy đề nghị người dùng hỏi cụ thể hơn.\n");
        prompt.append("Câu hỏi: ").append(question);

        // Gửi đến Gemini
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
                + geminiApiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> content = Map.of("parts", List.of(Map.of("text", prompt.toString())));
        Map<String, Object> request = Map.of("contents", List.of(content));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return ResponseEntity.status(500).body("No answer from Gemini");
            }
            Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
            String answer = (String) parts.get(0).get("text");
            return ResponseEntity.ok(Map.of("answer", answer));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Chatbot error: " + e.getMessage());
        }
    }
}
