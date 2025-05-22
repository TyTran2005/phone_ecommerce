package com.poly.controller;

import com.poly.entity.Account;
import com.poly.service.AccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class AccountAController {

	@Autowired
	private AccountService accountService;

	@RequestMapping("/admin/user")
	public String user(Model model) {
		Account item = new Account();
		model.addAttribute("item", item);
		List<Account> items = accountService.findAll();
		model.addAttribute("items", items);
		return "user";
	}

	@PostMapping("/admin/user/save")
	public String save(@ModelAttribute("item") Account account, @RequestParam("action") String action) {
		try {
			if ("create".equals(action)) {
				accountService.create(account);
			} else if ("update".equals(action)) {
				accountService.update(account);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/admin/user";
	}

	@GetMapping("/admin/user/delete/{id}")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		Optional<Account> optional = accountService.findById(id.longValue());
		if (optional.isPresent()) {
			accountService.delete(id.longValue());
		}
		return "redirect:/admin/user";
	}
}
