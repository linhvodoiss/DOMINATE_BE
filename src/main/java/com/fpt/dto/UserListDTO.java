package com.fpt.dto;

import com.fpt.entity.Role;
import com.fpt.entity.User;
import com.fpt.entity.UserStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserListDTO extends RepresentationModel<UserListDTO> {

	private Long id;
	
	private String userName;
	
	private String email;

	private String firstName;

	private String lastName;
	private String avatarUrl;
	private String phoneNumber;
	private Boolean isActive;
	private Role role;
	private UserStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	public User toEntity() {
		return new User(userName, email, firstName, lastName, phoneNumber,isActive,id,role,avatarUrl,status,createdAt,updatedAt);
	}
}