package com.poly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poly.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
