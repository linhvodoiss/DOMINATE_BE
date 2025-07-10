package com.fpt.specification;

import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class SubscriptionPackageSpecificationBuilder {

	private final String search;
	private final Boolean isActive;

	public SubscriptionPackageSpecificationBuilder(String search,Boolean isActive) {
		this.search = search;
		this.isActive = isActive;
	}
	public SubscriptionPackageSpecificationBuilder(String search) {
		this(search,  null);
	}
	public Specification<SubscriptionPackage> build() {
		Specification<SubscriptionPackage> searchSpec = Specification.where(null); // bắt đầu từ null

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

		return searchSpec;
	}
}
