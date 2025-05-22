package com.poly.rest.controller;

import com.poly.entity.Category;
import com.poly.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/category")
public class CategoryRestController {

    @Autowired
    private CategoryService categoryService;

    // Đường dẫn upload file (có thể cấu hình từ properties)
    private final String uploadDir = "C:/Users/thety/Documents/workspace-spring-tool-suite-4-4.27.0.RELEASE/ASM/src/main/resources/static/photos";

    // Lấy danh sách loại hàng
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    // Tạo mới loại hàng (bao gồm upload logo)
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestParam("name") String name,
                                              @RequestParam(value = "logoFile", required = false) MultipartFile logoFile) {
        Category category = new Category();
        category.setName(name);
        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String originalFileName = logoFile.getOriginalFilename();
                String fileName = System.currentTimeMillis() + "_" + originalFileName;
                InputStream inputStream = logoFile.getInputStream();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                category.setLogo(fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi upload file");
        }
        Category created = categoryService.create(category);
        return ResponseEntity.ok(created);
    }

    // Cập nhật loại hàng (có thể upload logo mới)
    @PutMapping
    public ResponseEntity<?> updateCategory(@RequestParam("id") Long id, @RequestParam("name") String name,
                                              @RequestParam(value = "logoFile", required = false) MultipartFile logoFile) {
        Optional<Category> optCategory = categoryService.findById(id);
        if (!optCategory.isPresent()) {
            return ResponseEntity.badRequest().body("Category not found");
        }
        Category category = optCategory.get();
        category.setName(name);
        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String originalFileName = logoFile.getOriginalFilename();
                String fileName = System.currentTimeMillis() + "_" + originalFileName;
                InputStream inputStream = logoFile.getInputStream();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                category.setLogo(fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi upload file");
        }
        Category updated = categoryService.update(category);
        return ResponseEntity.ok(updated);
    }

    // Xóa loại hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        Optional<Category> optional = categoryService.findById(id);
        if (!optional.isPresent()) {
            return ResponseEntity.badRequest().body("Category not found");
        }
        Category category = optional.get();
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            return ResponseEntity.badRequest().body("Không thể xóa loại hàng có sản phẩm!");
        }
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
