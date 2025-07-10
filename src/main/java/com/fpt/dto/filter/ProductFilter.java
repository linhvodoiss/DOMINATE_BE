package com.fpt.dto.filter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductFilter {
	private int minPrice;

	private int maxPrice;
	
	private int category_id;
	
}
