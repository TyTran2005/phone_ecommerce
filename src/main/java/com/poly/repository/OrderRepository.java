package com.poly.repository;

import com.poly.entity.Order;
import com.poly.entity.Product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	@Modifying
	@Query("DELETE FROM Order o WHERE o.account.id = ?1")
	void deleteByAccountId(Integer accountId);

	@Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.account.id = ?1")
    List<Order> findByAccountIdWithDetails(Integer accountId);
	
	Page<Order> findAll(Pageable pageable);
}
