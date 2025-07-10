package com.fpt.form;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class ProductFormForCreating {
	
	private Long category_id;
	
	private String name;
	
	private Long number_of_products;
	
	private float price;
	
	private String thumbnailUrl;
	
	private String description;
}
