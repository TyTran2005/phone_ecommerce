package com.poly.service;

import com.poly.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderService {

	List<Order> findAll();

	Optional<Order> findById(Long id);

	Order create(Order order);

	Order update(Order order);

	void delete(Long id);

	void deleteByAccountId(Integer accountId);

	List<Order> findByAccountId(Integer accountId);

	Page<Order> findAll(Pageable pageable);
}
