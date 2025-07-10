package com.fpt.repository;

import com.fpt.entity.Doc;
import com.fpt.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocRepository extends JpaRepository<Doc, Long> {

}
