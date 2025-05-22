package com.poly.rest.controller;

import com.poly.entity.Order;
import com.poly.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

	@Autowired
	private OrderService orderService;

	// Lấy danh sách đơn hàng với phân trang
	@GetMapping
	public ResponseEntity<?> getAllOrders(Pageable pageable) {
		Page<Order> page = orderService.findAll(pageable);
		// Tạo Map chứa các thuộc tính cần thiết
		Map<String, Object> response = new HashMap<>();
		response.put("content", page.getContent());
		response.put("totalPages", page.getTotalPages());
		return ResponseEntity.ok(response);
	}

	// Tạo mới đơn hàng (giả sử dữ liệu đơn hàng được gửi dưới dạng JSON)
	@PostMapping
	public ResponseEntity<?> createOrder(@RequestBody Order order) {
		try {
			Order created = orderService.create(order);
			return ResponseEntity.ok(created);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Lỗi tạo đơn hàng");
		}
	}

	// Cập nhật đơn hàng (dữ liệu gửi qua JSON)
	@PutMapping("/{id}")
	public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Order order) {
		Optional<Order> optOrder = orderService.findById(id);
		if (!optOrder.isPresent()) {
			return ResponseEntity.badRequest().body("Đơn hàng không tồn tại");
		}
		try {
			// Giữ nguyên orderDate nếu không sửa từ frontend
			order.setId(optOrder.get().getId());
			order.setOrderDate(optOrder.get().getOrderDate());
			Order updated = orderService.update(order);
			return ResponseEntity.ok(updated);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Lỗi cập nhật đơn hàng");
		}
	}

	// Xóa đơn hàng
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
		if (!orderService.findById(id).isPresent()) {
			return ResponseEntity.badRequest().body("Đơn hàng không tồn tại");
		}
		orderService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
