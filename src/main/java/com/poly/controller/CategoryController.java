package com.poly.controller;

import com.poly.entity.Category;
import com.poly.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Controller
public class CategoryController {

	@Autowired
	private CategoryService categoryService;

	@RequestMapping("/admin/category")
	public String category(Model model) {
		Category item = new Category();
		model.addAttribute("item", item);
		List<Category> items = categoryService.findAll();
		model.addAttribute("items", items);
		return "category";
	}

	@PostMapping("/admin/category/save")
	public String save(@ModelAttribute("item") Category category, @RequestParam("logoFile") MultipartFile logoFile,
			@RequestParam("action") String action) {
		try {
			String uploadDir = "C:/Users/thety/Documents/workspace-spring-tool-suite-4-4.27.0.RELEASE/ASM/src/main/resources/static/photos";
			Path uploadPath = Paths.get(uploadDir);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			if ("create".equals(action) || "update".equals(action)) {
				if (!logoFile.isEmpty()) {
					String originalFileName = logoFile.getOriginalFilename();
					String fileName = System.currentTimeMillis() + "_" + originalFileName;
					InputStream inputStream = logoFile.getInputStream();
					Path filePath = uploadPath.resolve(fileName);
					Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
					category.setLogo(fileName);
				} else {
					if ("update".equals(action)) {
						categoryService.findById(category.getId().longValue())
								.ifPresent(oldCategory -> category.setLogo(oldCategory.getLogo()));
					}
				}

				if ("create".equals(action)) {
					categoryService.create(category);
				} else {
					categoryService.update(category);
				}
			} else if ("delete".equals(action)) {
				if (category.getId() != null) {
					categoryService.delete(category.getId().longValue());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/admin/category";
	}

	@GetMapping("/admin/category/delete/{id}")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		Optional<Category> optional = categoryService.findById(id.longValue());
		if (optional.isPresent()) {
			Category category = optional.get();
			if (category.getProducts() != null && !category.getProducts().isEmpty()) {
				// Nếu có sản phẩm, không cho xóa và gửi thông báo lỗi
				redirectAttributes.addFlashAttribute("error", "Không thể xóa loại hàng có sản phẩm!");
				return "redirect:/admin/category";
			} else {
				categoryService.delete(id.longValue());
			}
		}
		return "redirect:/admin/category";
	}
}
