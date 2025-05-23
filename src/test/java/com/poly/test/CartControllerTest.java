package com.poly.test;

import com.poly.entity.Account;
import com.poly.entity.Cart;
import com.poly.entity.CartId;
import com.poly.entity.Product;
import com.poly.service.CartService;
import com.poly.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CartControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CartService cartService;

	@MockBean
	private ProductService productService;

	private Account mockAccount;
	private Product inStockProduct;
	private Product outOfStockProduct;

	@BeforeEach
	void setUp() {
		// Giả lập tài khoản user đã đăng nhập
		mockAccount = new Account();
		mockAccount.setId(1);
		mockAccount.setUsername("user1");
		mockAccount.setPassword("12345");
		mockAccount.setActive(true);

		// Sản phẩm còn hàng (id=1)
		inStockProduct = new Product();
		inStockProduct.setId(1);
		inStockProduct.setName("iPhone 14");
		inStockProduct.setQuantity(10);
		inStockProduct.setPrice(BigDecimal.valueOf(999));
		inStockProduct.setImage("img1.jpg");
		// Lưu ý: nếu Product có thuộc tính category (với not-null) bạn cần thiết lập
		// thêm category
		// Ví dụ: inStockProduct.setCategory(someCategory);

		// Sản phẩm hết hàng (id=2)
		outOfStockProduct = new Product();
		outOfStockProduct.setId(2);
		outOfStockProduct.setName("Samsung Galaxy S999");
		outOfStockProduct.setQuantity(0);
		outOfStockProduct.setPrice(BigDecimal.valueOf(1234));
		outOfStockProduct.setImage("img2.jpg");
		// Tương tự: outOfStockProduct.setCategory(someCategory);
	}

	/**
	 * TC_CART_001: Add Product to Cart Người dùng đã đăng nhập, sản phẩm còn hàng.
	 * Mong đợi: Sản phẩm được thêm vào giỏ, số lượng của product giảm, và chuyển
	 * hướng về /home/index.
	 */
	@Test
	public void testAddProductToCart_Success() throws Exception {
		when(productService.findById(1L)).thenReturn(Optional.of(inStockProduct));
		// Giả định cart hiện tại chưa tồn tại
		when(cartService.findByAccountIdAndProductId(1, 1)).thenReturn(Optional.empty());

		mockMvc.perform(get("/cart/add").param("productId", "1").sessionAttr("account", mockAccount))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/home/index"));

		// Kiểm tra số lượng product được giảm (10 -> 9)
		ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
		verify(productService).update(productCaptor.capture());
		assertEquals(9, productCaptor.getValue().getQuantity());

		// Kiểm tra cart được tạo
		ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
		verify(cartService).create(cartCaptor.capture());
		Cart createdCart = cartCaptor.getValue();
		assertEquals(1, createdCart.getProductid());
		assertEquals(1, createdCart.getAccountid());
		assertEquals(1, createdCart.getQuantity());
	}

	/**
	 * TC_CART_002: Add to Cart Not Logged In Người dùng chưa đăng nhập (không có
	 * session account). Mong đợi: Chuyển hướng đến trang đăng nhập với query
	 * parameter redirect, không thêm cart.
	 */
	@Test
	public void testAddProductToCart_NotLoggedIn() throws Exception {
		mockMvc.perform(get("/cart/add").param("productId", "5")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("/auth/login?redirect=/cart/add*"));

		verify(productService, never()).findById(any());
		verify(cartService, never()).create(any());
	}

	/**
	 * TC_CART_003: Add Non-existent Product productId = 999 không tồn tại. Mong
	 * đợi: Chuyển hướng đến /home/index (với thông báo lỗi "Product not found").
	 */
	@Test
	public void testAddNonExistentProduct() throws Exception {
		when(productService.findById(999L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/cart/add").param("productId", "999").sessionAttr("account", mockAccount))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/home/index"));

		verify(cartService, never()).create(any());
		verify(productService, never()).update(any());
	}

	/**
	 * TC_CART_004: Add Out-of-Stock Product Sản phẩm hết hàng. Mong đợi: Chuyển
	 * hướng đến /home/index (với thông báo lỗi "Product is out of stock"), không
	 * thêm vào cart.
	 */
	@Test
	public void testAddOutOfStockProduct() throws Exception {
		when(productService.findById(999L)).thenReturn(Optional.of(outOfStockProduct));

		mockMvc.perform(get("/cart/add").param("productId", "999").sessionAttr("account", mockAccount))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/home/index"));

		verify(cartService, never()).create(any());
		verify(productService, never()).update(any());
	}

	/**
	 * TC_CART_005: Update Cart (Increase) Cập nhật giỏ hàng: tăng số lượng sản phẩm
	 * (action=plus). Mong đợi: Số lượng cart tăng, số lượng sản phẩm giảm, chuyển
	 * hướng về /cart.
	 */
	@Test
	public void testUpdateCartIncrease() throws Exception {
		when(productService.findById(1L)).thenReturn(Optional.of(inStockProduct));
		// Giả định cart hiện tại có quantity = 2
		Cart existingCart = new Cart(1, 1, 2, null, null);
		when(cartService.findByAccountIdAndProductId(1, 1)).thenReturn(Optional.of(existingCart));

		mockMvc.perform(
				get("/cart/update").param("productId", "1").param("action", "plus").sessionAttr("account", mockAccount))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cart"));

		// Kiểm tra số lượng sản phẩm giảm (10 -> 9)
		ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
		verify(productService).update(productCaptor.capture());
		assertEquals(9, productCaptor.getValue().getQuantity());

		// Kiểm tra số lượng cart tăng (2 -> 3)
		ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
		verify(cartService).update(cartCaptor.capture());
		assertEquals(3, cartCaptor.getValue().getQuantity());
	}

	/**
	 * TC_CART_006: Update Cart (Decrease) Cập nhật giỏ hàng: giảm số lượng sản phẩm
	 * (action=minus). Mong đợi: Số lượng cart giảm, số lượng sản phẩm tăng, chuyển
	 * hướng về /cart.
	 */
	@Test
	public void testUpdateCartDecrease() throws Exception {
		when(productService.findById(1L)).thenReturn(Optional.of(inStockProduct));
		// Giả định cart hiện tại có quantity = 2
		Cart existingCart = new Cart(1, 1, 2, null, null);
		when(cartService.findByAccountIdAndProductId(1, 1)).thenReturn(Optional.of(existingCart));

		mockMvc.perform(get("/cart/update").param("productId", "1").param("action", "minus").sessionAttr("account",
				mockAccount)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cart"));

		// Kiểm tra số lượng sản phẩm tăng (10 -> 11)
		ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
		verify(productService).update(productCaptor.capture());
		assertEquals(11, productCaptor.getValue().getQuantity());

		// Kiểm tra số lượng cart giảm (2 -> 1)
		ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
		verify(cartService).update(cartCaptor.capture());
		assertEquals(1, cartCaptor.getValue().getQuantity());
	}

	/**
	 * TC_CART_007: Delete Cart Item Xóa sản phẩm khỏi giỏ hàng. Mong đợi: Sản phẩm
	 * được xóa khỏi cart, số lượng sản phẩm được cộng lại, chuyển hướng về /cart.
	 */
	@Test
	public void testDeleteCartItem() throws Exception {
		Cart existingCart = new Cart(1, 1, 3, null, null);
		when(cartService.findByAccountIdAndProductId(1, 1)).thenReturn(Optional.of(existingCart));
		when(productService.findById(1L)).thenReturn(Optional.of(inStockProduct));

		mockMvc.perform(get("/cart/delete").param("productId", "1").sessionAttr("account", mockAccount))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cart"));

		// Kiểm tra sản phẩm: quantity được cộng thêm (10 + 3 = 13)
		ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
		verify(productService).update(productCaptor.capture());
		assertEquals(13, productCaptor.getValue().getQuantity());

		// Kiểm tra rằng cart đã bị xóa
		ArgumentCaptor<CartId> cartIdCaptor = ArgumentCaptor.forClass(CartId.class);
		verify(cartService).delete(cartIdCaptor.capture());
		CartId deletedId = cartIdCaptor.getValue();
		assertEquals(1, deletedId.getProductid());
		assertEquals(1, deletedId.getAccountid());
	}
}
