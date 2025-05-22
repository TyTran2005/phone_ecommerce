package com.poly.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.entity.Account;
import com.poly.repository.AccountRepository;
import com.poly.service.AccountService;

@Service
public class AccountServiceImpl implements AccountService {

	@Autowired
	private AccountRepository accountRepository;

	@Override
	public List<Account> findAll() {
		return accountRepository.findAll();
	}

	@Override
	public Optional<Account> findById(Long id) {
		return accountRepository.findById(id);
	}

	@Override
	public Account create(Account account) {
		return accountRepository.save(account);
	}

	@Override
	public Account update(Account account) {
		return accountRepository.save(account);
	}

	@Override
	public void delete(Long id) {
		accountRepository.deleteById(id);
	}

	@Override
	public Optional<Account> findByUsernameOrEmail(String login) {
		return accountRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(login, login);
	}
}
