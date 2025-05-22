package com.poly.service;

import com.poly.entity.Cart;
import com.poly.entity.CartId;

import java.util.List;
import java.util.Optional;

public interface CartService {
    
    List<Cart> findAll();

    Optional<Cart> findById(CartId id);

    Cart create(Cart cart);

    Cart update(Cart cart);

    void delete(CartId id);

    Optional<Cart> findByAccountIdAndProductId(Integer accountId, Integer productId);

    List<Cart> findByAccount(Integer accountId);

    int getCartItemCount(Integer accountId);

    void deleteByAccountId(Integer accountId);
}
