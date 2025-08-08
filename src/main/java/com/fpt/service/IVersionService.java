package com.fpt.service;

import com.fpt.dto.DocDTO;
import com.fpt.dto.VersionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IVersionService {
    List<VersionDTO> getAll();
    Page<VersionDTO> getAllVersion(Pageable pageable, String search, Boolean isActive);
    Page<VersionDTO> getAllVersionCustomer(Pageable pageable, String search);
    VersionDTO getById(Long id);
    VersionDTO getByIdIfActive(Long id);
    VersionDTO create(VersionDTO dto);
    VersionDTO update(Long id, VersionDTO dto);
    void delete(Long id);
    void deleteMore(List<Long> ids);
}
