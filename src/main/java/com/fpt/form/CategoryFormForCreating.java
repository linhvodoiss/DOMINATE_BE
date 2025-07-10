package com.fpt.form;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryFormForCreating {

	private String name;
	
	private String description;
	
	private List<Product> products;
	
	@Data
	@NoArgsConstructor
	public static class Product {
		private Long id;
	}

}
