package com.fpt.form;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductFormForUpdating {
	private Long id;

	private String name;
	
	private Long category_id;
	
	private Long number_of_products;
	
	private float price;
	
	private String thumbnailUrl;
	
	private String description;
	
}
