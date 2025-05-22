package com.poly.rest.controller;

import com.poly.entity.Account;
import com.poly.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/account")
public class AccountRestController {

    @Autowired
    private AccountService accountService;

    // Get all accounts
    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accounts = accountService.findAll();
        return ResponseEntity.ok(accounts);
    }

    // Create a new account
    @PostMapping
    public ResponseEntity<?> createAccount(@RequestParam("username") String username,
                                             @RequestParam("password") String password,
                                             @RequestParam("email") String email,
                                             @RequestParam("fullname") String fullname,
                                             @RequestParam("admin") Boolean admin,
                                             @RequestParam("active") Boolean active) {
        try {
            Account account = new Account();
            account.setUsername(username);
            account.setPassword(password);
            account.setEmail(email);
            account.setFullname(fullname);
            account.setAdmin(admin);
            account.setActive(active);
            Account created = accountService.create(account);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating account");
        }
    }

    // Update an existing account
    @PutMapping
    public ResponseEntity<?> updateAccount(@RequestParam("id") Long id,
                                             @RequestParam("username") String username,
                                             @RequestParam("password") String password,
                                             @RequestParam("email") String email,
                                             @RequestParam("fullname") String fullname,
                                             @RequestParam("admin") Boolean admin,
                                             @RequestParam("active") Boolean active) {
        Optional<Account> optAccount = accountService.findById(id);
        if (!optAccount.isPresent()) {
            return ResponseEntity.badRequest().body("Account not found");
        }
        try {
            Account account = optAccount.get();
            account.setUsername(username);
            account.setPassword(password);
            account.setEmail(email);
            account.setFullname(fullname);
            account.setAdmin(admin);
            account.setActive(active);
			Account updated = accountService.update(account);
			return ResponseEntity.ok(updated);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Error updating account");
		}
	}

	// Delete an account
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
		if (!accountService.findById(id).isPresent()) {
			return ResponseEntity.badRequest().body("Account not found");
		}
		accountService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
