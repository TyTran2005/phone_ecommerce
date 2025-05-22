package com.poly.service.impl;

import com.poly.entity.Cart;
import com.poly.entity.CartId;
import com.poly.repository.CartRepository;
import com.poly.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepository cartRepository;

	@Override
	public List<Cart> findAll() {
		return cartRepository.findAll();
	}

	@Override
	public Optional<Cart> findById(CartId id) {
		return cartRepository.findById(id);
	}

	@Override
	public Cart create(Cart cart) {
		return cartRepository.save(cart);
	}

	@Override
	public Cart update(Cart cart) {
		return cartRepository.save(cart);
	}

	@Override
	public void delete(CartId id) {
		cartRepository.deleteById(id);
	}

	@Override
	public Optional<Cart> findByAccountIdAndProductId(Integer accountId, Integer productId) {
		return cartRepository.findByAccountidAndProductid(accountId, productId);
	}

	@Override
	public List<Cart> findByAccount(Integer accountId) {
		return cartRepository.findByAccountid(accountId);
	}

	@Override
	public int getCartItemCount(Integer accountId) {
		return findByAccount(accountId).stream().mapToInt(Cart::getQuantity).sum();
	}

	@Override
	public void deleteByAccountId(Integer accountId) {
		cartRepository.deleteByAccountid(accountId);
	}
}
