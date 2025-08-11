package com.fpt.repository;

import java.util.List;

import com.fpt.entity.Doc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fpt.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    List<Category> findByVersionIdAndIsActiveTrue(Long versionId);

}
