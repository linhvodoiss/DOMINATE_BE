package com.fpt.specification;

import com.fpt.entity.License;
import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class LicenseSpecificationBuilder {

	private final String search;
	private final SubscriptionPackage.TypePackage type;
	private final Long userId;

	public LicenseSpecificationBuilder(String search,Long userId,SubscriptionPackage.TypePackage type) {
		this.search = search;
		this.userId = userId;
		this.type=type;
	}
	public LicenseSpecificationBuilder(String search) {
		this(search,null, null);
	}

	public Specification<License> build() {
		Specification<License> searchSpec = Specification.where(null); // bắt đầu từ null

		if (StringUtils.hasText(search)) {
			String[] fields = {"licenseKey","ip"};
			for (String field : fields) {
				SearchCriteria criteria = new SearchCriteria(field, "Like", search);
				Specification<License> spec = new LicenseSpecification(criteria);
				searchSpec = searchSpec.or(spec);
			}
		}
		if (userId != null) {
			searchSpec = searchSpec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
		}
		if (type != null) {
			searchSpec = searchSpec.and((root, query, cb) -> cb.equal(root.get("subscriptionPackage").get("typePackage"), type));
		}
		return searchSpec;
	}
}
