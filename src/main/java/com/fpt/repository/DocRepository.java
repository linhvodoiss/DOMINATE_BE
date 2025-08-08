package com.fpt.repository;

import com.fpt.entity.Doc;
import com.fpt.entity.License;
import com.fpt.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DocRepository extends JpaRepository<Doc, Long>, JpaSpecificationExecutor<Doc> {
    Optional<Doc> findByIdAndIsActiveTrue(Long id);

}
