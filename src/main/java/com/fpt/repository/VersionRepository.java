package com.fpt.repository;

import com.fpt.entity.Category;
import com.fpt.entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface VersionRepository extends JpaRepository<Version, Long>, JpaSpecificationExecutor<Version> {

    List<Version> findByIsActiveTrueOrderByCreatedAtDesc();
}
