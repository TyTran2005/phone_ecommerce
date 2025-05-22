package com.poly.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "orderdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "orderid", nullable = false)
	private Integer orderId;

	@Column(name = "productid", nullable = false)
	private Integer productId;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@ManyToOne
	@JoinColumn(name = "orderid", insertable = false, updatable = false)
	@JsonBackReference
	private Order order;

	@ManyToOne
	@JoinColumn(name = "productid", insertable = false, updatable = false)
	private Product product;
}
