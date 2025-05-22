package com.poly.rest.controller;

import com.poly.entity.Product;
import com.poly.entity.Category;
import com.poly.service.ProductService;
import com.poly.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/product")
public class ProductRestController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService; // Inject CategoryService

    // Upload directory (you may configure this in application.properties)
    private final String uploadDir = "C:/Users/thety/Documents/workspace-spring-tool-suite-4-4.27.0.RELEASE/ASM/src/main/resources/static/photos";

    // Get all products (with pagination)
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.findAll(pageable));
    }

    // Create new product (with image upload)
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestParam("name") String name,
                                             @RequestParam("price") String price,
                                             @RequestParam("quantity") Integer quantity,
                                             @RequestParam("categoryId") Long categoryId,
                                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setPrice(new java.math.BigDecimal(price));
            product.setQuantity(quantity);
            // Retrieve and set category from categoryId
            Optional<Category> optCategory = categoryService.findById(categoryId);
            if (!optCategory.isPresent()) {
                return ResponseEntity.badRequest().body("Category not found");
            }
            product.setCategory(optCategory.get());

            if (imageFile != null && !imageFile.isEmpty()) {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String originalFileName = imageFile.getOriginalFilename();
                String fileName = System.currentTimeMillis() + "_" + originalFileName;
                InputStream inputStream = imageFile.getInputStream();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                product.setImage(fileName);
            }
            Product created = productService.create(product);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi upload ảnh hoặc tạo sản phẩm");
        }
    }

    // Update product (with image upload)
    @PutMapping
    public ResponseEntity<?> updateProduct(@RequestParam("id") Long id,
                                             @RequestParam("name") String name,
                                             @RequestParam("price") String price,
                                             @RequestParam("quantity") Integer quantity,
                                             @RequestParam("categoryId") Long categoryId,
                                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        Optional<Product> optProduct = productService.findById(id);
        if (!optProduct.isPresent()) {
            return ResponseEntity.badRequest().body("Product not found");
        }
        try {
            Product product = optProduct.get();
            product.setName(name);
            product.setPrice(new java.math.BigDecimal(price));
            product.setQuantity(quantity);
            // Retrieve and set category from categoryId
            Optional<Category> optCategory = categoryService.findById(categoryId);
            if (!optCategory.isPresent()) {
                return ResponseEntity.badRequest().body("Category not found");
            }
            product.setCategory(optCategory.get());

            if (imageFile != null && !imageFile.isEmpty()) {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String originalFileName = imageFile.getOriginalFilename();
                String fileName = System.currentTimeMillis() + "_" + originalFileName;
                InputStream inputStream = imageFile.getInputStream();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                product.setImage(fileName);
            }
            Product updated = productService.update(product);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi cập nhật sản phẩm");
        }
    }

    // Delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (!productService.findById(id).isPresent()) {
            return ResponseEntity.badRequest().body("Product not found");
        }
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
