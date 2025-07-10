//package com.fpt.specification;
//
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;
//
//import org.springframework.data.jpa.domain.Specification;
//
//import com.fpt.entity.Product;
//
//public class ProductSpecification implements Specification<Product> {
//
//	private static final long serialVersionUID = 1L;
//	private SearchCriteria criteria;
//
//	public ProductSpecification(SearchCriteria criteria) {
//		this.criteria = criteria;
//	}
//
//	@Override
//	public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//
//		if (criteria.getOperator().equalsIgnoreCase("Like")) {
//			return criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
//		}
//
//		if (criteria.getOperator().equalsIgnoreCase(">=")) {
//			return criteriaBuilder.greaterThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString());
//		}
//
//		if (criteria.getOperator().equalsIgnoreCase("<=")) {
//			return criteriaBuilder.lessThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString());
//		}
//
//		if (criteria.getOperator().equalsIgnoreCase("=")) {
//			if (criteria.getKey().equals("category.id")) {
//				return criteriaBuilder.equal(root.get("category").get("id"), criteria.getValue());
//			} else {
//				return criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue().toString());
//			}
//		}
//		return null;
//	}
//
//}
