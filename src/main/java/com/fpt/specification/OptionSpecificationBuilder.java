package com.fpt.specification;

import com.fpt.entity.License;
import com.fpt.entity.Option;
import com.fpt.entity.SubscriptionPackage;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class OptionSpecificationBuilder {

	private final String search;
	private final Boolean isActive;

	public OptionSpecificationBuilder(String search,Boolean isActive) {
		this.search = search;
		this.isActive=isActive;

	}

	public Specification<Option> build() {
		Specification<Option> searchSpec = Specification.where(null); // bắt đầu từ null

		if (StringUtils.hasText(search)) {
			String[] fields = {"name"};
			for (String field : fields) {
				SearchCriteria criteria = new SearchCriteria(field, "Like", search);
				Specification<Option> spec = new OptionSpecification(criteria);
				searchSpec = searchSpec.or(spec);
			}
		}
		if (isActive != null) {
			searchSpec = searchSpec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
		}

		return searchSpec;
	}
}
