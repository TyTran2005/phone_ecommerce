package com.poly.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poly.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByCategoryId(Long categoryId);
	
	Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

	List<Product> findByNameContainingIgnoreCase(String name);

	Page<Product> findAllByOrderByPriceAsc(Pageable pageable);

	Page<Product> findAllByOrderByPriceDesc(Pageable pageable);

	Page<Product> findAll(Pageable pageable);

}
