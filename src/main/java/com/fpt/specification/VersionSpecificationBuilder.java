package com.fpt.specification;

import com.fpt.entity.Category;
import com.fpt.entity.Version;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class VersionSpecificationBuilder {

    private final String search;
    private final Boolean isActive;


    public VersionSpecificationBuilder(String search, Boolean isActive) {
        this.search = search;
        this.isActive=isActive;
    }

    public Specification<Version> build() {
        Specification<Version> spec = Specification.where(null);

        // search field free text
        if (StringUtils.hasText(search)) {
            String[] fields = {"version","description"};
            for (String field : fields) {
                SearchCriteria criteria = new SearchCriteria(field, "Like", search);
                spec = spec.or(new VersionSpecification(criteria));
            }
        }

        // filter by active
        if (isActive != null) {
            SearchCriteria statusCriteria = new SearchCriteria("isActive", "Equal", isActive);
            spec = spec.and(new VersionSpecification(statusCriteria));
        }
        return spec;
    }
}
