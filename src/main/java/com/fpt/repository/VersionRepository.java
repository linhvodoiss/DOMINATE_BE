package com.fpt.repository;

import com.fpt.entity.Category;
import com.fpt.entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VersionRepository extends JpaRepository<Version, Long> {


}
