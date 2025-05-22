package com.poly.service;

import com.poly.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
	List<Product> findAll();

	List<Product> findByCategoryId(Long categoryId);

	Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

	Optional<Product> findById(Long id);

	Product create(Product product);

	Product update(Product product);

	void delete(Long id);

	List<Product> findByNameContaining(String name);

	Page<Product> findAllSortedByPriceAsc(Pageable pageable);

	Page<Product> findAllSortedByPriceDesc(Pageable pageable);

	Page<Product> findAll(Pageable pageable);
}
