package com.fpt.dto;

import com.fpt.entity.Role;

public class LoginInfoUser {

	private String token;

	private String userName;

	private String email;

	private String firstName;

	private String lastName;
	
	private String phoneNumber;

	private Role role;
	private Long id;

	private String status;



	public LoginInfoUser(String token, String userName, String email, String firstName, String lastName, String phoneNumber, Role role, Long id,
                         String status) {
		this.token = token;
		this.userName = userName;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
		this.role = role;
		this.id = id;
		this.status = status;
	}

	public String getToken() {
		return token;
	}

	public String getUserName() {
		return userName;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
	
	public String getphoneNumber() {
		return phoneNumber;
	}

	public Role getRole() {
		return role;
	}

	public String getStatus() {
		return status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
