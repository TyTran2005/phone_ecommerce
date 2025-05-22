package com.poly.controller;

import com.poly.entity.Account;
import com.poly.service.CartService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

	@Autowired
	private CartService cartService;

	@ModelAttribute("cartItemCount")
	public Integer cartItemCount(HttpServletRequest request) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			return 0;
		}
		return cartService.getCartItemCount(account.getId());
	}
}
