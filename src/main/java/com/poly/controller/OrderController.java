package com.poly.controller;

import com.poly.entity.Account;
import com.poly.entity.Cart;
import com.poly.entity.Order;
import com.poly.entity.OrderDetail;
import com.poly.service.CartService;
import com.poly.service.OrderDetailService;
import com.poly.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrderController {

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderDetailService orderDetailService;

	@RequestMapping(value = "/order/checkout", method = { RequestMethod.GET, RequestMethod.POST })
	public String checkout(
			@RequestParam(value = "selectedCartItems", required = false) List<Integer> selectedProductIds,
			@RequestParam("accountId") Integer accountId, // also add accountId here if needed
			HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			return "redirect:/auth/login";
		}
		if (selectedProductIds == null || selectedProductIds.isEmpty()) {
			redirectAttributes.addFlashAttribute("checkoutError", "Vui lòng chọn sản phẩm để đặt hàng");
			return "redirect:/cart";
		}

		List<Cart> allCartItems = cartService.findByAccount(account.getId());
		List<Cart> selectedCartItems = allCartItems.stream()
				.filter(cart -> selectedProductIds.contains(cart.getProductid())).collect(Collectors.toList());

		if (selectedCartItems.isEmpty()) {
			redirectAttributes.addFlashAttribute("checkoutError", "Vui lòng chọn sản phẩm để đặt hàng");
			return "redirect:/cart";
		}

		BigDecimal total = selectedCartItems.stream()
				.map(cart -> cart.getProduct().getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		model.addAttribute("cartItems", selectedCartItems);
		model.addAttribute("totalAmount", total.doubleValue());
		model.addAttribute("account", account);
		request.getSession().setAttribute("selectedCartItems", selectedCartItems);

		return "orderlayout"; // View that shows the order summary before final submission
	}

	@PostMapping("/order/submit")
	public String submitOrder(@RequestParam("address") String address, @RequestParam("phonenumber") String phonenumber,
			@RequestParam("totalAmount") double totalAmount, HttpServletRequest request, Model model) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			return "redirect:/auth/login";
		}

		List<Cart> selectedCartItems = (List<Cart>) request.getSession().getAttribute("selectedCartItems");
		if (selectedCartItems == null || selectedCartItems.isEmpty()) {
			return "redirect:/cart";
		}

		Order order = new Order();
		order.setAccount(account);
		order.setAddress(address.trim());
		order.setPhonenumber(phonenumber.trim());
		order.setTotalAmount(BigDecimal.valueOf(totalAmount));
		order.setOrderDate(LocalDateTime.now());
		order.setStatus("Chờ xác nhận");

		Order savedOrder = orderService.create(order);

		for (Cart cart : selectedCartItems) {
			OrderDetail detail = new OrderDetail();
			detail.setOrderId(savedOrder.getId());
			detail.setProductId(cart.getProductid());
			detail.setQuantity(cart.getQuantity());
			detail.setPrice(cart.getProduct().getPrice());
			orderDetailService.create(detail);
		}

		request.getSession().removeAttribute("selectedCartItems");

		return "redirect:/order/detail";
	}

	@RequestMapping("/order/detail")
	public String orders(HttpServletRequest request, Model model) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			return "redirect:/auth/login";
		}

		List<Order> orders = orderService.findByAccountId(account.getId());
		model.addAttribute("orders", orders);

		return "orderdetail";
	}

	@RequestMapping("/order/delete")
	public String deleteOrder(@RequestParam("orderId") Integer orderId, HttpServletRequest request) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			return "redirect:/auth/login";
		}

		orderService.delete(orderId.longValue());

		return "redirect:/order/detail";
	}
}
