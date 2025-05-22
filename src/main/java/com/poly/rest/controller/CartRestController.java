package com.poly.rest.controller;

import com.poly.entity.Cart;
import com.poly.entity.CartId;
import com.poly.entity.Product;
import com.poly.service.CartService;
import com.poly.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductService productService;

	// Lấy danh sách các mục trong giỏ hàng theo accountId
	@GetMapping("/{accountId}")
	public ResponseEntity<List<Cart>> getCartItems(@PathVariable Integer accountId) {
		return ResponseEntity.ok(cartService.findByAccount(accountId));
	}

	// Thêm sản phẩm vào giỏ hàng
	@PostMapping("/add")
	public ResponseEntity<?> addToCart(@RequestBody Cart cart) {
		// Kiểm tra số lượng sản phẩm còn đủ hay không
		Optional<Product> optProduct = productService.findById(Long.valueOf(cart.getProductid()));
		if (!optProduct.isPresent()) {
			return ResponseEntity.badRequest().body("Product not found");
		}
		Product product = optProduct.get();
		if (product.getQuantity() < cart.getQuantity()) {
			return ResponseEntity.badRequest().body("Not enough product stock");
		}
		// Giảm số lượng sản phẩm trong kho
		product.setQuantity(product.getQuantity() - cart.getQuantity());
		productService.update(product);

		// Nếu sản phẩm đã tồn tại trong giỏ, cập nhật số lượng
		Optional<Cart> existingCart = cartService.findByAccountIdAndProductId(cart.getAccountid(), cart.getProductid());
		Cart resultCart;
		if (existingCart.isPresent()) {
			resultCart = existingCart.get();
			resultCart.setQuantity(resultCart.getQuantity() + cart.getQuantity());
			resultCart = cartService.update(resultCart);
		} else {
			resultCart = cartService.create(cart);
		}
		return ResponseEntity.ok(resultCart);
	}

	// Cập nhật số lượng của một mục giỏ hàng
	@PutMapping("/update")
	public ResponseEntity<?> updateCart(@RequestBody Cart cart) {
		// Lấy mục giỏ hàng hiện tại
		Optional<Cart> optExisting = cartService.findByAccountIdAndProductId(cart.getAccountid(), cart.getProductid());
		if (!optExisting.isPresent()) {
			return ResponseEntity.badRequest().body("Cart item not found");
		}
		Cart existingCart = optExisting.get();
		int oldQuantity = existingCart.getQuantity();
		int newQuantity = cart.getQuantity();
		int diff = newQuantity - oldQuantity;

		// Lấy sản phẩm tương ứng
		Optional<Product> optProduct = productService.findById(Long.valueOf(cart.getProductid()));
		if (!optProduct.isPresent()) {
			return ResponseEntity.badRequest().body("Product not found");
		}
		Product product = optProduct.get();
		if (diff > 0) { // Nếu tăng số lượng, cần kiểm tra kho
			if (product.getQuantity() < diff) {
				return ResponseEntity.badRequest().body("Not enough product stock for increase");
			}
			product.setQuantity(product.getQuantity() - diff);
		} else if (diff < 0) { // Nếu giảm số lượng, trả lại cho kho
			product.setQuantity(product.getQuantity() + (-diff));
		}
		productService.update(product);

		existingCart.setQuantity(newQuantity);
		Cart updatedCart = cartService.update(existingCart);
		return ResponseEntity.ok(updatedCart);
	}

	// Xóa sản phẩm khỏi giỏ hàng
	@DeleteMapping("/delete/{accountId}/{productId}")
	public ResponseEntity<?> removeFromCart(@PathVariable Integer accountId, @PathVariable Integer productId) {
		Optional<Cart> optCart = cartService.findByAccountIdAndProductId(accountId, productId);
		if (!optCart.isPresent()) {
			return ResponseEntity.badRequest().body("Cart item not found");
		}
		Cart cart = optCart.get();
		// Trả lại số lượng sản phẩm cho kho
		Optional<Product> optProduct = productService.findById(Long.valueOf(productId));
		if (optProduct.isPresent()) {
			Product product = optProduct.get();
			product.setQuantity(product.getQuantity() + cart.getQuantity());
			productService.update(product);
		}
		cartService.delete(new CartId(productId, accountId));
		return ResponseEntity.noContent().build();
	}
}
