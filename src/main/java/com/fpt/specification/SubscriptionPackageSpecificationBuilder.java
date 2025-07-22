package com.fpt.specification;

import com.fpt.entity.SubscriptionPackage;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class SubscriptionPackageSpecificationBuilder {

	private final String search;
	private final Boolean isActive;
	private final Double minPrice;
	private final Double maxPrice;
	private final SubscriptionPackage.TypePackage type;
	private final SubscriptionPackage.BillingCycle cycle;
	public SubscriptionPackageSpecificationBuilder(String search, Boolean isActive, Double minPrice, Double maxPrice,SubscriptionPackage.TypePackage type,SubscriptionPackage.BillingCycle cycle) {
		this.search = search;
		this.isActive = isActive;
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
		this.type=type;
		this.cycle=cycle;
	}


	public Specification<SubscriptionPackage> build() {
		Specification<SubscriptionPackage> searchSpec = Specification.where(null); // bắt đầu từ null

		// Search theo tên
		if (StringUtils.hasText(search)) {
			String[] fields = {"name"};
			for (String field : fields) {
				SearchCriteria criteria = new SearchCriteria(field, "Like", search);
				Specification<SubscriptionPackage> spec = new SubscriptionPackageSpecification(criteria);
				searchSpec = searchSpec.or(spec);
			}
		}


		if (isActive != null) {
			searchSpec = searchSpec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
		}

		if (type != null) {
			searchSpec = searchSpec.and((root, query, cb) -> cb.equal(root.get("typePackage"), type));
		}

		if (cycle != null) {
			searchSpec = searchSpec.and((root, query, cb) -> cb.equal(root.get("billingCycle"), cycle));
		}

		// MIN
		if (minPrice != null) {
			searchSpec = searchSpec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
		}

		// MAX
		if (maxPrice != null) {
			searchSpec = searchSpec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
		}

		return searchSpec;
	}
}
