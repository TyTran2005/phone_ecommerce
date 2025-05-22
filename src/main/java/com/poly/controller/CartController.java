package com.poly.controller;

import com.poly.entity.Account;
import com.poly.entity.Cart;
import com.poly.entity.CartId;
import com.poly.entity.Product;
import com.poly.service.CartService;
import com.poly.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
public class CartController {

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductService productService;

	@RequestMapping("/cart/add")
	public String addToCart(@RequestParam("productId") Integer productId, HttpServletRequest request, Model model) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			String currentUrl = request.getRequestURI();
			String query = request.getQueryString();
			if (query != null && !query.isEmpty()) {
				currentUrl += "?" + query;
			}
			return "redirect:/auth/login?redirect=" + currentUrl;
		}

		Optional<Product> optProduct = productService.findById(Long.valueOf(productId));
		if (!optProduct.isPresent()) {
			model.addAttribute("error", "Product not found");
			return "redirect:/home/index";
		}
		Product product = optProduct.get();

		if (product.getQuantity() <= 0) {
			model.addAttribute("error", "Product is out of stock");
			return "redirect:/home/index";
		}

		product.setQuantity(product.getQuantity() - 1);
		productService.update(product);

		Integer accountId = account.getId();
		Optional<Cart> optCart = cartService.findByAccountIdAndProductId(accountId, productId);
		Cart cart;
		if (optCart.isPresent()) {
			cart = optCart.get();
			cart.setQuantity(cart.getQuantity() + 1);
		} else {
			cart = new Cart(productId, accountId, 1, null, null);
		}
		cartService.create(cart);
		return "redirect:/home/index";
	}

	@RequestMapping("/cart/update")
	public String updateCart(@RequestParam("productId") Integer productId, @RequestParam("action") String action,
			HttpServletRequest request) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			return "redirect:/auth/login";
		}

		Integer accountId = account.getId();
		Optional<Cart> optCart = cartService.findByAccountIdAndProductId(accountId, productId);

		if (optCart.isPresent()) {
			Cart cart = optCart.get();
			Optional<Product> optProduct = productService.findById(Long.valueOf(productId));

			if ("plus".equalsIgnoreCase(action) && optProduct.isPresent() && optProduct.get().getQuantity() > 0) {
				Product product = optProduct.get();
				product.setQuantity(product.getQuantity() - 1);
				productService.update(product);

				cart.setQuantity(cart.getQuantity() + 1);
				cartService.update(cart);
			} else if ("minus".equalsIgnoreCase(action) && cart.getQuantity() > 1) {
				if (optProduct.isPresent()) {
					Product product = optProduct.get();
					product.setQuantity(product.getQuantity() + 1);
					productService.update(product);
				}
				cart.setQuantity(cart.getQuantity() - 1);
				cartService.update(cart);
			}
		}
		return "redirect:/cart";
	}

	@RequestMapping("/cart/delete")
	public String deleteCartItem(@RequestParam("productId") Integer productId, HttpServletRequest request) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			return "redirect:/auth/login";
		}

		Integer accountId = account.getId();
		Optional<Cart> optCart = cartService.findByAccountIdAndProductId(accountId, productId);

		if (optCart.isPresent()) {
			Cart cart = optCart.get();
			Optional<Product> optProduct = productService.findById(Long.valueOf(productId));

			if (optProduct.isPresent()) {
				Product product = optProduct.get();
				product.setQuantity(product.getQuantity() + cart.getQuantity());
				productService.update(product);
			}
			cartService.delete(new CartId(productId, accountId));
		}
		return "redirect:/cart";
	}

	@RequestMapping("/cart")
	public String cart(HttpServletRequest request, Model model) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			String currentUrl = request.getRequestURI();
			String query = request.getQueryString();
			if (query != null && !query.isEmpty()) {
				currentUrl += "?" + query;
			}
			return "redirect:/auth/login?redirect=" + currentUrl;
		}

		Integer accountId = account.getId();
		model.addAttribute("cartItems", cartService.findByAccount(accountId));

		BigDecimal total = cartService.findByAccount(accountId).stream()
				.map(cart -> cart.getProduct().getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		model.addAttribute("totalAmount", total.doubleValue());
		return "cart";
	}
}
