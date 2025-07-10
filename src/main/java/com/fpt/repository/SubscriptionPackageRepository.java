package com.fpt.repository;

import com.fpt.entity.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubscriptionPackageRepository extends JpaRepository<SubscriptionPackage, Long>, JpaSpecificationExecutor<SubscriptionPackage> {

}
