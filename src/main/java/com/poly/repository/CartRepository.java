package com.poly.repository;

import com.poly.entity.Cart;
import com.poly.entity.CartId;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, CartId> {

	List<Cart> findByAccountid(Integer accountid);

	Optional<Cart> findByAccountidAndProductid(Integer accountid, Integer productid);

	@Modifying
	@Query("DELETE FROM Cart c WHERE c.accountid = ?1")
	void deleteByAccountid(Integer accountId);
}
