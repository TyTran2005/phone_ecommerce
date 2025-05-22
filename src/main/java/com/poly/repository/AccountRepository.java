package com.poly.repository;

import com.poly.entity.Account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);
}
