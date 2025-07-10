package com.fpt.service;

import com.fpt.dto.DocDTO;

import java.util.List;

public interface IDocService {
    List<DocDTO> getAll();
    DocDTO getById(Long id);
    DocDTO create(DocDTO dto);
    DocDTO update(Long id, DocDTO dto);
    void delete(Long id);
}
