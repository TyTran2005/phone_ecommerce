package com.poly.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false, length = 255)
	private String image;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "categoryid", nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "products" })
	private Category category;

	// Thuộc tính lưu giá đã format (ví dụ "$999.99")
	private String formattedPrice;

	public String getFormattedPrice() {
		return formattedPrice;
	}

	public void setFormattedPrice(String formattedPrice) {
		this.formattedPrice = formattedPrice;
	}
}
