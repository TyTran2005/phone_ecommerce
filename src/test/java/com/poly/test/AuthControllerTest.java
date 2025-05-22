package com.poly.test;

import com.poly.entity.Account;
import com.poly.service.AccountService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AccountService accountService;

	private Account account;

	@BeforeEach
	void setUp() {
		account = new Account();
		account.setId(1);
		account.setUsername("user1");
		account.setPassword("12345");
		account.setEmail("khang@gmail.com");
		account.setActive(true);
		account.setAdmin(false);
	}

	@Test
	public void testLoginValid() throws Exception {
		Mockito.when(accountService.findByUsernameOrEmail("user1")).thenReturn(Optional.of(account));

		mockMvc.perform(post("/login").param("username", "user1").param("password", "12345"))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/home/index"));
	}

	@Test
	public void testLoginInvalidUsername() throws Exception {
		Mockito.when(accountService.findByUsernameOrEmail("invalidUser")).thenReturn(Optional.empty());

		mockMvc.perform(post("/login").param("username", "invalidUser").param("password", "pass123"))
				.andExpect(status().isOk()).andExpect(model().attributeExists("error")).andExpect(view().name("login"));
	}

	@Test
	public void testLoginWithEmail() throws Exception {
		account.setEmail("user2@example.com");
		Mockito.when(accountService.findByUsernameOrEmail("user2@example.com")).thenReturn(Optional.of(account));

		mockMvc.perform(post("/login").param("username", "user2@example.com").param("password", "12345"))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/home/index"));
	}

	@Test
	public void testInactiveAccountLogin() throws Exception {
		account.setActive(false);
		Mockito.when(accountService.findByUsernameOrEmail("user4")).thenReturn(Optional.of(account));

		mockMvc.perform(post("/login").param("username", "user4").param("password", "12345")).andExpect(status().isOk())
				.andExpect(model().attributeExists("error")).andExpect(view().name("login"));
	}

	@Test
	public void testLoginWithRememberMeOn() throws Exception {
		Mockito.when(accountService.findByUsernameOrEmail("user1")).thenReturn(Optional.of(account));

		mockMvc.perform(post("/login").param("username", "user1").param("password", "12345").param("remember-me", "on"))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/home/index"))
				.andExpect(cookie().exists("user"));
	}

	@Test
	public void testLoginWithRememberMeOff() throws Exception {
		Mockito.when(accountService.findByUsernameOrEmail("user1")).thenReturn(Optional.of(account));

		mockMvc.perform(post("/login").param("username", "user1").param("password", "12345"))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/home/index"))
				.andExpect(cookie().doesNotExist("user"));
	}

	@Test
	public void testAdminLogin() throws Exception {
		account.setUsername("admin");
		account.setPassword("admin123");
		account.setAdmin(true);

		Mockito.when(accountService.findByUsernameOrEmail(Mockito.anyString())).thenReturn(Optional.of(account));

		mockMvc.perform(post("/login").param("username", "admin").param("password", "admin123")).andDo(print())
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/index"));
	}

}
