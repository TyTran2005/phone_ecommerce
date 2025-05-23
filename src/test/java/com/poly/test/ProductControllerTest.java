package com.poly.test;

import com.poly.entity.Account;
import com.poly.entity.Category;
import com.poly.entity.Product;
import com.poly.service.CategoryService;
import com.poly.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductService productService;

	@MockBean
	private CategoryService categoryService;

	// Tạo đối tượng admin để giả lập session (đảm bảo có quyền admin)
	private Account adminAccount;
	private Category sampleCategory;
	private Product sampleProduct1;
	private Product sampleProduct2;

	@BeforeEach
	public void setUp() {
		// Khởi tạo adminAccount có quyền admin
		adminAccount = new Account();
		adminAccount.setId(1);
		adminAccount.setUsername("admin");
		adminAccount.setPassword("admin123");
		adminAccount.setEmail("admin@example.com");
		adminAccount.setActive(true);
		adminAccount.setAdmin(true);

		// Thiết lập dữ liệu mẫu
		sampleCategory = new Category(1, "Smartphone", "logo.png", Collections.emptyList());
		sampleProduct1 = new Product(1, "iPhone 14", BigDecimal.valueOf(999.99), 10, "img1.jpg", sampleCategory,
				"$999.99");
		sampleProduct2 = new Product(2, "iPhone 13", BigDecimal.valueOf(799.99), 8, "img2.jpg", sampleCategory,
				"$799.99");
	}

	// TC_PROD_001: Xem trang quản lý sản phẩm
	@Test
	public void testViewProductManagementPage() throws Exception {
		List<Product> productList = Arrays.asList(sampleProduct1, sampleProduct2);
		Pageable pageable = PageRequest.of(0, 4);
		Page<Product> productPage = new PageImpl<>(productList, pageable, productList.size());
		when(productService.findAll(any(Pageable.class))).thenReturn(productPage);
		when(categoryService.findAll()).thenReturn(Arrays.asList(sampleCategory));

		mockMvc.perform(get("/admin/product").param("page", "0").sessionAttr("account", adminAccount))
				.andExpect(status().isOk()).andExpect(view().name("product"))
				.andExpect(model().attribute("currentPage", 0)).andExpect(model().attribute("productPage", productPage))
				.andExpect(model().attribute("categories", hasSize(1)));
	}

	// TC_PROD_002: Tạo sản phẩm mới (dữ liệu hợp lệ)
	@Test
	public void testCreateNewProduct_ValidData() throws Exception {
		byte[] fileContent = "fake image content".getBytes(StandardCharsets.UTF_8);
		MockMultipartFile imageFile = new MockMultipartFile("imageFile", "iphone14pro.jpg", "image/jpeg", fileContent);

		// Giả lập tạo sản phẩm mới
		Product newProduct = new Product(3, "iPhone 14 Pro", BigDecimal.valueOf(1299.99), 15,
				"timestamp_iphone14pro.jpg", sampleCategory, "$1299.99");
		when(productService.create(any(Product.class))).thenReturn(newProduct);

		mockMvc.perform(multipart("/admin/product/save").file(imageFile).param("name", "iPhone 14 Pro")
				.param("price", "1299.99").param("quantity", "15").param("action", "create").param("category.id", "1")
				.sessionAttr("account", adminAccount)).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/product"));

		ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
		verify(productService).create(productCaptor.capture());
		Product created = productCaptor.getValue();
		assertEquals("iPhone 14 Pro", created.getName());
		assertEquals(BigDecimal.valueOf(1299.99), created.getPrice());
		assertEquals(15, created.getQuantity());
	}

	// TC_PROD_004: Cập nhật sản phẩm hiện có
	@Test
	public void testUpdateExistingProduct() throws Exception {
		Product existingProduct = new Product(1, "iPhone 14", BigDecimal.valueOf(999.99), 10, "img1.jpg",
				sampleCategory, "$999.99");
		when(productService.findById(1L)).thenReturn(Optional.of(existingProduct));

		byte[] fileContent = "new image content".getBytes(StandardCharsets.UTF_8);
		MockMultipartFile newImageFile = new MockMultipartFile("imageFile", "iphone14promax.jpg", "image/jpeg",
				fileContent);

		mockMvc.perform(multipart("/admin/product/save").file(newImageFile).param("id", "1")
				.param("name", "iPhone 14 Pro Max").param("price", "1399.99").param("quantity", "20")
				.param("action", "update").param("category.id", "1").sessionAttr("account", adminAccount))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/product"));

		ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
		verify(productService).update(productCaptor.capture());
		Product updated = productCaptor.getValue();
		assertEquals("iPhone 14 Pro Max", updated.getName());
		assertEquals(BigDecimal.valueOf(1399.99), updated.getPrice());
		assertEquals(20, updated.getQuantity());
		// Nếu cần kiểm tra hình ảnh mới:
		// assertTrue(updated.getImage().endsWith("iphone14promax.jpg"));
	}

	// Các test còn lại (TC_PROD_005, TC_PROD_006, TC_PROD_007) cũng cần thêm
	// sessionAttr("account", adminAccount)
	// để giả lập admin đã đăng nhập.

	// Ví dụ, TC_PROD_005: Xóa sản phẩm (bằng POST)
	@Test
	public void testDeleteProductViaPost() throws Exception {
		// Giả lập đăng nhập admin
		MockMultipartFile emptyFile = new MockMultipartFile("imageFile", "", "application/octet-stream", new byte[0]);

		mockMvc.perform(multipart("/admin/product/save").file(emptyFile).param("id", "5").param("action", "delete")
				.sessionAttr("account", adminAccount)).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/product"));

		verify(productService).delete(5L);
	}

	// TC_PROD_006: Xóa sản phẩm (bằng GET)
	@Test
	public void testDeleteProductViaGet() throws Exception {
		mockMvc.perform(get("/admin/product/delete/10").sessionAttr("account", adminAccount))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/product"));

		verify(productService).delete(10L);
	}

	// TC_PROD_007: Kiểm tra phân trang trên trang quản lý sản phẩm
	@Test
	public void testPaginationOnProductPage() throws Exception {
		List<Product> productsPage0 = Arrays.asList(
				new Product(1, "SP 1", BigDecimal.valueOf(100), 5, "img1.jpg", sampleCategory, "$100"),
				new Product(2, "SP 2", BigDecimal.valueOf(200), 5, "img2.jpg", sampleCategory, "$200"),
				new Product(3, "SP 3", BigDecimal.valueOf(300), 5, "img3.jpg", sampleCategory, "$300"),
				new Product(4, "SP 4", BigDecimal.valueOf(400), 5, "img4.jpg", sampleCategory, "$400"));
		List<Product> productsPage1 = Arrays.asList(
				new Product(5, "SP 5", BigDecimal.valueOf(500), 5, "img5.jpg", sampleCategory, "$500"),
				new Product(6, "SP 6", BigDecimal.valueOf(600), 5, "img6.jpg", sampleCategory, "$600"));

		Pageable pageable0 = PageRequest.of(0, 4);
		Pageable pageable1 = PageRequest.of(1, 4);
		Page<Product> page0 = new PageImpl<>(productsPage0, pageable0, 6);
		Page<Product> page1 = new PageImpl<>(productsPage1, pageable1, 6);

		when(productService.findAll(any(Pageable.class))).thenReturn(page0).thenReturn(page1);
		when(categoryService.findAll()).thenReturn(Arrays.asList(sampleCategory));

		// Kiểm tra trang 0
		mockMvc.perform(get("/admin/product").param("page", "0").sessionAttr("account", adminAccount))
				.andExpect(status().isOk()).andExpect(view().name("product"))
				.andExpect(model().attribute("currentPage", 0)).andExpect(model().attribute("productPage", page0));

		// Kiểm tra trang 1
		mockMvc.perform(get("/admin/product").param("page", "1").sessionAttr("account", adminAccount))
				.andExpect(status().isOk()).andExpect(view().name("product"))
				.andExpect(model().attribute("currentPage", 1)).andExpect(model().attribute("productPage", page1));
	}
}
