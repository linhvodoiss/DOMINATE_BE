package com.fpt.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`User`")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE `User` SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "`id`", unique = true, nullable = false)
	private Long id;

	@Column(name = "`username`", nullable = false, length = 50, unique = true)
	private String userName;

	@Column(name = "`email`", nullable = false, length = 50, unique = true)
	private String email;

	@Column(name = "`password`", nullable = false, length = 800)
	private String password;

	@Column(name = "`firstName`", nullable = false, length = 50)
	private String firstName;

	@Column(name = "`lastName`", nullable = false, length = 50)
	private String lastName;
	
	@Column(name = "phoneNumber", nullable = false, length = 50)
	private String phoneNumber;

	@Formula("concat(firstName, ' ', lastName)")
	private String fullName;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private Role role = Role.CUSTOMER;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "`status`", nullable = false)
	private UserStatus status = UserStatus.NOT_ACTIVE;
	private Boolean isActive = true;
	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted = false;

	@Column(name = "avatarUrl")
	private String avatarUrl;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;


	public User(Long id, String userName, String email, String password, String firstName, String lastName, String phoneNumber, String fullName, Role role, UserStatus status, Boolean isActive, String avatarUrl,LocalDateTime createdAt,LocalDateTime updatedAt) {
		this.id = id;
		this.userName = userName;
		this.email = email;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
		this.fullName = fullName;
		this.role = role;
		this.status = status;
		this.isActive = isActive;
		this.avatarUrl = avatarUrl;
		this.createdAt=createdAt;
		this.updatedAt=updatedAt;
	}

	public User(String userName, String email, String password, String firstName, String lastName, String phoneNumber, Boolean isActive, Long id, Role role,String avatarUrl, UserStatus status) {
		this.userName = userName;
		this.role = role;
		this.email = email;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
		this.isActive = isActive;
		this.id=id;
		this.avatarUrl = avatarUrl;
		this.status = status;
	}

	public User(String userName, String email, String firstName, String lastName, String phoneNumber, Boolean isActive, Long id, Role role,String avatarUrl, UserStatus status,LocalDateTime createdAt,LocalDateTime updatedAt) {
		this.userName = userName;
		this.role = role;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
		this.isActive = isActive;
		this.id=id;
		this.avatarUrl = avatarUrl;
		this.status = status;
		this.createdAt=createdAt;
		this.updatedAt=updatedAt;
	}

}