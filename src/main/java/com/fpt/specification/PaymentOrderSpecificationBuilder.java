package com.fpt.specification;

import com.fpt.entity.PaymentOrder;
import com.fpt.entity.PaymentOrder.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class PaymentOrderSpecificationBuilder {

	private final String search;
	private final PaymentStatus status;
	private final Long subscriptionId;
	private final Long userId;
	public PaymentOrderSpecificationBuilder(String search, Long subscriptionId, PaymentStatus status,Long userId) {
		this.search = search;
		this.status = status;
		this.subscriptionId = subscriptionId;
		this.userId = userId;
	}
	public PaymentOrderSpecificationBuilder(String search, Long subscriptionId, PaymentStatus status) {
		this(search, subscriptionId, status, null);
	}

	public Specification<PaymentOrder> build() {
		Specification<PaymentOrder> spec = Specification.where(null);

		// Search LIKE on orderCode and customerName
		if (StringUtils.hasText(search)) {
			String[] fields = {"orderId", "subscriptionPackage.name"};
			Specification<PaymentOrder> searchSpec = Specification.where(null);
			for (String field : fields) {
				searchSpec = searchSpec.or((root, query, cb) -> {
					if (field.contains(".")) {
						String[] parts = field.split("\\.");
						return cb.like(cb.lower(root.get(parts[0]).get(parts[1])), "%" + search.toLowerCase() + "%");
					} else {
						return cb.like(cb.lower(root.get(field)), "%" + search.toLowerCase() + "%");
					}
				});
			}
			spec = spec.and(searchSpec);
		}

		// Filter by enum status
		if (status != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("paymentStatus"), status));
		}

		// Filter by subscriptionId (foreign key)
		if (subscriptionId != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("subscriptionPackage").get("id"), subscriptionId));
		}
		if (userId != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
		}
		return spec;
	}
}
