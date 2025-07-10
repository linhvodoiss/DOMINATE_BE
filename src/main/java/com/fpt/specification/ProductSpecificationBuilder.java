//package com.fpt.specification;
//
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.util.StringUtils;
//
//import com.fpt.dto.filter.ProductFilter;
//import com.fpt.entity.Product;
//
//public class ProductSpecificationBuilder {
//
//	private ProductFilter filter;
//	private String search;
//
//	public ProductSpecificationBuilder(ProductFilter filter, String search) {
//		this.filter = filter;
//		this.search = search;
//	}
//
//	@SuppressWarnings("deprecation")
//	public Specification<Product> build() {
//
//		SearchCriteria searchCriteria = new SearchCriteria("name", "Like", search);
//		SearchCriteria minPriceCriteria = new SearchCriteria("price", ">=", filter.getMinPrice());
//		SearchCriteria maxPriceCriteria = new SearchCriteria("price", "<=", filter.getMaxPrice());
//		SearchCriteria categoryIdCriteria = new SearchCriteria("category.id", "=", filter.getCategory_id()); // changed "category_id" to "category.id"
//
//		Specification<Product> where = null;
//
//		// search
//		if (!StringUtils.isEmpty(search)) {
//			where = new ProductSpecification(searchCriteria);
//		}
//
//		// min Price filter
//		if (filter.getMinPrice() != 0) {
//			if (where != null) {
//				where = where.and(new ProductSpecification(minPriceCriteria));
//			} else {
//				where = new ProductSpecification(minPriceCriteria);
//			}
//		}
//
//		// max Price filter
//		if (filter.getMaxPrice() != 0) {
//			if (where != null) {
//				where = where.and(new ProductSpecification(maxPriceCriteria));
//			} else {
//				where = new ProductSpecification(maxPriceCriteria);
//			}
//		}
//
//		// categoryId filter
//		if(filter.getCategory_id() != 0) {
//			if (where != null) {
//				where = where.and(new ProductSpecification(categoryIdCriteria));
//			} else {
//				where = new ProductSpecification(categoryIdCriteria);
//			}
//		}
//
//		return where;
//	}
//}