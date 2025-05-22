package com.poly.service.impl;

import com.poly.entity.Order;
import com.poly.repository.OrderRepository;
import com.poly.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private OrderRepository orderRepository;

	@Override
	public List<Order> findAll() {
		return orderRepository.findAll();
	}

	@Override
	public Optional<Order> findById(Long id) {
		return orderRepository.findById(id);
	}

	@Override
	public Order create(Order order) {
		return orderRepository.save(order);
	}

	@Override
	public Order update(Order order) {
		return orderRepository.save(order);
	}

	@Override
	public void delete(Long id) {
		orderRepository.deleteById(id);
	}

	@Override
	public void deleteByAccountId(Integer accountId) {
		orderRepository.deleteByAccountId(accountId);
	}

	@Override
	public List<Order> findByAccountId(Integer accountId) {
		return orderRepository.findByAccountIdWithDetails(accountId);
	}

	@Override
	public Page<Order> findAll(Pageable pageable) {
		return orderRepository.findAll(pageable);
	}
}
