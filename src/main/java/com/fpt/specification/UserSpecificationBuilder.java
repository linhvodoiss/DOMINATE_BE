package com.fpt.specification;

import com.fpt.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecificationBuilder {

	private final String search;
	private final Integer status;

	public UserSpecificationBuilder(String search, Integer status) {
		this.search = search;
		this.status = status;
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

		return spec;
	}
}
