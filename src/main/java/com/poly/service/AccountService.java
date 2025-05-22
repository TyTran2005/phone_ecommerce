package com.poly.service;

import com.poly.entity.Account;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    
    List<Account> findAll();

    Optional<Account> findById(Long id);

    Account create(Account account);

    Account update(Account account);

    void delete(Long id);

    Optional<Account> findByUsernameOrEmail(String login);
}
