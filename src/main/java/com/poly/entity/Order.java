package com.poly.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "orderdate")
	private LocalDateTime orderDate;

	@Column(name = "totalamount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;

	@Column(length = 50)
	private String status;
	
	@Column(name = "address", length = 255)
	private String address;
	
	@Column(name = "phonenumber", length = 50)
	private String phonenumber;
	
	@ManyToOne
	@JoinColumn(name = "accountid", nullable = false)
	private Account account;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<OrderDetail> orderDetails;
}
