package com.fpt.service;

import com.fpt.dto.DocDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IDocService {
    List<DocDTO> getAll();
    Page<DocDTO> getAllDoc(Pageable pageable, String search, Boolean isActive,Long categoryId,Long versionId);
    Page<DocDTO> getAllDocCustomer(Pageable pageable, String search,Long categoryId,Long versionId);
    List<Map<String, Object>> getDocsByAllVersions();
    DocDTO getByIdIfActive(Long id);
    DocDTO getById(Long id);
    DocDTO create(DocDTO dto);
    DocDTO update(Long id, DocDTO dto);
    void delete(Long id);
    void deleteMore(List<Long> ids);
}
