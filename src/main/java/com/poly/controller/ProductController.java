package com.poly.controller;

import com.poly.entity.Product;
import com.poly.service.CategoryService;
import com.poly.service.ProductService;
import com.poly.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.util.List;

@Controller
public class ProductController {

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryService categoryService; // để lấy danh sách loại hàng

	// Hiển thị trang quản lý sản phẩm với phân trang
	@RequestMapping("/admin/product")
	public String product(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
		Product item = new Product();
		model.addAttribute("item", item);

		// Lấy danh sách categories
		List<Category> categories = categoryService.findAll();
		model.addAttribute("categories", categories);

		// Sử dụng phân trang: mỗi trang 4 sản phẩm
		Pageable pageable = PageRequest.of(page, 4);
		Page<Product> productPage = productService.findAll(pageable);
		model.addAttribute("productPage", productPage);
		model.addAttribute("currentPage", page);

		return "product";
	}

	@PostMapping("/admin/product/save")
	public String save(@ModelAttribute("item") Product product, @RequestParam("imageFile") MultipartFile imageFile,
			@RequestParam("action") String action) {
		try {
			// Đường dẫn lưu ảnh (cùng thư mục với category)
			String uploadDir = "C:/Users/thety/Documents/workspace-spring-tool-suite-4-4.27.0.RELEASE/ASM/src/main/resources/static/photos";
			Path uploadPath = Paths.get(uploadDir);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			if ("create".equals(action) || "update".equals(action)) {
				if (!imageFile.isEmpty()) {
					String originalFileName = imageFile.getOriginalFilename();
					// Đổi tên file để tránh trùng lặp và ký tự không hợp lệ
					String fileName = System.currentTimeMillis() + "_" + originalFileName;
					InputStream inputStream = imageFile.getInputStream();
					Path filePath = uploadPath.resolve(fileName);
					Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
					product.setImage(fileName);
				} else {
					if ("update".equals(action)) {
						productService.findById(product.getId().longValue())
								.ifPresent(oldProduct -> product.setImage(oldProduct.getImage()));
					}
				}

				if ("create".equals(action)) {
					productService.create(product);
				} else {
					productService.update(product);
				}
			} else if ("delete".equals(action)) {
				if (product.getId() != null) {
					productService.delete(product.getId().longValue());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/admin/product";
	}

	@GetMapping("/admin/product/delete/{id}")
	public String delete(@PathVariable("id") Integer id) {
		productService.delete(id.longValue());
		return "redirect:/admin/product";
	}
}
