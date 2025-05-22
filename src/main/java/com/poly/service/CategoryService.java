package com.poly.service;

import com.poly.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
	List<Category> findAll();

	Optional<Category> findById(Long id);

	Category create(Category category);

	Category update(Category category);

	void delete(Long id);
}
