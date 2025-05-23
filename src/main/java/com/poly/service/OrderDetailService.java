package com.poly.service;

import com.poly.entity.OrderDetail;
import java.util.List;
import java.util.Optional;

public interface OrderDetailService {

	List<OrderDetail> findAll();

	Optional<OrderDetail> findById(Long id);

	OrderDetail create(OrderDetail orderDetail);

	OrderDetail update(OrderDetail orderDetail);

	void delete(Long id);

	List<OrderDetail> findByOrderId(Integer orderId);
}
