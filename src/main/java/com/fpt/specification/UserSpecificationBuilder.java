package com.fpt.specification;

import com.fpt.entity.SubscriptionPackage;
import com.fpt.entity.User;
import com.fpt.entity.UserStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecificationBuilder {

	private final String search;
	private final Integer status;
	private final Boolean isActive;

	public UserSpecificationBuilder(String search, Integer status,Boolean isActive) {
		this.search = search;
		this.status = status;
		this.isActive=isActive;
	}

	public Specification<User> build() {
		Specification<User> spec = Specification.where(null);

		// search field free text
		if (StringUtils.hasText(search)) {
			String[] fields = {"firstName", "lastName", "email", "userName", "phoneNumber"};
			for (String field : fields) {
				SearchCriteria criteria = new SearchCriteria(field, "Like", search);
				spec = spec.or(new UserSpecification(criteria));
			}
		}

		// filter by status
		if (status != null) {
			SearchCriteria statusCriteria = new SearchCriteria("status", "Equal", status);
			spec = spec.and(new UserSpecification(statusCriteria));
		}
		// filter by active
		if (isActive != null) {
			SearchCriteria statusCriteria = new SearchCriteria("isActive", "Equal", isActive);
			spec = spec.and(new UserSpecification(statusCriteria));
		}

		return spec;
	}
}
