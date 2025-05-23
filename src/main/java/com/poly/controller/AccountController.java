package com.poly.controller;

import com.poly.entity.Account;
import com.poly.service.AccountService;
import com.poly.service.CartService;
import com.poly.service.EmailService;
import com.poly.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AccountController {

	@Autowired
	private AccountService accountService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@RequestMapping("/account/logout")
	public String logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return "redirect:/auth/login";
	}

	@RequestMapping("/account/register")
	public String register(Model model) {
		return "register";
	}

	@PostMapping("/register")
	public String processRegister(@RequestParam("username") String username, @RequestParam("email") String email,
			@RequestParam("fullname") String fullname, @RequestParam("password") String password,
			@RequestParam("confirmPassword") String confirmPassword, Model model) {

		username = username.trim();
		email = email.trim();
		fullname = fullname.trim();
		password = password.trim();
		confirmPassword = confirmPassword.trim();

		if (!password.equals(confirmPassword)) {
			model.addAttribute("error", "Mật khẩu không khớp");
			return "register";
		}

		Optional<Account> optUser = accountService.findByUsernameOrEmail(username);
		Optional<Account> optEmail = accountService.findByUsernameOrEmail(email);
		if (optUser.isPresent() || optEmail.isPresent()) {
			model.addAttribute("error", "Username hoặc Email đã tồn tại");
			return "register";
		}

		Account newAccount = new Account();
		newAccount.setUsername(username);
		newAccount.setEmail(email);
		newAccount.setFullname(fullname);
		newAccount.setPassword(password);
		newAccount.setAdmin(false);
		newAccount.setActive(false);

		accountService.create(newAccount);

		return "redirect:/auth/login";
	}

	@RequestMapping("/account/update")
	public String update(HttpServletRequest request, Model model) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			String currentUrl = request.getRequestURI();
			String query = request.getQueryString();
			if (query != null && !query.isEmpty()) {
				currentUrl += "?" + query;
			}
			return "redirect:/auth/login?redirect=" + currentUrl;
		}
		model.addAttribute("account", account);
		return "update";
	}

	@PostMapping("/account/update")
	public String processUpdate(@RequestParam("email") String email, @RequestParam("fullname") String fullname,
			HttpServletRequest request, Model model) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			String currentUrl = request.getRequestURI();
			String query = request.getQueryString();
			if (query != null && !query.isEmpty()) {
				currentUrl += "?" + query;
			}
			return "redirect:/auth/login?redirect=" + currentUrl;
		}
		email = email.trim();
		fullname = fullname.trim();

		Optional<Account> opt = accountService.findByUsernameOrEmail(email);
		if (opt.isPresent() && !opt.get().getId().equals(account.getId())) {
			model.addAttribute("error", "Email bạn cập nhật đã tồn tại");
			model.addAttribute("account", account);
			return "update";
		}
		account.setEmail(email);
		account.setFullname(fullname);

		accountService.update(account);
		request.getSession().setAttribute("account", account);

		model.addAttribute("success", "Cập nhật thông tin thành công");
		model.addAttribute("account", account);
		return "update";
	}

	@RequestMapping("/account/change-password")
	public String changepassword(HttpServletRequest request, Model model) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			String currentUrl = request.getRequestURI();
			String query = request.getQueryString();
			if (query != null && !query.isEmpty()) {
				currentUrl += "?" + query;
			}
			return "redirect:/auth/login?redirect=" + currentUrl;
		}
		model.addAttribute("account", account);
		return "change-password";
	}

	@PostMapping("/account/change-password")
	public String processChangePassword(@RequestParam("currentPassword") String currentPassword,
			@RequestParam("newPassword") String newPassword, HttpServletRequest request, Model model) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			String currentUrl = request.getRequestURI();
			String query = request.getQueryString();
			if (query != null && !query.isEmpty()) {
				currentUrl += "?" + query;
			}
			return "redirect:/auth/login?redirect=" + currentUrl;
		}
		currentPassword = currentPassword.trim();
		newPassword = newPassword.trim();

		if (!account.getPassword().equals(currentPassword)) {
			model.addAttribute("error", "Mật khẩu hiện tại không đúng");
			model.addAttribute("account", account);
			return "change-password";
		}

		account.setPassword(newPassword);
		accountService.update(account);
		request.getSession().setAttribute("account", account);

		model.addAttribute("success", "Đổi mật khẩu thành công");
		model.addAttribute("account", account);
		return "change-password";
	}

	@RequestMapping("/account/delete")
	@Transactional // Ensure that all delete operations are performed in a transaction
	public String deleteAccount(HttpServletRequest request, Model model) {
		Account account = (Account) request.getSession().getAttribute("account");
		if (account == null) {
			String currentUrl = request.getRequestURI();
			String query = request.getQueryString();
			if (query != null && !query.isEmpty()) {
				currentUrl += "?" + query;
			}
			return "redirect:/auth/login?redirect=" + currentUrl;
		}
		Integer accountId = account.getId();

		// Delete all cart items for the account
		cartService.deleteByAccountId(accountId);

		// Delete all orders for the account
		orderService.deleteByAccountId(accountId);

		// Now delete the account
		accountService.delete(Long.valueOf(accountId));

		// Invalidate session after deletion
		request.getSession().invalidate();

		return "redirect:/auth/login?deleted=true";
	}

	@RequestMapping("/account/forgot-password")
	public String forgotpassword(Model model) {
		return "forgot-password";
	}

	@PostMapping("/account/forgot-password")
	public String processForgotPassword(@RequestParam("email") String email, Model model) {
		email = email.trim();
		Optional<Account> opt = accountService.findByUsernameOrEmail(email);
		if (!opt.isPresent()) {
			model.addAttribute("error", "Không tìm thấy email");
			return "forgot-password";
		}
		Account account = opt.get();

		if (account.getAdmin() != null && account.getAdmin()) {
			model.addAttribute("error", "Không được lấy lại mật khẩu cho tài khoản admin");
			return "forgot-password";
		}

		String subject = "Forgot Password - ShopOnline";
		String text = "Your password is: " + account.getPassword();
		try {
			emailService.sendSimpleMessage(email, subject, text);
			model.addAttribute("success", "Mật khẩu của bạn đã được gửi");
		} catch (Exception e) {
			model.addAttribute("error", "Gửi thất bại");
		}
		return "forgot-password";
	}

	// Hiển thị form kích hoạt tài khoản
	@RequestMapping("/account/activate")
	public String showActivateForm() {
		return "activate"; // view: activate.html
	}

	// Xử lý kích hoạt: Khi người dùng nhập username và email vào form kích hoạt
	@PostMapping("/account/activate")
	public String processActivate(@RequestParam("username") String username, @RequestParam("email") String email,
			HttpServletRequest request, Model model) {
		username = username.trim();
		email = email.trim();
		Optional<Account> opt = accountService.findByUsernameOrEmail(username);
		if (!opt.isPresent() || !opt.get().getEmail().equalsIgnoreCase(email)) {
			model.addAttribute("error", "Username và Email không khớp");
			return "activate";
		}
		Account account = opt.get();
		if (account.getActive() != null && account.getActive()) {
			model.addAttribute("error", "Tài khoản đã được kích hoạt");
			return "activate";
		}
		// Tạo mã kích hoạt ngẫu nhiên gồm 5 chữ số
		int activationCode = (int) (Math.random() * 90000) + 10000;
		// Lưu mã kích hoạt và id tài khoản vào session để kiểm tra sau
		request.getSession().setAttribute("activationCode", activationCode);
		request.getSession().setAttribute("activationAccountId", account.getId());

		// Gửi email chứa mã kích hoạt đến email của người dùng
		String subject = "Kích hoạt tài khoản - ShopOnline";
		String text = "Mã kích hoạt tài khoản của bạn là: " + activationCode;
		try {
			emailService.sendSimpleMessage(email, subject, text);
			model.addAttribute("success", "Mã kích hoạt đã được gửi đến email của bạn");
		} catch (Exception e) {
			model.addAttribute("error", "Gửi email thất bại, vui lòng thử lại sau");
			return "activate";
		}
		// Chuyển hướng đến form xác thực mã kích hoạt
		return "redirect:/account/verify-activation";
	}

	// Hiển thị form xác thực mã kích hoạt
	@RequestMapping("/account/verify-activation")
	public String showVerifyActivationForm() {
		return "verify-activation"; // view: verify-activation.html
	}

	// Xử lý xác thực mã kích hoạt
	@PostMapping("/account/verify-activation")
	public String processVerifyActivation(@RequestParam("code") String codeInput, HttpServletRequest request,
			Model model) {
		Object sessionCodeObj = request.getSession().getAttribute("activationCode");
		Object accountIdObj = request.getSession().getAttribute("activationAccountId");
		if (sessionCodeObj == null || accountIdObj == null) {
			model.addAttribute("error", "Phiên kích hoạt đã hết hạn, vui lòng thử lại");
			return "verify-activation";
		}
		int sessionCode = (Integer) sessionCodeObj;
		if (!codeInput.trim().equals(String.valueOf(sessionCode))) {
			model.addAttribute("error", "Mã kích hoạt không đúng");
			return "verify-activation";
		}
		// Mã kích hoạt đúng, cập nhật trạng thái active của tài khoản thành true
		Long accountId = Long.valueOf(accountIdObj.toString());
		Optional<Account> optAccount = accountService.findById(accountId);
		if (optAccount.isPresent()) {
			Account account = optAccount.get();
			account.setActive(true);
			accountService.update(account);
			// Xóa thông tin kích hoạt khỏi session
			request.getSession().removeAttribute("activationCode");
			request.getSession().removeAttribute("activationAccountId");
			model.addAttribute("success", "Tài khoản của bạn đã được kích hoạt thành công");
			return "verify-activation";
		} else {
			model.addAttribute("error", "Không tìm thấy tài khoản");
			return "verify-activation";
		}
	}

}
