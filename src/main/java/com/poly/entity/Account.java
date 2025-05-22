package com.poly.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, length = 50, unique = true)
	private String username;

	@Column(nullable = false, length = 255)
	private String password;

	@Column(nullable = false, length = 100, unique = true)
	private String email;

	@Column(length = 100)
	private String fullname;

	private Boolean admin;

	private Boolean active;
}
