package com.fpt.dto;

import com.fpt.entity.Role;
import com.fpt.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@NoArgsConstructor
public class UserListDTO extends RepresentationModel<UserListDTO> {

	private Long id;
	
	private String userName;
	
	private String email;

	private String firstName;

	private String lastName;

	private String phoneNumber;
	private Boolean isActive;
	private Role role;

	public User toEntity() {
		return new User(userName, email, firstName, lastName, phoneNumber,isActive,id,role);
	}
}