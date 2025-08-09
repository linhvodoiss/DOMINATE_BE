package com.fpt.specification;

import com.fpt.entity.Doc;
import com.fpt.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class DocSpecificationBuilder {

	private final String search;
	private final Boolean isActive;
	private final Long categoryId;
	private final Long versionId;

	public DocSpecificationBuilder(String search,  Boolean isActive,Long categoryId,Long versionId) {
		this.search = search;
		this.isActive=isActive;
		this.categoryId=categoryId;
		this.versionId=versionId;
	}

	public Specification<Doc> build() {
		Specification<Doc> spec = Specification.where(null);

		// search field free text
		if (StringUtils.hasText(search)) {
			String[] fields = {"title"};
			for (String field : fields) {
				SearchCriteria criteria = new SearchCriteria(field, "Like", search);
				spec = spec.or(new DocSpecification(criteria));
			}
		}

		// filter by active
		if (isActive != null) {
			SearchCriteria statusCriteria = new SearchCriteria("isActive", "Equal", isActive);
			spec = spec.and(new DocSpecification(statusCriteria));
		}


		if (categoryId != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
		}
		if (versionId != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("version").get("id"), versionId));
		}
		return spec;
	}
}
