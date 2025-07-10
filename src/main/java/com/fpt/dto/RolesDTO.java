package com.fpt.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class RolesDTO {

	private String roleValue;
	
	private String roleName;

	public RolesDTO(String roleValue, String roleName) {
		super();
		this.roleValue = roleValue;
		this.roleName = roleName;
	}
	
}
