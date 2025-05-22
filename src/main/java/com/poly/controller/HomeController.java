package com.poly.controller;

import com.poly.service.CategoryService;
import com.poly.service.ProductService;
import com.poly.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DecimalFormat;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @RequestMapping("/home/index")
    public String home(Model model, @RequestParam(defaultValue = "0") int page) {
        int pageSize = 8;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Product> productPage = productService.findAll(pageable);
        List<Product> products = productPage.getContent();

        formatProductPrices(products);

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("products", products);
        model.addAttribute("selectedCategoryId", null);
        model.addAttribute("isHomePage", true);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("currentOrder", null);
        return "home";
    }

    @RequestMapping("/products/category/{id}")
    public String getProductsByCategory(@PathVariable("id") Long categoryId,
                                        @RequestParam(defaultValue = "0") int page,
                                        Model model) {
        int pageSize = 8;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Product> productPage = productService.findByCategoryId(categoryId, pageable);
        List<Product> products = productPage.getContent();

        formatProductPrices(products);

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("products", products);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("isHomePage", true);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("currentOrder", null);
        return "home";
    }

    @RequestMapping("/home/search")
    public String search(@RequestParam("query") String query, Model model) {
        List<Product> products = productService.findByNameContaining(query);

        formatProductPrices(products);

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("products", products);
        model.addAttribute("isHomePage", true);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);
        model.addAttribute("currentOrder", null);
        return "home";
    }

    @RequestMapping("/home/sort")
    public String sortProducts(@RequestParam("order") String order, 
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {
        int pageSize = 8;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Product> productPage;

        if ("asc".equalsIgnoreCase(order)) {
            productPage = productService.findAllSortedByPriceAsc(pageable);
        } else if ("desc".equalsIgnoreCase(order)) {
            productPage = productService.findAllSortedByPriceDesc(pageable);
        } else {
            productPage = productService.findAll(pageable);
            order = null;
        }

        List<Product> products = productPage.getContent();

        formatProductPrices(products);

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("products", products);
        model.addAttribute("selectedCategoryId", null);
        model.addAttribute("isHomePage", true);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("currentOrder", order);
        return "home";
    }

    // Helper method to format product prices
    private void formatProductPrices(List<Product> products) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        for (Product product : products) {
            String formattedPrice = formatter.format(product.getPrice());
            product.setFormattedPrice(formattedPrice);
        }
    }
}
