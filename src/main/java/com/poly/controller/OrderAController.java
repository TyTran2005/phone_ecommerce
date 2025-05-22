package com.poly.controller;

import com.poly.entity.Order;
import com.poly.service.OrderService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OrderAController {

	@Autowired
	private OrderService orderService;

	// Hiển thị trang quản lý đơn hàng với phân trang (mỗi trang 4 đơn hàng)
	@RequestMapping("/admin/order")
	public String order(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
		Order item = new Order();
		model.addAttribute("item", item);
		Pageable pageable = PageRequest.of(page, 4);
		Page<Order> orderPage = orderService.findAll(pageable);
		model.addAttribute("orderPage", orderPage);
		model.addAttribute("currentPage", page);
		return "order";
	}

	// Xử lý lưu đơn hàng (create/update)
	@PostMapping("/admin/order/save")
	public String save(@ModelAttribute("item") Order order, @RequestParam("action") String action) {
		try {
			if ("create".equals(action)) {
				orderService.create(order);
			} else if ("update".equals(action)) {
				orderService.update(order);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/admin/order";
	}

	// Xóa đơn hàng
	@GetMapping("/admin/order/delete/{id}")
	public String delete(@PathVariable("id") Integer id) {
		orderService.delete(id.longValue());
		return "redirect:/admin/order";
	}

	// Thêm vào OrderAController.java

	@GetMapping("/admin/order/detail/{id}")
	public String orderDetail(@PathVariable("id") Integer id, Model model) {
		Optional<Order> opt = orderService.findById(id.longValue());
		if (opt.isPresent()) {
			Order order = opt.get();
			// Lưu ý: Đảm bảo rằng order.getOrderDetails() đã được khởi tạo (có thể dùng
			// OSIV hoặc truy vấn join fetch)
			model.addAttribute("order", order);
			return "orderdetailA";
		} else {
			return "redirect:/admin/order";
		}
	}

}
