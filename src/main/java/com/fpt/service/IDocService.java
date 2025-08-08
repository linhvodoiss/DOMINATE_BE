package com.fpt.service;

import com.fpt.dto.DocDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IDocService {
    List<DocDTO> getAll();
    Page<DocDTO> getAllDoc(Pageable pageable, String search, Boolean isActive,Long categoryId);
    Page<DocDTO> getAllDocCustomer(Pageable pageable, String search,Long categoryId);
    DocDTO getByIdIfActive(Long id);
    DocDTO getById(Long id);
    DocDTO create(DocDTO dto);
    DocDTO update(Long id, DocDTO dto);
    void delete(Long id);
    void deleteMore(List<Long> ids);
}
