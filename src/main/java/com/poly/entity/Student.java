package com.poly.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
	@Id
	String email;
	String fullname;
	Double marks;
	Boolean gender;
	String country;
}
