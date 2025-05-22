package com.poly.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CartId.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Cart {

	@Id
	@Column(name = "productid", nullable = false)
	private Integer productid;

	@Id
	@Column(name = "accountid", nullable = false)
	private Integer accountid;

	@Column(nullable = false)
	private Integer quantity;

	// Quan hệ Many-to-One với Product
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productid", insertable = false, updatable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private Product product;

	// Quan hệ Many-to-One với Account (đảm bảo Account cũng được cấu hình tương tự)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accountid", insertable = false, updatable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "carts" })
	private Account account;
}
