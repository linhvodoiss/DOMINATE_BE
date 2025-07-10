package com.fpt.service;

import com.fpt.dto.VersionDTO;

import java.util.List;

public interface IVersionService {
    List<VersionDTO> getAll();
    VersionDTO getById(Long id);
    VersionDTO create(VersionDTO dto);
    VersionDTO update(Long id, VersionDTO dto);
    void delete(Long id);
}
