package com.fpt.dto;

import com.fpt.entity.Role;
import com.fpt.entity.UserStatus;
import org.springframework.hateoas.RepresentationModel;

import com.fpt.entity.User;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserDTO extends RepresentationModel<UserDTO> {

	private Long id;
	
	private String userName;
	
	private String email;

	private String password;

	private String firstName;

	private String lastName;
	private String avatarUrl;
	private String phoneNumber;
	private Boolean isActive;
	private UserStatus status;
	private Role role=Role.CUSTOMER;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public User toEntity() {
		return new User(userName, email, password, firstName, lastName, phoneNumber,isActive,id,role,avatarUrl);
	}

}