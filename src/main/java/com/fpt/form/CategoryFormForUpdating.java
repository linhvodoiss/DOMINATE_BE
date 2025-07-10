package com.fpt.form;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryFormForUpdating {
	private Long id;

	private String name;

	private String description;

}
