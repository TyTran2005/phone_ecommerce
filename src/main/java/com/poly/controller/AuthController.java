package com.poly.controller;

import com.poly.entity.Account;
import com.poly.service.AccountService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Base64;
import java.util.Optional;

@Controller
public class AuthController {

	@Autowired
	private AccountService accountService;

	@RequestMapping("/auth/login")
	public String login(Model model, HttpServletRequest request,
			@RequestParam(value = "redirect", required = false) String redirect) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("user".equals(cookie.getName())) {
					try {
						String encoded = cookie.getValue();
						byte[] decodedBytes = Base64.getDecoder().decode(encoded);
						String userInfo = new String(decodedBytes);
						int sepIndex = userInfo.indexOf(":");
						if (sepIndex > 0) {
							String savedUsername = userInfo.substring(0, sepIndex);
							String savedPassword = userInfo.substring(sepIndex + 1);
							model.addAttribute("savedUsername", savedUsername);
							model.addAttribute("savedPassword", savedPassword);
						}
					} catch (Exception e) {
					}
					break;
				}
			}
		}
		if (redirect != null) {
			model.addAttribute("redirectUrl", redirect);
		}
		return "login";
	}

	@PostMapping("/login")
	public String processLogin(@RequestParam("username") String loginInput, @RequestParam("password") String password,
			@RequestParam(value = "remember-me", required = false) String remember,
			@RequestParam(value = "redirect", required = false) String redirect, Model model,
			HttpServletRequest request, HttpServletResponse response) {
		loginInput = loginInput.trim();
		password = password.trim();

		Optional<Account> opt = accountService.findByUsernameOrEmail(loginInput);
		if (!opt.isPresent()) {
			model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
			return "login";
		}
		Account account = opt.get();
		if (!account.getPassword().equals(password)) {
			model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
			return "login";
		}
		if (account.getActive() == null || !account.getActive()) {
			model.addAttribute("error", "Tài khoản của bạn chưa được kích hoạt");
			return "login";
		}
		request.getSession().setAttribute("account", account);

		if ("on".equalsIgnoreCase(remember)) {
			String userInfo = loginInput + ":" + password;
			String encoded = Base64.getEncoder().encodeToString(userInfo.getBytes());
			Cookie cookie = new Cookie("user", encoded);
			cookie.setMaxAge(30 * 24 * 60 * 60); // 30 ngày
			cookie.setPath("/");
			response.addCookie(cookie);
		} else {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("user".equals(cookie.getName())) {
						cookie.setMaxAge(0);
						cookie.setPath("/");
						response.addCookie(cookie);
						break;
					}
				}
			}
		}

		if (account.getAdmin() != null && account.getAdmin()) {
			return "redirect:/admin/index";
		} else {
			if (redirect != null && !redirect.isEmpty()) {
				return "redirect:" + redirect;
			}
			return "redirect:/home/index";
		}
	}
}